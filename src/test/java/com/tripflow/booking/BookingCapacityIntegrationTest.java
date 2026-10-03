package com.tripflow.booking;

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

class BookingCapacityIntegrationTest extends AbstractIntegrationTest {

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
    void secondBooking_whenCapacityOne_returnsConflict() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Tiny Agency", agencyEmail, "AGENCY", "Tiny Agency");
        api.verifyAgency(agencyEmail);

        long tripId = api.createPublishedTrip(agencyToken, 1);

        String customer1 = api.register("Alice", ApiFixtures.uniqueEmail("c1"), "CUSTOMER", null);
        String customer2 = api.register("Bob", ApiFixtures.uniqueEmail("c2"), "CUSTOMER", null);

        api.bookTrip(customer1, tripId);

        ObjectNode body = objectMapper.createObjectNode().put("tripId", tripId);
        mockMvc.perform(post("/api/bookings")
                        .header("Authorization", "Bearer " + customer2)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict());
    }
}
