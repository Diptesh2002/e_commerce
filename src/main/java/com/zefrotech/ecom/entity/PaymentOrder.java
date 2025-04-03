package com.zefrotech.ecom.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Entity@Getter@Setter@NoArgsConstructor@AllArgsConstructor
    @Table(name = "payment_orders")
    public class PaymentOrder {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String razorpayOrderId; // Stores Razorpay's order ID
        private double amount;
        private String currency;
        private String status;


    }

