package com.escapehub.escapehub.common.web;

import org.junit.jupiter.api.Test;

import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class HealthControllerTest {

    @Test
    void healthEndpointReturnsOk() throws Exception {

        MockMvc mockMvc = standaloneSetup(
                new HealthController()
        ).build();

        var result = mockMvc.perform(
                        get("/api/v1/health")
                )
                .andExpect(status().isOk())
                .andReturn();

        assertEquals(
                "{\"status\":\"ok\"}",
                result.getResponse().getContentAsString()
        );
    }
}
