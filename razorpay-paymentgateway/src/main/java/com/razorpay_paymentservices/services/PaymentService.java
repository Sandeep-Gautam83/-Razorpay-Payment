package com.razorpay_paymentservices.services;

import com.razorpay_paymentservices.dtos.PaymentLinkRequestDto;
import com.razorpay_paymentservices.models.PaymentDetails;
import com.razorpay_paymentservices.models.PaymentStatus;
import com.razorpay_paymentservices.repositories.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PaymentService {

    private final PaymentGateway paymentGateway;
    private final PaymentRepository paymentRepository;

    @Autowired
    public PaymentService(PaymentGateway paymentGateway, PaymentRepository paymentRepository) {
        this.paymentGateway = paymentGateway;
        this.paymentRepository = paymentRepository;
    }

    // 🛠️ Create Payment Link Method
    public String createLink(String orderId) throws JSONException {
        System.out.println("Creating payment link for Order ID: " + orderId);  // Debug log

        PaymentLinkRequestDto paymentLinkRequestDto = new PaymentLinkRequestDto();
        paymentLinkRequestDto.setCustomerName("Sandeep");
        paymentLinkRequestDto.setOrderId(orderId);
        paymentLinkRequestDto.setPhone("6387542596");
        paymentLinkRequestDto.setAmount(100);

        // Generate payment link using the payment gateway
        String paymentLink = paymentGateway.createPaymentLink(paymentLinkRequestDto);

        // Save payment details in the repository
        PaymentDetails paymentResponse = new PaymentDetails();
        paymentResponse.setPaymentLink(paymentLink);
        paymentResponse.setOrderId(orderId);
        paymentResponse.setStatus(PaymentStatus.INITIATED);  // Set initial status

        try {
            PaymentDetails savedPayment = paymentRepository.save(paymentResponse);
            System.out.println("Saved Payment Details: " + savedPayment);  // Debug log
        } catch (Exception e) {
            throw new RuntimeException("Failed to save payment details", e);
        }

        return paymentLink;
    }

    // 🛠️ Get Payment Status Method
    public PaymentStatus getPaymentStatus(String paymentId, String orderId) {
        System.out.println("Fetching payment status for Order ID: " + orderId + ", Payment ID: " + paymentId);  // Debug log

        // Retrieve payment details by order ID
        Optional<PaymentDetails> paymentDetails = paymentRepository.findByOrderId(orderId);

        if (paymentDetails.isEmpty()) {
            System.out.println("No payment found for Order ID: " + orderId);  // Debug log
            throw new RuntimeException("Payment not found");
        }

        // Get payment status from the payment gateway
        PaymentStatus status = paymentGateway.getStatus(paymentId, orderId);

        // Update and save payment details with the new status
        PaymentDetails paymentResponse = paymentDetails.get();
        paymentResponse.setStatus(status);
        paymentResponse.setPaymentId(paymentId);

        try {
            paymentRepository.save(paymentResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update payment status", e);
        }

        System.out.println("Updated payment status to: " + status);  // Debug log
        return status;
    }
}
