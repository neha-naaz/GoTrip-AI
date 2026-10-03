package com.tripflow.booking;

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

class BookingCancelIntegrationTest extends AbstractIntegrationTest {

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
    void cancelPendingBooking_freesSeatForAnotherCustomer() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Cancel Agency", agencyEmail, "AGENCY", "Cancel Agency");
        long tripId = api.createPublishedTrip(agencyToken, 1);

        String customer1 = api.register("Alice", ApiFixtures.uniqueEmail("c1"), "CUSTOMER", null);
        String customer2 = api.register("Bob", ApiFixtures.uniqueEmail("c2"), "CUSTOMER", null);

        long bookingId = api.bookTrip(customer1, tripId);

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customer1))
                .andExpect(status().isOk());

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM bookings WHERE id = ?", String.class, bookingId);
        assertThat(status).isEqualTo("CANCELLED");

        // Seat freed — second customer can book capacity=1 trip
        api.bookTrip(customer2, tripId);
    }

    @Test
    void cancelConfirmedBooking_returnsConflict() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Agency", agencyEmail, "AGENCY", "Agency");
        String customerToken = api.register("Cust", ApiFixtures.uniqueEmail("cust"), "CUSTOMER", null);

        long tripId = api.createPublishedTrip(agencyToken, 2);
        long bookingId = api.bookTrip(customerToken, tripId);
        String providerRef = api.initiatePay(customerToken, bookingId);
        api.webhookSuccess(providerRef);

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isConflict());
    }

    @Test
    void afterCancel_duplicateActiveBookingAllowedAgain() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Agency2", agencyEmail, "AGENCY", "Agency2");
        String customerToken = api.register("Cust2", ApiFixtures.uniqueEmail("cust2"), "CUSTOMER", null);

        long tripId = api.createPublishedTrip(agencyToken, 3);
        long bookingId = api.bookTrip(customerToken, tripId);

        mockMvc.perform(post("/api/bookings/" + bookingId + "/cancel")
                        .header("Authorization", "Bearer " + customerToken))
                .andExpect(status().isOk());

        ObjectNode body = objectMapper.createObjectNode().put("tripId", tripId);
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }
}
