package com.tripflow.trip;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tripflow.support.AbstractIntegrationTest;
import com.tripflow.support.ApiFixtures;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

class TripImageIntegrationTest extends AbstractIntegrationTest {

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
    void uploadAndUrl_thenLockedAfterPublish() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Photo Agency", agencyEmail, "AGENCY", "Photo Agency");

        ObjectNode body = objectMapper.createObjectNode()
                .put("title", "Photo Trip")
                .put("description", "With gallery")
                .put("source", "Delhi")
                .put("destination", "Goa")
                .put("startDate", java.time.LocalDate.now().plusDays(20).toString())
                .put("endDate", java.time.LocalDate.now().plusDays(25).toString())
                .put("price", "20000.00")
                .put("bookingAmount", "3000.00")
                .put("capacity", 8);

        MvcResult create = mockMvc.perform(post("/api/agency/trips")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long tripId = objectMapper.readTree(create.getResponse().getContentAsString()).path("id").asLong();

        MockMultipartFile file = new MockMultipartFile(
                "file", "cover.jpg", "image/jpeg", new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 0x00});

        MvcResult upload = mockMvc.perform(multipart("/api/agency/trips/" + tripId + "/images/upload")
                        .file(file)
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode uploaded = objectMapper.readTree(upload.getResponse().getContentAsString());
        assertThat(uploaded.path("sourceType").asText()).isEqualTo("UPLOAD");
        assertThat(uploaded.path("cover").asBoolean()).isTrue();
        assertThat(uploaded.path("url").asText()).startsWith("/api/media/");

        ObjectNode urlBody = objectMapper.createObjectNode()
                .put("url", "https://images.example.com/beach.jpg");
        mockMvc.perform(post("/api/agency/trips/" + tripId + "/images/url")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlBody)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/agency/trips/" + tripId + "/publish")
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/agency/trips/" + tripId + "/images/url")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlBody)))
                .andExpect(status().isConflict());

        MvcResult detail = mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/trips/" + tripId))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode published = objectMapper.readTree(detail.getResponse().getContentAsString());
        assertThat(published.path("coverImageUrl").asText()).startsWith("/api/media/");
        assertThat(published.path("images").isArray()).isTrue();
        assertThat(published.path("images")).hasSize(2);
    }

    @Test
    void deleteImage_allowedOnlyOnDraft() throws Exception {
        String agencyEmail = ApiFixtures.uniqueEmail("agency");
        String agencyToken = api.register("Del Agency", agencyEmail, "AGENCY", "Del Agency");
        long tripId = api.createPublishedTrip(agencyToken, 4);

        // createPublishedTrip already published — create a fresh draft instead
        ObjectNode body = objectMapper.createObjectNode()
                .put("title", "Draft Photos")
                .put("source", "Pune")
                .put("destination", "Goa")
                .put("startDate", java.time.LocalDate.now().plusDays(10).toString())
                .put("endDate", java.time.LocalDate.now().plusDays(12).toString())
                .put("price", "10000.00")
                .put("bookingAmount", "2000.00")
                .put("capacity", 5);
        MvcResult create = mockMvc.perform(post("/api/agency/trips")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andReturn();
        long draftId = objectMapper.readTree(create.getResponse().getContentAsString()).path("id").asLong();

        ObjectNode urlBody = objectMapper.createObjectNode().put("url", "https://images.example.com/a.jpg");
        MvcResult added = mockMvc.perform(post("/api/agency/trips/" + draftId + "/images/url")
                        .header("Authorization", "Bearer " + agencyToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(urlBody)))
                .andExpect(status().isCreated())
                .andReturn();
        long imageId = objectMapper.readTree(added.getResponse().getContentAsString()).path("id").asLong();

        mockMvc.perform(delete("/api/agency/trips/" + draftId + "/images/" + imageId)
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isNoContent());

        // published trip from helper — delete must fail
        mockMvc.perform(delete("/api/agency/trips/" + tripId + "/images/1")
                        .header("Authorization", "Bearer " + agencyToken))
                .andExpect(status().isConflict());
    }
}
