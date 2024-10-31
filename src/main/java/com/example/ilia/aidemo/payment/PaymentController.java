package com.example.ilia.aidemo.payment;

import com.example.ilia.aidemo.payload.StripeLinksResponse;
import com.example.ilia.aidemo.payload.StripeSubscriptionResponse;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @Operation(summary = "DO NOT SEND REQUESTS it is used to process a successful Stripe operation")
    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {
        return paymentService.handleStripePayment(payload, sigHeader);
    }

    @Operation(summary = "before: /getStripeLinks")
    @GetMapping("/stripe-links")
    public StripeLinksResponse getStripeLinks(){
        return paymentService.getStripeLinks();
    }

    @Operation(summary = "before: /getSubscriptionInfo")
    @GetMapping("/subscription-details")
    public StripeSubscriptionResponse getSubscriptionInfo() throws StripeException {
        return paymentService.getSubscriptionInfo();
    }

    @Operation(summary = "before: /cancelSubscription")
    @PostMapping("/cancel-subscription")
    public ResponseEntity<?> cancelSubscription() throws StripeException {
        return paymentService.cancelSubscription();
    }
}
