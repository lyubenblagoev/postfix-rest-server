package com.lyubenblagoev.postfixrest.controller;

import com.lyubenblagoev.postfixrest.security.JwtTokenProvider;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"dev"})
@TestMethodOrder(OrderAnnotation.class)
class DomainControllerIntegrationTest {

    public static final String PATH_API_V_1_DOMAINS = "/api/v1/domains";

    private final MockMvc mockMvc;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public DomainControllerIntegrationTest(MockMvc mockMvc, JwtTokenProvider jwtTokenProvider) {
        this.mockMvc = mockMvc;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Test
    @Order(1)
    public void getAllDomains_shouldReturnEmptyList() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                get(PATH_API_V_1_DOMAINS)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(50)))
                .andExpect(jsonPath("$.total", is(0)));
    }

    @Test
    @Order(2)
    public void getDomain_shouldReturn403() throws Exception {
        mockMvc.perform(get(PATH_API_V_1_DOMAINS + "/example.com"))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(3)
    public void addDomain_shouldSucceed() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                post(PATH_API_V_1_DOMAINS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{ \"name\": \"example.com\", \"enabled\": true }"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(true)));

    }

    @Test
    @Order(4)
    public void edit_shouldDisableDomain() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                put(PATH_API_V_1_DOMAINS + "/example.com")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{ \"name\": \"example.com\", \"enabled\": false }"))
                .andExpect(status().is2xxSuccessful())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @Order(5)
    public void getAllDomains_shouldReturnListOfOne() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                get(PATH_API_V_1_DOMAINS)
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.total", is(1)));
    }

    @Test
    @Order(6)
    public void getAllDomains_shouldReturnRequestedPage() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                post(PATH_API_V_1_DOMAINS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + token)
                        .content("{ \"name\": \"example.net\", \"enabled\": true }"))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get(PATH_API_V_1_DOMAINS + "?page=2&pageSize=1")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(2)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.total", is(2)))
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].name", is("example.net")));
    }

    @Test
    @Order(7)
    public void getDomain_shouldReturnDisabledDomainInstance() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                get(PATH_API_V_1_DOMAINS + "/example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("example.com")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @Order(8)
    public void delete_shouldSucceed() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                delete(PATH_API_V_1_DOMAINS + "/example.com")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    public void delete_nonexistentShouldFail() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(
                delete(PATH_API_V_1_DOMAINS + "/example.org")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    public void list_shouldRejectPageSizeAboveConfiguredMaximum() throws Exception {
        String token = jwtTokenProvider.createToken("admin@example.com", Collections.emptyList());
        mockMvc.perform(get(PATH_API_V_1_DOMAINS + "?pageSize=201")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

}
