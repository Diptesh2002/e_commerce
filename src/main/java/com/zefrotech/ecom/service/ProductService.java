package com.zefrotech.ecom.service;

import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.repo.ProductRepo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    @Autowired
    ProductRepo productRepo;

    public Page<Product> getAllProduct(int page, int size) {
        return productRepo.findAll(PageRequest.of(page, size));
    }
    public List<Product> getAllProduct() {
        return productRepo.findAll();
    }

    public Product getProductById(Long id) throws ResourceNotFoundException {
        return productRepo.findById(id).orElseThrow(() -> new ResourceNotFoundException("Product Not Found"));
    }

    public void deleteProduct(Long id){
        productRepo.deleteById(id);
    }

    public void saveProduct(Product product){
        productRepo.save(product);
    }

    public void addProduct(Product product) {
    }


}
