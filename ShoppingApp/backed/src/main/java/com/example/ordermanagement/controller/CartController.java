package com.example.ordermanagement.controller;

import com.example.ordermanagement.model.Cart;
import com.example.ordermanagement.service.CartService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam String username) {
        return ResponseEntity.ok(cartService.getCart(username));
    }

    @PostMapping("/add-scenic")
    public ResponseEntity<Cart> addScenic(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        Long scenicId = Long.valueOf(body.get("scenicId").toString());
        Integer quantity = body.containsKey("quantity")
                ? Integer.valueOf(body.get("quantity").toString()) : 1;
        return ResponseEntity.ok(cartService.addScenicToCart(username, scenicId, quantity));
    }

    @PostMapping("/add-route")
    public ResponseEntity<Cart> addRouteTickets(@RequestBody Map<String, Object> body) {
        String username = (String) body.get("username");
        Long routeId = Long.valueOf(body.get("routeId").toString());
        return ResponseEntity.ok(cartService.addRouteTicketsToCart(username, routeId));
    }

    @DeleteMapping("/item/{itemId}")
    public ResponseEntity<Cart> removeItem(
            @RequestParam String username,
            @PathVariable Long itemId) {
        return ResponseEntity.ok(cartService.removeCartItem(username, itemId));
    }

    @PutMapping("/item/{itemId}")
    public ResponseEntity<Cart> updateQuantity(
            @RequestParam String username,
            @PathVariable Long itemId,
            @RequestBody Map<String, Object> body) {
        Integer quantity = Integer.valueOf(body.get("quantity").toString());
        return ResponseEntity.ok(cartService.updateItemQuantity(username, itemId, quantity));
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(@RequestParam String username) {
        cartService.clearCart(username);
        return ResponseEntity.noContent().build();
    }
}
