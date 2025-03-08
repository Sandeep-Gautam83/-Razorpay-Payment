package com.razorpay_paymentservices.services;

import com.razorpay_paymentservices.dtos.PaymentLinkRequestDto;
import com.razorpay_paymentservices.models.PaymentStatus;
import org.springframework.boot.configurationprocessor.json.JSONException;

public interface PaymentGateway {
    String createPaymentLink(PaymentLinkRequestDto paymentLinkRequestDto) throws JSONException;
    PaymentStatus getStatus(String paymentId, String orderId);
}
