package com.tripflow.payment.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tripflow.payment.config.PaymentProperties;
import com.tripflow.payment.entity.PaymentStatus;
import com.tripflow.payment.exception.WebhookUnauthorizedException;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RazorpayPaymentProviderTest {

    private RazorpayPaymentProvider provider;
    private PaymentProperties properties;

    @BeforeEach
    void setUp() {
        properties = new PaymentProperties();
        properties.getRazorpay().setKeyId("rzp_test_key");
        properties.getRazorpay().setKeySecret("test_secret");
        properties.getRazorpay().setWebhookSecret("whsec_test");
        provider = new RazorpayPaymentProvider(properties, new ObjectMapper(), HttpClient.newHttpClient());
    }

    @Test
    void verifyCheckoutSignature_acceptsValidHmac() {
        String orderId = "order_ABC";
        String paymentId = "pay_XYZ";
        String signature = RazorpayPaymentProvider.hmacSha256Hex(
                orderId + "|" + paymentId, "test_secret");

        assertThat(provider.verifyCheckoutSignature(orderId, paymentId, signature)).isTrue();
        assertThat(provider.verifyCheckoutSignature(orderId, paymentId, "bad")).isFalse();
    }

    @Test
    void toPaise_convertsInr() {
        assertThat(RazorpayPaymentProvider.toPaise(new BigDecimal("3000.00"))).isEqualTo(300000L);
        assertThat(RazorpayPaymentProvider.toPaise(new BigDecimal("99.99"))).isEqualTo(9999L);
    }

    @Test
    void parseWebhook_paymentCaptured() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode root = mapper.createObjectNode();
        root.put("event", "payment.captured");
        root.putObject("payload")
                .putObject("payment")
                .putObject("entity")
                .put("order_id", "order_123");

        String body = mapper.writeValueAsString(root);
        String signature = RazorpayPaymentProvider.hmacSha256Hex(body, "whsec_test");

        Optional<PaymentProvider.WebhookEvent> event = provider.parseWebhook(body, signature);

        assertThat(event).isPresent();
        assertThat(event.get().providerRef()).isEqualTo("order_123");
        assertThat(event.get().status()).isEqualTo(PaymentStatus.SUCCESS);
    }

    @Test
    void parseWebhook_rejectsBadSignature() {
        assertThatThrownBy(() -> provider.parseWebhook("{\"event\":\"payment.captured\"}", "nope"))
                .isInstanceOf(WebhookUnauthorizedException.class);
    }

    @Test
    void checkoutSession_includesKeyAndOrder() {
        Optional<PaymentProvider.CheckoutSession> session =
                provider.checkoutSession("order_1", new BigDecimal("1500.00"));

        assertThat(session).isPresent();
        assertThat(session.get().keyId()).isEqualTo("rzp_test_key");
        assertThat(session.get().amountPaise()).isEqualTo(150000L);
        assertThat(session.get().currency()).isEqualTo("INR");
        assertThat(session.get().orderId()).isEqualTo("order_1");
    }

    @Test
    void allowsSandboxConfirm_isFalse() {
        assertThat(provider.allowsSandboxConfirm()).isFalse();
    }
}
