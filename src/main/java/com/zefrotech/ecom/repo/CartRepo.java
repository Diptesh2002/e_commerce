package com.zefrotech.ecom.repo;

import com.zefrotech.ecom.entity.Cart;
import com.zefrotech.ecom.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepo extends JpaRepository<Cart,Long> {
    Optional<Cart> findByUser(User user);
}
