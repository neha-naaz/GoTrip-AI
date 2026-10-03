package com.tripflow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tripflow.support.AbstractIntegrationTest;
import com.tripflow.support.ApiFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class MarketplaceFlowIntegrationTest extends AbstractIntegrationTest {

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
    void registerPublishBookPayWebhook_joinsTripGroup() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String customerEmail = ApiFixtures.uniqueEmail("customer");

        String agencyToken = api.register("Coastal Tours", agencyEmail, "AGENCY", "Coastal Tours");
        api.verifyAgency(agencyEmail);
        String customerToken = api.register("Neha Traveler", customerEmail, "CUSTOMER", null);

        long tripId = api.createPublishedTrip(agencyToken, 4);
        long bookingId = api.bookTrip(customerToken, tripId);
        String providerRef = api.initiatePay(customerToken, bookingId);
        api.webhookSuccess(providerRef);

        String bookingStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM bookings WHERE id = ?", String.class, bookingId);
        assertThat(bookingStatus).isEqualTo("CONFIRMED");

        String paymentStatus = jdbcTemplate.queryForObject(
                "SELECT status FROM payments WHERE provider_ref = ?", String.class, providerRef);
        assertThat(paymentStatus).isEqualTo("SUCCESS");

        MvcResult members = mockMvc.perform(get("/api/trips/" + tripId + "/group/members")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode roster = objectMapper.readTree(members.getResponse().getContentAsString());
        assertThat(roster.isArray()).isTrue();
        assertThat(roster).hasSize(1);
        assertThat(roster.get(0).path("name").asText()).isEqualTo("Neha Traveler");
        assertThat(roster.get(0).path("bookingId").asLong()).isEqualTo(bookingId);
    }
}
