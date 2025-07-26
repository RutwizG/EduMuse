package com.project.hotelBookingManagement.config;

import com.stripe.Stripe;
import org.springframework.beans.factory.annotation.Value;

public class StripeConfig {

    public StripeConfig(@Value("${stripe.secret.key}") String secretKey) {
        Stripe.apiKey = secretKey;
    }
}
