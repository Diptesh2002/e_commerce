package com.zefrotech.ecom.controller;

import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.List;

@Controller
public class HomeController {

    @Autowired
    private ProductService productService;

    @GetMapping("/")
    public String home(Model model) {
        List<Product> products = productService.getAllProduct();
        model.addAttribute("products", products);
        return "home"; // home.html
    }
}

