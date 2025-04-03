package com.zefrotech.ecom.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.zefrotech.ecom.entity.PaymentOrder;
import com.zefrotech.ecom.repo.PaymentOrderRepository;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    @Value("${razorpay.key.id}")
    private String keyId;

    @Value("${razorpay.key.secret}")
    private String keySecret;

    @Autowired
    private PaymentOrderRepository paymentOrderRepository;

    @Transactional(rollbackFor = {Exception.class})
    public String createOrder(double amount) {
        try {
            validateConfiguration();
            validateAmount(amount);


            PaymentOrder pendingOrder = createPendingOrderInDatabase(amount);


            Order razorpayOrder = createRazorpayOrder(amount);


            updateOrderStatus(pendingOrder, razorpayOrder.get("id"), "COMPLETED");

            return razorpayOrder.toString();
        } catch (RazorpayException e) {
            logger.error("Razorpay API failed: {}", e.getMessage());
            throw new RuntimeException("Payment gateway error", e);
        } catch (DataAccessException e) {
            logger.error("Database operation failed: {}", e.getMessage());
            throw new RuntimeException("Database error", e);
        }
    }

    private void validateConfiguration() {
        if (keyId == null || keyId.isEmpty() || keySecret == null || keySecret.isEmpty()) {
            throw new IllegalStateException("Razorpay keys are not configured.");
        }
    }

    private void validateAmount(double amount) {
        if (amount < 1.0) {
            throw new IllegalArgumentException("Amount must be ≥ 1 INR.");
        }
    }

    private PaymentOrder createPendingOrderInDatabase(double amount) {
        PaymentOrder order = new PaymentOrder();
        order.setAmount(amount);
        order.setCurrency("INR");
        order.setStatus("PENDING");
        return paymentOrderRepository.save(order);
    }

    private Order createRazorpayOrder(double amount) throws RazorpayException {
        RazorpayClient razorpayClient = new RazorpayClient(keyId, keySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100));
        orderRequest.put("currency", "INR");
        orderRequest.put("payment_capture", 1);
        return razorpayClient.orders.create(orderRequest);
    }

    private void updateOrderStatus(PaymentOrder order, String razorpayId, String status) {
        order.setRazorpayOrderId(razorpayId);
        order.setStatus(status);
        paymentOrderRepository.save(order);
    }
}


