package com.zefrotech.ecom.controller;

import com.zefrotech.ecom.entity.Cart;
import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.service.AuthService;
import com.zefrotech.ecom.service.CartService;
import com.zefrotech.ecom.service.ProductService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasRole('ROLE_USER')")
public class UserPageController {

    private final ProductService productService;
    private final CartService cartService;
    private final AuthService authService;

    public UserPageController(ProductService productService, CartService cartService, AuthService authService) {
        this.productService = productService;
        this.cartService = cartService;
        this.authService = authService;
    }

    // Display the available products using the 'user/products.html' template.
    @GetMapping("/products")
    public String productsPage(Model model) {
        List<Product> products = productService.getAllProduct(); // Ensure your ProductService method is named correctly
        model.addAttribute("products", products);
        return "user/products"; // This maps to src/main/resources/templates/user/products.html
    }

    // Display the user's cart using the 'user/cart.html' template.
    @GetMapping("/cart")
    public String cartPage(Model model, Principal principal) throws ResourceNotFoundException {
        User user = authService.getCurrentUser(principal.getName());
        Cart cart = cartService.getOrCreateCart(user);
        model.addAttribute("cart", cart);
        return "user/cart"; // This maps to src/main/resources/templates/user/cart.html
    }

    // Add a product to the cart and then redirect back to the cart page.
    @PostMapping("/cart/add/{productId}")
    public String addToCart(@PathVariable Long productId,
                            @RequestParam(value = "quantity", defaultValue = "1") int quantity,
                            Principal principal,
                            RedirectAttributes redirectAttributes) {
        try {
            User user = authService.getCurrentUser(principal.getName());
            cartService.addToCart(user, productId, quantity);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("error", "Error adding product to cart: " + e.getMessage());
        }
        return "redirect:/user/cart";
    }
}
