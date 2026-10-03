package com.tripflow.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tripflow.support.AbstractIntegrationTest;
import com.tripflow.support.ApiFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

class PaymentWebhookIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ApiFixtures api;

    @BeforeEach
    void setUp() {
        api = new ApiFixtures(mockMvc, objectMapper, jdbcTemplate);
    }

    @Test
    void duplicateSuccessWebhook_isIdempotent() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Pay Agency", agencyEmail, "AGENCY", "Pay Agency");
        api.verifyAgency(agencyEmail);

        String customerToken = api.register("Pay Customer", ApiFixtures.uniqueEmail("payc"), "CUSTOMER", null);
        long tripId = api.createPublishedTrip(agencyToken, 3);
        long bookingId = api.bookTrip(customerToken, tripId);
        String providerRef = api.initiatePay(customerToken, bookingId);

        api.webhookSuccess(providerRef);
        api.webhookSuccess(providerRef);

        Integer memberCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*) FROM group_members gm
                JOIN trip_groups tg ON tg.id = gm.group_id
                WHERE tg.trip_id = ? AND gm.booking_id = ?
                """,
                Integer.class,
                tripId,
                bookingId);
        assertThat(memberCount).isEqualTo(1);

        String bookingStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM bookings WHERE id = ?", String.class, bookingId);
        assertThat(bookingStatus).isEqualTo("CONFIRMED");
    }

    @Test
    void webhook_withWrongSecret_returnsUnauthorized() throws Exception {
        ObjectNode body = objectMapper.createObjectNode()
                .put("providerRef", "mock_missing")
                .put("status", "SUCCESS");

        mockMvc.perform(post("/api/payments/webhook")
                        .header("X-Tripflow-Webhook-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isUnauthorized());
    }
}
