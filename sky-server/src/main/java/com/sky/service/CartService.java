package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface CartService {

    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);
    void delShoppingCart(ShoppingCartDTO shoppingCartDTO);
    List<ShoppingCart> queryShoppingCart();
    void cleanShoppingCart();
}
