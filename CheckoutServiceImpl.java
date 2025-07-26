package com.project.hotelBookingManagement.service;


import com.project.hotelBookingManagement.entity.Booking;
import com.project.hotelBookingManagement.entity.User;
import com.project.hotelBookingManagement.repository.BookingRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.Review;
import com.stripe.model.checkout.Session;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService{

    private final BookingRepository bookingRepository;

    @Override
    public String getCheckoutSession(Booking booking, String successUrl, String failureUrl) throws StripeException {

        log.info("Creating Stripe Session for booking Id: {}",booking.getId());
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Customer customer = Customer.create(
                CustomerCreateParams.builder()
                        .setName(user.getName())
                        .setEmail(user.getEmail())
                        .build()
        );
        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setBillingAddressCollection(SessionCreateParams.BillingAddressCollection.REQUIRED)
                .setCustomer(customer.getId())
                .setSuccessUrl(successUrl)
                .setCancelUrl(failureUrl)
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("usd")
                                                .setUnitAmount(booking.getAmount().longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(booking.getHotel().getName()+" "+booking.getRoom().getType())
                                                                .setDescription("Booking Id" +booking.getId())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);
        booking.setPaymentSessionId(session.getId());
        bookingRepository.save(booking);

        log.info("Stripe Session created for booking Id: {}",booking.getId());
        return session.getUrl();
    }
}
