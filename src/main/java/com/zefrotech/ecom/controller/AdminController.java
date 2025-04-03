package com.zefrotech.ecom.controller;

import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.repo.UserRepo;
import com.zefrotech.ecom.service.OrderService;
import com.zefrotech.ecom.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class AdminController {

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepo userRepository;

    @Autowired
    private OrderService orderService;

    // Display the admin product dashboard
    @GetMapping("/products")
    public String manageProducts(Model model) {
        model.addAttribute("products", productService.getAllProduct());
        model.addAttribute("newProduct", new Product());
        // Add users to the model
        model.addAttribute("users", userRepository.findAll());
        return "admin/products";
    }

    // Show the edit product form for a given product ID
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) throws ResourceNotFoundException {
        Product product = productService.getProductById(id); // loads the product with its ID
        model.addAttribute("product", product);
        return "admin/editProduct";  // Corresponds to src/main/resources/templates/admin/editProduct.html
    }

    // Update product endpoint - expects the complete Product object (including its prodId) in the request
    @PostMapping("/products/update")
    public String updateProduct(@Valid @ModelAttribute("product") Product product,
                                BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "admin/editProduct";
        }
        // The product object should already contain its prodId.
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    // Handle adding a new product from the admin dashboard
    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute("newProduct") Product product,
                             BindingResult result,
                             Model model) {
        if (result.hasErrors()) {
            model.addAttribute("products", productService.getAllProduct());
            return "admin/products";
        }
        productService.saveProduct(product);
        return "redirect:/admin/products";
    }

    // Delete product endpoint
    @GetMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products";
    }

//    // List all users for admin
//    @GetMapping("/users")
//    public String listUsers(Model model) {
//        model.addAttribute("users", userRepository.findAll());
//        return "userList"; // Ensure userList.html exists
//    }

    // List all orders for admin
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin/orders";
    }
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userRepository.findAll());
        return "admin/users"; // Now points to admin/users.html
    }
}


