package com.ecommerce.paymentservice.controllers;

import com.ecommerce.paymentservice.dtos.CreatePaymentLinkRequestDto;
import com.ecommerce.paymentservice.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.is;
import com.fasterxml.jackson.databind.ObjectMapper;

public class PaymentControllerTest {

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private PaymentController paymentController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(paymentController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void testCreatePayment() throws Exception {
        CreatePaymentLinkRequestDto requestDto = new CreatePaymentLinkRequestDto();
        requestDto.setOrderId(1L);
        requestDto.setUserId(1L);
        requestDto.setAmount(100.0);

        String paymentLink = "http://localhost:8082/payments/process/12345";
        when(paymentService.createPaymentLink(any(Long.class), any(Long.class), any(Double.class)))
                .thenReturn(paymentLink);

        mockMvc.perform(post("/payments/create-payment-link")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentLink", is(paymentLink)));
    }

    @Test
    public void testProcessPayment() throws Exception {
        String paymentId = "12345";
        String status = "success";

        when(paymentService.processPayment(any(String.class), any(String.class)))
                .thenReturn(true);

        mockMvc.perform(post("/payments/process/{paymentId}", paymentId)
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(paymentId)))
                .andExpect(jsonPath("$.status", is("SUCCESS")));
    }

    @Test
    public void testProcessPayment_Failed() throws Exception {
        String paymentId = "12345";
        String status = "failed";

        when(paymentService.processPayment(any(String.class), any(String.class)))
                .thenReturn(false);

        mockMvc.perform(post("/payments/process/{paymentId}", paymentId)
                        .param("status", status)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId", is(paymentId)))
                .andExpect(jsonPath("$.status", is("FAILED")));
    }
}