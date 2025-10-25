package com.ecommerce.order.controllers;

import com.ecommerce.order.dto.CartItemRequest;
import com.ecommerce.order.models.CartItem;
import com.ecommerce.order.services.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @PostMapping
    public ResponseEntity<String> addToCart(
            @RequestHeader("X-User-ID") String userId,
            @RequestBody CartItemRequest request
    ) {
        boolean status = cartService.addToCArt(userId, request);
        if (!status) {
            return new ResponseEntity<>("Product Out of Stock or User not found or Product not found.", HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>("Product successfully added to cart.", HttpStatus.CREATED);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromCart(
            @RequestHeader("X-User-ID") String userId,
            @PathVariable String productId
    ) {
        boolean deleted =  cartService.deleteItemFromCart(userId, productId);

        return deleted
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(
            @RequestHeader("X-User-ID") String userId
    ) {
        List<CartItem> cartItem = cartService.getCart(userId);

        return new ResponseEntity<>(cartItem, HttpStatus.OK);
    }
}
