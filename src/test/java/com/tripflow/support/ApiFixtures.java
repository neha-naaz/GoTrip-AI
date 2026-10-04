package com.tripflow.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

/**
 * Shared HTTP helpers for marketplace integration tests.
 */
public final class ApiFixtures {

    public static final String WEBHOOK_SECRET = "test-webhook-secret";

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    public ApiFixtures(MockMvc mockMvc, ObjectMapper objectMapper, JdbcTemplate jdbcTemplate) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    public String register(String name, String email, String role, String agencyName) throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("name", name)
                .put("email", email)
                .put("password", "password1")
                .put("role", role);
        if (agencyName != null) {
            body.put("agencyName", agencyName);
        }

        MvcResult result = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("accessToken")
                .asText();
    }

    public void verifyAgency(String agencyEmail) {
        jdbcTemplate.update(
                """
                UPDATE agency_profiles
                SET verification_status = 'VERIFIED'
                WHERE user_id = (SELECT id FROM users WHERE email = ?)
                """,
                agencyEmail);
    }

    public long createPublishedTrip(String agencyToken, int capacity) throws Exception {
        LocalDate start = LocalDate.now().plusDays(30);
        LocalDate end = start.plusDays(5);

        ObjectNode body = objectMapper.createObjectNode()
                .put("title", "Goa Escape " + UUID.randomUUID().toString().substring(0, 8))
                .put("description", "Beach trip for IT")
                .put("source", "Mumbai")
                .put("destination", "Goa")
                .put("startDate", start.toString())
                .put("endDate", end.toString())
                .put("price", new BigDecimal("25000.00"))
                .put("bookingAmount", new BigDecimal("3000.00"))
                .put("capacity", capacity);

        MvcResult create = mockMvc.perform(post("/api/agency/trips")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();

        long tripId = objectMapper.readTree(create.getResponse().getContentAsString()).path("id").asLong();

        addRequiredItinerary(agencyToken, tripId, start, end);

        mockMvc.perform(post("/api/agency/trips/" + tripId + "/publish")
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isOk());

        return tripId;
    }

    public void addRequiredItinerary(String agencyToken, long tripId, LocalDate start, LocalDate end)
            throws Exception {
        int lengthDays = (int) ChronoUnit.DAYS.between(start, end) + 1;
        for (int day = 1; day <= lengthDays; day++) {
            ObjectNode itinerary = objectMapper.createObjectNode()
                    .put("dayNumber", day)
                    .put("title", "Day " + day)
                    .put("description", "IT itinerary");
            mockMvc.perform(post("/api/agency/trips/" + tripId + "/itineraries")
                            .header("Authorization", "Bearer " + agencyToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(itinerary)))
                    .andExpect(status().isCreated());
        }
    }

    public long bookTrip(String customerToken, long tripId) throws Exception {
        ObjectNode body = objectMapper.createObjectNode().put("tripId", tripId);
        MvcResult result = mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    public String initiatePay(String customerToken, long bookingId) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/bookings/" + bookingId + "/pay")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .path("providerRef")
                .asText();
    }

    public void webhookSuccess(String providerRef) throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("providerRef", providerRef)
                .put("status", "SUCCESS");
        mockMvc.perform(post("/api/payments/webhook")
                        .header("X-Tripflow-Webhook-Secret", WEBHOOK_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    public JsonNode getJson(MvcResult result) throws Exception {
        return objectMapper.readTree(result.getResponse().getContentAsString());
    }

    public static String uniqueEmail(String prefix) {
        return prefix + "+" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }
}
