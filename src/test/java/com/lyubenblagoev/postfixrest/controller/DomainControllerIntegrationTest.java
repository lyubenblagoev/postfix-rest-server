package com.lyubenblagoev.postfixrest.controller;

import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@TestMethodOrder(OrderAnnotation.class)
class DomainControllerIntegrationTest {

    public static final String PATH_API_V_1_DOMAINS = "/api/v1/domains";

    @Autowired
    private MockMvc mockMvc;

    @Test
    @Order(1)
    public void getAllDomains_shouldReturnEmptyList() throws Exception {
        mockMvc.perform(get(PATH_API_V_1_DOMAINS))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("[]")));
    }

    @Test
    @Order(2)
    public void getDomain_shouldReturn404() throws Exception {
        mockMvc.perform(get(PATH_API_V_1_DOMAINS + "/example.com"))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Order(3)
    public void addDomain_shouldSucceed() throws Exception {
        mockMvc.perform(
                post(PATH_API_V_1_DOMAINS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"example.com\", \"enabled\": true }"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(true)));

    }

    @Test
    @Order(4)
    public void edit_shouldDisableDomain() throws Exception {
        mockMvc.perform(
                put(PATH_API_V_1_DOMAINS + "/example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"name\": \"example.com\", \"enabled\": false }"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @Order(5)
    public void getAllDomains_shouldReturnListOfOne() throws Exception {
        mockMvc.perform(get(PATH_API_V_1_DOMAINS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @Order(6)
    public void getDomain_shouldReturnDisabledDomainInstance() throws Exception {
        mockMvc.perform(get(PATH_API_V_1_DOMAINS + "/example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @Order(7)
    public void delete_shouldSucceed() throws Exception {
        mockMvc.perform(delete(PATH_API_V_1_DOMAINS + "/example.com"))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_nonexistentShouldFail() throws Exception {
        mockMvc.perform(delete(PATH_API_V_1_DOMAINS + "/example.org"))
                .andExpect(status().is4xxClientError());
    }

}