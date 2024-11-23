package com.example.ilia.aidemo.payment;

import com.example.ilia.aidemo.entity.User;
import com.example.ilia.aidemo.payload.StripeLinksResponse;
import com.example.ilia.aidemo.payload.StripeSubscriptionResponse;
import com.example.ilia.aidemo.repository.UserRepository;
import com.example.ilia.aidemo.service.UserService;
import com.stripe.exception.InvalidRequestException;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.SubscriptionUpdateParams;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;


import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class PaymentService {
    private final UserService userService;
    private final UserRepository userRepository;

    public ResponseEntity<String> handleStripePayment(String payload, String sigHeader) throws StripeException {
        Event event;


        try {
            event = Webhook.constructEvent(payload, sigHeader, "whsec_A9GW1as7sW4jXktMKmWnFf5JuOe9NnRF");
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing event");
        }
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);

        // Handle unknown event types
        if (event.getType().equals("checkout.session.completed")) {// Retrieve session details and update reservation
            if (session != null) {
                //User setup
                String userId = session.getClientReferenceId();
                String subscriptionId = session.getSubscription();
                System.out.println(userId);
                User user = userRepository.findById(userId).orElseThrow();
                user.setTier(getTier(session));
                user.setCredits(2147483647);
                user.setSubscriptionId(subscriptionId);
                System.out.println(subscriptionId);
                userRepository.save(user);


                //set the tier


            } else {
                throw new RuntimeException("Error whith Stripe");
            }

            return ResponseEntity.ok("Received");

        }
        return null;
    }




    private String getTier(Session session) throws StripeException {
        Map<String, String> retrievedMetadata = session.getMetadata();
        return retrievedMetadata.get("name");
    }


    public StripeLinksResponse getStripeLinks() {
        User user = userService.getCurrentUser();
        StripeLinksResponse stripeLinksResponse = new StripeLinksResponse();
        stripeLinksResponse.setProLink(getProSubscription(user));
        stripeLinksResponse.setPremiumLink(getPremiumSubscription(user));
        return stripeLinksResponse;
    }



    public Subscription getSubscriptionFromJWT() throws StripeException {
        Optional<User> userOptional = userService.getCurrentUserOptional();
        if (userOptional.isEmpty()) return null;
        User user = userOptional.get();
        String subscriptionId = user.getSubscriptionId();

        if(subscriptionId.equals("none")){
            return null;
        }


        try {
            // Attempt to retrieve the subscription
            return Subscription.retrieve(subscriptionId);
        } catch (InvalidRequestException e) {
            // Check if the exception is due to a missing resource (subscription not found)
            if ("resource_missing".equals(e.getCode()) && e.getMessage().contains("No such subscription")) {
                System.err.println("Subscription not found for ID: " + subscriptionId);
                return null; // Or throw a custom exception if preferred
            }
            // If it's a different error, rethrow it
            throw e;
        }
    }


    private String getProSubscription(User user) {
        String initialLink = "https://buy.stripe.com/dR6aEX23f7qWeIw8ww";
        return initialLink+"?prefilled_email="+URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                +"&client_reference_id="+user.getId();
    }

    private String getPremiumSubscription(User user) {
        String initialLink = "https://buy.stripe.com/cN2dR9eQ1cLgcAo4gh";
        return initialLink+"?prefilled_email="+ URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8)
                +"&client_reference_id="+user.getId();
    }

    public ResponseEntity<?> cancelSubscription() throws StripeException {
        Subscription resource = getSubscriptionFromJWT();


        SubscriptionUpdateParams params =
                SubscriptionUpdateParams.builder().setCancelAtPeriodEnd(true).build();

        resource.update(params);


        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Subscription was successfully canceled. You will keep the benefits of your subscriptions until the next billing period");
    }

    public StripeSubscriptionResponse getSubscriptionInfo() throws StripeException {

        StripeSubscriptionResponse stripeSubscriptionResponse = new StripeSubscriptionResponse();

        Subscription subscription = getSubscriptionFromJWT();

        if(subscription==null) return handleEmptySubscription();

        String productId = subscription.getItems().getData().get(0).getPrice().getProduct();
        String planName = Product.retrieve(productId).getMetadata().get("name");
        String status = subscription.getStatus();
        String activeUntil = convertLongToDate(subscription.getCurrentPeriodEnd());
        boolean autoRenew = !subscription.getCancelAtPeriodEnd();

        if(!autoRenew) planName = "FREE"; //?????



        stripeSubscriptionResponse.setPlanName(planName);
        stripeSubscriptionResponse.setStatus(status);
        stripeSubscriptionResponse.setActiveUntil(activeUntil);
        stripeSubscriptionResponse.setAutoRenew(autoRenew);

        return stripeSubscriptionResponse;

    }

    private StripeSubscriptionResponse handleEmptySubscription() {
        StripeSubscriptionResponse stripeSubscriptionResponse = new StripeSubscriptionResponse();
        stripeSubscriptionResponse.setPlanName("FREE");
        stripeSubscriptionResponse.setStatus("NOT ACTIVE");
        stripeSubscriptionResponse.setActiveUntil(null);
        stripeSubscriptionResponse.setAutoRenew(false);
        return stripeSubscriptionResponse;
    }

    private String convertLongToDate(Long currentPeriodEnd) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochSecond(currentPeriodEnd), ZoneId.systemDefault());

        // Create a formatter for the desired output
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM d, yyyy");

        // Format the date
        return dateTime.format(formatter);
    }
}
