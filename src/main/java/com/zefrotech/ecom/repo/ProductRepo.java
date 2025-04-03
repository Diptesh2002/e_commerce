package com.zefrotech.ecom.repo;

import com.zefrotech.ecom.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepo extends JpaRepository<Product,Long> {
}
