package com.example.ordermanagement.service;

import com.example.ordermanagement.model.Cart;
import com.example.ordermanagement.model.CartItem;
import com.example.ordermanagement.model.RouteScenic;
import com.example.ordermanagement.model.Scenic;
import com.example.ordermanagement.repository.CartItemRepository;
import com.example.ordermanagement.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private RouteScenicService routeScenicService;

    @Autowired
    private ScenicService scenicService;

    private Cart getOrCreateCart(String username) {
        Optional<Cart> existing = cartRepository.findByUsername(username);
        if (existing.isPresent()) {
            return existing.get();
        }
        Cart cart = new Cart();
        cart.setUsername(username);
        return cartRepository.save(cart);
    }

    public Cart getCart(String username) {
        return getOrCreateCart(username);
    }

    @Transactional
    public Cart addScenicToCart(String username, Long scenicId, Integer quantity) {
        Cart cart = getOrCreateCart(username);
        Scenic scenic = scenicService.getById(scenicId);
        if (scenic == null) {
            throw new RuntimeException("景区不存在");
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getScenicId().equals(scenicId) && item.getRouteId() == null)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + quantity);
        } else {
            CartItem item = new CartItem();
            item.setCart(cart);
            item.setScenicId(scenicId);
            item.setScenicName(scenic.getName());
            item.setTicketPrice(scenic.getTicketPrice());
            item.setQuantity(quantity);
            cart.getItems().add(item);
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart addRouteTicketsToCart(String username, Long routeId) {
        Cart cart = getOrCreateCart(username);
        List<RouteScenic> routeScenics = routeScenicService.getByRouteId(routeId);

        for (RouteScenic rs : routeScenics) {
            Scenic scenic = rs.getScenic();
            if (scenic == null) continue;

            Optional<CartItem> existingItem = cart.getItems().stream()
                    .filter(item -> item.getScenicId().equals(scenic.getId())
                            && item.getRouteId() != null
                            && item.getRouteId().equals(routeId))
                    .findFirst();

            if (existingItem.isPresent()) {
                existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setScenicId(scenic.getId());
                item.setScenicName(scenic.getName());
                item.setTicketPrice(scenic.getTicketPrice());
                item.setQuantity(1);
                item.setRouteId(routeId);
                cart.getItems().add(item);
            }
        }

        return cartRepository.save(cart);
    }

    @Transactional
    public Cart removeCartItem(String username, Long itemId) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().removeIf(item -> item.getId().equals(itemId));
        return cartRepository.save(cart);
    }

    @Transactional
    public Cart updateItemQuantity(String username, Long itemId, Integer quantity) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
        return cartRepository.save(cart);
    }

    @Transactional
    public void clearCart(String username) {
        Cart cart = getOrCreateCart(username);
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}
