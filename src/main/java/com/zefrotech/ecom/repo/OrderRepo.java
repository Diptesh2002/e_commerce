package com.zefrotech.ecom.repo;

import com.zefrotech.ecom.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepo extends JpaRepository<Order, Long> {
    List<Order> getOrderByUser_UserName(String userName);
}
