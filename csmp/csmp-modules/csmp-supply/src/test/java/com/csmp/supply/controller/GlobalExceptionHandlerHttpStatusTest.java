package com.csmp.supply.controller;

import com.csmp.common.web.handler.GlobalExceptionHandler;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.NoHandlerFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("dev")
class GlobalExceptionHandlerHttpStatusTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();
    }

    @Test
    void servletExceptionShouldReturnHttp500() throws Exception {
        mockMvc.perform(get("/probe/servlet-404"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value(500))
            .andExpect(jsonPath("$.msg").value("No static resource supply/cloud-platforms/list."));
    }

    @Test
    void noHandlerFoundExceptionShouldReturnHttp404() throws Exception {
        mockMvc.perform(get("/probe/no-handler"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value(404))
            .andExpect(jsonPath("$.msg").value("No endpoint GET /supply/cloud-platforms/list."));
    }

    @RestController
    static class ThrowingController {

        @GetMapping("/probe/servlet-404")
        String servlet404() throws ServletException {
            throw new ServletException("No static resource supply/cloud-platforms/list.");
        }

        @GetMapping("/probe/no-handler")
        String noHandler() throws NoHandlerFoundException {
            throw new NoHandlerFoundException("GET", "/supply/cloud-platforms/list", HttpHeaders.EMPTY);
        }
    }
}
