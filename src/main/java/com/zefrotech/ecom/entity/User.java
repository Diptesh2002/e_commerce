package com.zefrotech.ecom.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity@Data@AllArgsConstructor@NoArgsConstructor@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1)
    private Long userId;


    private String userName;
    @Column(unique = true, nullable = false)
    private String email;

    private String password;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Cart cart;

    @OneToMany(mappedBy = "user")
    @ToString.Exclude
    private List<Order> orders = new ArrayList<>();

    public void initializeCart() {
        if (this.cart == null) {
            this.cart = new Cart();
            this.cart.setUser(this);
        }
    }


}
