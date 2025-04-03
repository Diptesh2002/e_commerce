package com.zefrotech.ecom.service;

import com.zefrotech.ecom.entity.Cart;
import com.zefrotech.ecom.entity.CartItem;
import com.zefrotech.ecom.entity.Product;
import com.zefrotech.ecom.entity.User;
import com.zefrotech.ecom.exception.ResourceNotFoundException;
import com.zefrotech.ecom.repo.CartRepo;
import com.zefrotech.ecom.repo.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class CartService {
    @Autowired
    CartRepo cartRepository;
    @Autowired
    ProductService productService;
    public Cart getOrCreateCart(User user) {
        return cartRepository.findByUser(user)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });
    }

    public void addToCart(User user, Long productId, int quantity) throws ResourceNotFoundException {
        Cart cart = getOrCreateCart(user);
        Product product = productService.getProductById(productId);

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getProdId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProduct(product);
            newItem.setQuantity(quantity);
            cart.addItem(newItem);
        }

        cartRepository.save(cart);
    }
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public Cart getCartByUser(User user) {
        return cartRepository.findByUser(user).orElse(null);
    }

}
