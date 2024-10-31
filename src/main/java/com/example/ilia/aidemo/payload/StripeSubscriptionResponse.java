package com.example.ilia.aidemo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StripeSubscriptionResponse {
    private String planName;
    private String status; // e.g., active, canceled, trialing
    private String activeUntil;
    private boolean autoRenew;

}
