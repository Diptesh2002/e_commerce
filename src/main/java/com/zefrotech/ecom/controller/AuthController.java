package com.zefrotech.ecom.controller;
import com.zefrotech.ecom.entity.Role;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.service.AuthService;
import com.zefrotech.ecom.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller

public class AuthController {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "admin", required = false, defaultValue = "false") boolean isAdmin,
            Model model
    ) {
        model.addAttribute("isAdmin", isAdmin);
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletResponse response,
            Model model
    ) {
        Optional<User> userOpt = authService.findByUserName(username);

        if (userOpt.isEmpty() || !passwordEncoder.matches(password, userOpt.get().getPassword())) {
            model.addAttribute("error", "Invalid credentials");
            return "login";
        }

        User user = userOpt.get();
        String token = jwtService.generateToken(user.getUserName());

        // Add JWT to cookies
        Cookie jwtCookie = new Cookie("JWT", token);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(7 * 24 * 60 * 60); // 1 week
        response.addCookie(jwtCookie);

        if (user.getRole() == Role.ROLE_ADMIN) {
            return "redirect:/admin/products";
        }
        return "redirect:/user/products";
    }


    // Render registration page
    @GetMapping("/register")
    public String registerPage() {
        return "register"; // register.html
    }

    @PostMapping("/register")
    public String register(@ModelAttribute User user) {
        user.setRole(Role.ROLE_USER);
        authService.registerUser(user);
        return "redirect:/login";
    }
}
