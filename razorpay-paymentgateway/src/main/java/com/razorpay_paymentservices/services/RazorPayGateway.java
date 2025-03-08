package com.razorpay_paymentservices.services;

import com.razorpay.Payment;
import com.razorpay.PaymentLink;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay_paymentservices.dtos.PaymentLinkRequestDto;
import com.razorpay_paymentservices.models.PaymentStatus;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

@Service
public class RazorPayGateway implements PaymentGateway {

    private final RazorpayClient razorpayClient;

    @Autowired
    public RazorPayGateway(RazorpayClient razorpayClient) {
        this.razorpayClient = razorpayClient;
    }

    @Override
    public String createPaymentLink(PaymentLinkRequestDto paymentLinkRequestDto) {
        JSONObject paymentLinkRequest = new JSONObject();
        paymentLinkRequest.put("amount", paymentLinkRequestDto.getAmount());
        paymentLinkRequest.put("currency", "INR");
        paymentLinkRequest.put("expire_by", LocalDate.now().plusDays(7).atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        paymentLinkRequest.put("description", "Payment for order no " + paymentLinkRequestDto.getOrderId());

        // Customer Details
        JSONObject customer = new JSONObject();
        customer.put("name", paymentLinkRequestDto.getCustomerName());
        customer.put("contact", paymentLinkRequestDto.getPhone());
        customer.put("email", "tradinge083@gmail.com");
        paymentLinkRequest.put("customer", customer);

        // Additional Information
        JSONObject notes = new JSONObject();
        notes.put("policy_name", "Trading Expert");
        paymentLinkRequest.put("notes", notes);
        paymentLinkRequest.put("callback_url", "https://www.tradingexpert.com");
        paymentLinkRequest.put("callback_method", "get");

        try {
            PaymentLink payment = razorpayClient.paymentLink.create(paymentLinkRequest);
            return payment.get("short_url");
        } catch (RazorpayException e) {
            throw new RuntimeException("Failed to create payment link", e);
        }
    }

    @Override
    public PaymentStatus getStatus(String paymentId, String orderId) {
        try {
            Payment payment = razorpayClient.payments.fetch(paymentId);
            String statusType = payment.get("status");
            return switch (statusType) {
                case "captured" -> PaymentStatus.SUCCESS;
                case "failed" -> PaymentStatus.FAILURE;
                default -> PaymentStatus.INITIATED;
            };
        } catch (RazorpayException e) {
            throw new RuntimeException("Unable to fetch the payment details", e);
        }
    }
}
