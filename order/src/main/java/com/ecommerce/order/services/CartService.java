package com.ecommerce.order.services;

import com.ecommerce.order.clients.ProductServiceClient;
import com.ecommerce.order.clients.UserServiceClient;
import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.dto.ProductResponse;
import com.ecommerce.order.dto.UserResponse;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.repositories.CartItemRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final UserServiceClient userServiceClient;

//    @CircuitBreaker(name = "productService", fallbackMethod = "addToCartFallBack")
    @Retry(name = "retryBreaker", fallbackMethod = "addToCartFallBack")
    public boolean addToCArt(String userId, CartItemRequest request) {
//        Look for product and confirm quantity
        ProductResponse productResponse = productServiceClient.getProductDetails(Long.valueOf(request.getProductId()));
        if(productResponse == null || productResponse.getStockQuantity() < request.getQuantity()) {
            return false;
        }

//        // Verify user
        UserResponse userResponse = userServiceClient.getUserDetails(userId);
        if(userResponse == null) {
            return false;
        }

        // Add to CartItem
        CartItem existingCartItem = cartItemRepository.findByUserIdAndProductId(userId, request.getProductId());
        if (existingCartItem != null) {
            // Update cart quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + request.getQuantity());
//            existingCartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(existingCartItem.getQuantity())));
            existingCartItem.setPrice(BigDecimal.valueOf(1000));

            cartItemRepository.save(existingCartItem);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setProductId(request.getProductId());
            cartItem.setUserId(userId);
            cartItem.setQuantity(request.getQuantity());
//            cartItem.setPrice(product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity())));
            cartItem.setPrice(BigDecimal.valueOf(1000));

            cartItemRepository.save(cartItem);
        }

        return true;
    }

    public boolean addToCartFallBack(String userId,
                                     CartItemRequest request,
                                     Exception exception) {
        return false;
    }

    public boolean deleteItemFromCart(String userId, String productId) {
        // Look for product
//        Optional<Product> productOpt = productRepository.findById(productId);
//        // Verify user
//        Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId);

//        if(productOpt.isPresent() && userOpt.isPresent()) {
//            cartItemRepository.deleteByUserAndProduct(userOpt.get(), productOpt.get());
//            return true;
//        }

        if(cartItem != null) {
            cartItemRepository.delete(cartItem);
            return true;
        }

        return false;
    }

    public List<CartItem> getCart(String userId) {
//        return userRepository.findById(Long.valueOf(userId))
//                .map(cartItemRepository::findByUser)
//                .orElseGet(List::of);

        return cartItemRepository.findByUserId(userId);
    }

    public void clearCart(String userId) {
//        userRepository.findById(Long.valueOf(userId))
//                .ifPresent(cartItemRepository::deleteByUser);

        cartItemRepository.deleteByUserId(userId);
    }
}
