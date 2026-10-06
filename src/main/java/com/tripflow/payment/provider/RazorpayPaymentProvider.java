package com.tripflow.payment.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tripflow.payment.config.PaymentProperties;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.PaymentGatewayException;
import com.tripflow.payment.exception.PaymentGatewayUnauthorizedException;
import com.tripflow.payment.exception.PaymentNotAllowedException;
import com.tripflow.payment.exception.WebhookUnauthorizedException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.Optional;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Razorpay Orders + Checkout + webhook/signature verification.
 * Keys come from {@code tripflow.payment.razorpay.*}.
 */
public class RazorpayPaymentProvider implements PaymentProvider {

    public static final String NAME = "RAZORPAY";
    private static final Logger log = LoggerFactory.getLogger(RazorpayPaymentProvider.class);
    private static final String CURRENCY = "INR";

    private final PaymentProperties.Razorpay razorpay;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public RazorpayPaymentProvider(PaymentProperties paymentProperties) {
        this(paymentProperties, new ObjectMapper(), HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build());
    }

    RazorpayPaymentProvider(PaymentProperties paymentProperties, ObjectMapper objectMapper, HttpClient httpClient) {
        this.razorpay = paymentProperties.getRazorpay();
        this.objectMapper = objectMapper;
        this.httpClient = httpClient;
        if (isBlank(razorpay.getKeyId()) || isBlank(razorpay.getKeySecret())) {
            throw new IllegalStateException(
                    "tripflow.payment.provider=razorpay requires key-id and key-secret");
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean allowsSandboxConfirm() {
        return false;
    }

    @Override
    public CreateOrderResult createOrder(CreateOrderRequest request) {
        long amountPaise = toPaise(request.amountInr());
        try {
            ObjectNode body = objectMapper.createObjectNode();
            body.put("amount", amountPaise);
            body.put("currency", CURRENCY);
            body.put("receipt", truncate(request.receipt(), 40));
            body.putObject("notes").put("bookingId", String.valueOf(request.bookingId()));

            String credentials = razorpay.getKeyId() + ":" + razorpay.getKeySecret();
            String basic = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));

            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(trimTrailingSlash(razorpay.getApiBaseUrl()) + "/orders"))
                    .timeout(Duration.ofSeconds(20))
                    .header("Authorization", "Basic " + basic)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status == 401 || status == 403) {
                log.warn("Razorpay order create auth failed status={}", status);
                throw new PaymentGatewayUnauthorizedException();
            }
            if (status < 200 || status >= 300) {
                log.warn("Razorpay order create failed status={} body={}", status, response.body());
                throw new PaymentGatewayException("Could not create Razorpay order");
            }

            JsonNode json = objectMapper.readTree(response.body());
            String orderId = json.path("id").asText(null);
            if (isBlank(orderId)) {
                throw new PaymentNotAllowedException("Razorpay order response missing id");
            }
            return new CreateOrderResult(orderId);
        } catch (PaymentNotAllowedException | PaymentGatewayException | PaymentGatewayUnauthorizedException ex) {
            throw ex;
        } catch (Exception ex) {
            log.error("Razorpay order create error", ex);
            throw new PaymentGatewayException("Could not create Razorpay order");
        }
    }

    @Override
    public Optional<CheckoutSession> checkoutSession(String providerRef, BigDecimal amount) {
        return Optional.of(new CheckoutSession(
                razorpay.getKeyId(),
                toPaise(amount),
                CURRENCY,
                providerRef));
    }

    @Override
    public boolean verifyCheckoutSignature(String orderId, String paymentId, String signature) {
        if (isBlank(orderId) || isBlank(paymentId) || isBlank(signature)) {
            return false;
        }
        String payload = orderId + "|" + paymentId;
        String expected = hmacSha256Hex(payload, razorpay.getKeySecret());
        return constantTimeEquals(expected, signature);
    }

    @Override
    public Optional<WebhookEvent> parseWebhook(String rawBody, String signatureHeader) {
        if (isBlank(rawBody)) {
            return Optional.empty();
        }
        String webhookSecret = razorpay.getWebhookSecret();
        if (isBlank(webhookSecret)) {
            log.warn("Razorpay webhook received but webhook-secret is not configured");
            return Optional.empty();
        }
        String expected = hmacSha256Hex(rawBody, webhookSecret);
        if (!constantTimeEquals(expected, signatureHeader)) {
            throw new WebhookUnauthorizedException();
        }

        try {
            JsonNode root = objectMapper.readTree(rawBody);
            String event = root.path("event").asText("");
            JsonNode paymentEntity = root.path("payload").path("payment").path("entity");
            String orderId = paymentEntity.path("order_id").asText(null);
            if (isBlank(orderId)) {
                orderId = root.path("payload").path("order").path("entity").path("id").asText(null);
            }
            if (isBlank(orderId)) {
                return Optional.empty();
            }
            return switch (event) {
                case "payment.captured", "order.paid" -> Optional.of(new WebhookEvent(orderId, PaymentStatus.SUCCESS));
                case "payment.failed" -> Optional.of(new WebhookEvent(orderId, PaymentStatus.FAILED));
                default -> Optional.empty();
            };
        } catch (Exception ex) {
            log.warn("Failed to parse Razorpay webhook", ex);
            return Optional.empty();
        }
    }

    static long toPaise(BigDecimal amountInr) {
        if (amountInr == null || amountInr.signum() <= 0) {
            throw new PaymentNotAllowedException("Payment amount must be positive");
        }
        long paise = amountInr.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
        if (paise < 100) {
            throw new PaymentNotAllowedException("Payment amount must be at least 100 paise (₹1)");
        }
        return paise;
    }

    static String hmacSha256Hex(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception ex) {
            throw new IllegalStateException("HMAC failed", ex);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        if (left.length != right.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < left.length; i++) {
            result |= left[i] ^ right[i];
        }
        return result == 0;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    private static String trimTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return "https://api.razorpay.com/v1";
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}
