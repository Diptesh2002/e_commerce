package com.zefrotech.ecom.controller;

import com.zefrotech.ecom.entity.Cart;
import com.zefrotech.ecom.entity.Order;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.service.AuthService;
import com.zefrotech.ecom.service.CartService;
import com.zefrotech.ecom.service.OrderService;
import com.zefrotech.ecom.service.PaymentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final AuthService authService;
    private final CartService cartService;

    @Value("${razorpay.key.id}")
    
    private String razorpayKeyId;

    public OrderController(OrderService orderService, PaymentService paymentService,
                           AuthService authService, CartService cartService) {
        this.orderService = orderService;
        this.paymentService = paymentService;
        this.authService = authService;
        this.cartService = cartService;
    }
    @GetMapping("/checkout")
    public String checkout(Model model, Principal principal) throws ResourceNotFoundException {
        User user = authService.getCurrentUser(principal.getName());
        Cart cart = cartService.getOrCreateCart(user);

        // Check if cart is empty
        if (cart == null || cart.getItems().isEmpty()) {
            model.addAttribute("error", "Your cart is empty. Please add items to your cart before checking out.");
            return "user/cart";  // Or you can return a dedicated 'empty-cart' view.
        }

        BigDecimal totalPrice = orderService.calculateTotal(cart);
        model.addAttribute("cart", cart);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("shippingAddress", "");
        return "user/checkout"; // This resolves to src/main/resources/templates/user/checkout.html
    }



    // Process checkout: create order from cart with shipping details
    @PostMapping("/checkout")
    public String processCheckout(
            @RequestParam("shippingAddress") String shippingAddress,
            @RequestParam("city") String city,
            @RequestParam("zipCode") String zipCode,
            Principal principal
    ) throws ResourceNotFoundException {
        User user = authService.getCurrentUser(principal.getName());
        Order order = orderService.createOrderFromCart(user, shippingAddress, city, zipCode);
        System.out.println("Proceeding to payment for Order ID: " + order.getId());
        return "redirect:/user/payment?orderId=" + order.getId();
    }

    // Display payment page
    @GetMapping("/payment")
    public String paymentPage(@RequestParam("orderId") Long orderId, Model model) throws ResourceNotFoundException {
        Order order = orderService.getOrderDetails(orderId);
        model.addAttribute("order", order);
        model.addAttribute("totalPrice", order.getTotalPrice());

        try {
            String paymentOrderDetails = paymentService.createOrder(order.getTotalPrice().doubleValue());
            if (paymentOrderDetails == null || paymentOrderDetails.trim().isEmpty()) {
                model.addAttribute("error", "Payment initialization returned no details. Please try again.");
                return "user/payment-error"; // Ensure this template exists
            }
            model.addAttribute("paymentOrderDetails", paymentOrderDetails);
        } catch (RuntimeException e) {
            model.addAttribute("error", "Failed to initialize payment: " + e.getMessage());
            return "user/payment-error"; // Ensure this template exists
        }

        model.addAttribute("razorpayKeyId", razorpayKeyId);
        return "user/payment"; // Ensure this template exists
    }

    // Process payment submission
    @PostMapping("/payment")
    public String processPayment(@RequestParam("orderId") Long orderId, Model model) throws ResourceNotFoundException {
        Order order = orderService.getOrderDetails(orderId);
        try {
            String paymentOrderId = paymentService.createOrder(order.getTotalPrice().doubleValue());
            if (paymentOrderId == null || paymentOrderId.trim().isEmpty()) {
                model.addAttribute("error", "Payment processing returned an empty response.");
                return "user/payment-error";
            }
            return "redirect:/user/orders/" + orderId;
        } catch (Exception e) {
            model.addAttribute("error", "Payment processing failed: " + e.getMessage());
            return "user/payment-error";
        }
    }

    // View order details
    @GetMapping("/orders/{orderId}")
    public String viewOrderDetails(@PathVariable Long orderId, Model model) throws ResourceNotFoundException {
        Order order = orderService.getOrderDetails(orderId);
        model.addAttribute("order", order);
        return "user/order-details";
    }

    // View orders for the current user
    @GetMapping("/orders")
    public String viewUserOrders(Principal principal, Model model) throws ResourceNotFoundException {
        User user = authService.getCurrentUser(principal.getName());
        List<Order> orders = orderService.getOrdersByUser(user);
        model.addAttribute("orders", orders);
        return "user/orders";
    }

    // Handle payment success and update order status to PAID
    @PostMapping("/payment/success/{orderId}")
    public String handlePaymentSuccess(@PathVariable Long orderId) throws ResourceNotFoundException {
        Order order = orderService.getOrderDetails(orderId);
        order.setStatus("PAID");
        orderService.updateOrder(order);
        return "redirect:/user/orders";
    }
}



