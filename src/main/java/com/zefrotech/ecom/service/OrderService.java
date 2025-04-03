package com.zefrotech.ecom.service;

import com.zefrotech.ecom.entity.Cart;
import com.zefrotech.ecom.entity.Order;
import com.zefrotech.ecom.entity.OrderItem;
import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.repo.OrderRepo;
import com.zefrotech.ecom.repo.UserRepo;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepo orderRepo;
    private final UserRepo userRepository;
    private final ProductService productService;
    private final CartService cartService;

    @Autowired
    public OrderService(OrderRepo orderRepo, UserRepo userRepository,
                        ProductService productService, CartService cartService) {
        this.orderRepo = orderRepo;
        this.userRepository = userRepository;
        this.productService = productService;
        this.cartService = cartService;
    }

    public Order placeOrder(Order order) {
        return orderRepo.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepo.findAll();
    }

    @Transactional
    public Order createOrderFromProducts(String username, List<Long> productIds) throws ResourceNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());

        List<OrderItem> items = productIds.stream().map(productId -> {
            try {
                Product product = productService.getProductById(productId);
                if (product.getStockQuantity() <= 0) {
                    throw new ResourceNotFoundException("Product out of stock: " + product.getProdName());
                }
                OrderItem item = new OrderItem();
                item.setProduct(product);
                item.setQuantity(1);
                item.setPriceAtPurchase(product.getProdPrice());
                item.setOrder(order);
                return item;
            } catch (ResourceNotFoundException e) {
                throw new RuntimeException("Error processing order item: " + productId, e);
            }
        }).collect(Collectors.toList());

        order.setItems(items);
        return orderRepo.save(order);
    }

    @Transactional
    public Order createOrderFromCart(User user, String shippingAddress, String city, String zipCode)
            throws ResourceNotFoundException {
        Cart cart = cartService.getOrCreateCart(user);

        // Validate that the cart is not empty.
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new ResourceNotFoundException("Cart is empty for user: " + user.getUserName());
        }

        // Create new order from the cart
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setCity(city);
        order.setZipCode(zipCode);
        order.setTotalPrice(calculateTotal(cart));

        // Convert cart items into order items
        Order finalOrder = order;
        cart.getItems().forEach(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPriceAtPurchase(cartItem.getProduct().getProdPrice());
            orderItem.setOrder(finalOrder);
            finalOrder.getItems().add(orderItem);
        });

        // Save the order and then clear the cart
        order = orderRepo.save(order);
        cartService.clearCart(cart);
        return order;
    }


    public BigDecimal calculateTotal(Cart cart) {
        return BigDecimal.valueOf(cart.getItems().stream()
                .mapToDouble(item -> item.getProduct().getProdPrice().doubleValue() * item.getQuantity())
                .sum());
    }

    public Order getOrderDetails(Long orderId) throws ResourceNotFoundException {
        return orderRepo.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepo.getOrderByUser_UserName(user.getUserName());
    }

    public Order updateOrder(Order order) {
        return orderRepo.save(order);
    }
}

