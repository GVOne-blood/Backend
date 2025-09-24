package com.spring_food.springfood.service.Impl;

import com.spring_food.springfood.dto.response.CartDetailResponse;
import com.spring_food.springfood.exception.custom.InvalidDataException;
import com.spring_food.springfood.model.Cart;
import com.spring_food.springfood.model.CartItem;
import com.spring_food.springfood.model.Product;
import com.spring_food.springfood.repository.CartItemRepository;
import com.spring_food.springfood.repository.CartRepository;
import com.spring_food.springfood.repository.ProductRepository;
import com.spring_food.springfood.service.CartService;
import com.spring_food.springfood.service.ProductService;
import com.spring_food.springfood.service.UserService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CartServiceImpl implements CartService {

    UserService userService;
    CartRepository cartRepository;
    ProductRepository productRepository;

    CartItemRepository cartItemRepository;
    ProductService productService;

    @Override
    public Page<CartDetailResponse> getCartDetail(Pageable pageable, String userId) {

//        User user = userService.findById(userId);
//
//        Cart cart = user.getCart();
//        System.out.println(cart);

        return cartRepository.findCartItemByUserId(pageable, userId);
    }

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    @Override
    public void addToCart(String userId, String productId, int quantity) {

        Optional<Cart> cart = cartRepository.findByUserId(userId);
        Optional<Product> product = productRepository.findById(productId);

        if (product.isEmpty()) throw new InvalidDataException("Product not found");

        if (product.get().getQuantity() < quantity) throw new InvalidDataException("Quantity is over than stock");

        List<CartItem> cartItems = cart.get().getCartItems();
        CartItem cartItem = new CartItem();
        cartItem.setCart(cart.get());
        cartItem.setQuantity(quantity);
        cartItem.setProduct(product.get());
        cartItems.add(cartItem);

        // save
    }

    @Transactional
    @Override
    public void deleteProductInCart(List<String> listProductId, String userId) {

        Optional<Cart> cart = cartRepository.findByUserId(userId);
        if (cart.isEmpty()) throw new InvalidDataException("Cart is empty");

        List<CartItem> cartItems = cartItemRepository.findAllByUserId(userId);

        for (String productId : listProductId) {
            if (!productService.isProductExists(productId)) throw new InvalidDataException("Product not found");
        }
        cartItemRepository.deleteAllByCartIdAndProductIds(cart.get().getId(), listProductId);
    }

    @Override
    public void clearCartByUserId(String userId) {

        cartItemRepository.deleteAllItemsByUserId(userId);
    }


}
