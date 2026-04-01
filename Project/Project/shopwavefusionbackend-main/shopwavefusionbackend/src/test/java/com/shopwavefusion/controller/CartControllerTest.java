package com.shopwavefusion.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.request.AddItemRequest;
import com.shopwavefusion.service.CartItemService;
import com.shopwavefusion.service.CartService;

@SpringBootTest
@AutoConfigureMockMvc
public class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CartService cartService;

    @MockBean
    private CartItemService cartItemService;

    private Cart testCart;
    private User testUser;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setUser(testUser);
        testCart.setCartItems(new ArrayList<>());
        testCart.setTotalPrice(0);

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("Laptop");
        testProduct.setPrice(1000);
        testProduct.setQuantity(10);

        testCartItem = new CartItem();
        testCartItem.setId(1L);
        testCartItem.setCart(testCart);
        testCartItem.setProduct(testProduct);
        testCartItem.setQuantity(2);
        testCartItem.setPrice(2000);
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetUserCart_Success() throws Exception {
        when(cartService.findUserCart(1L)).thenReturn(testCart);

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.user.id", is(1)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetUserCart_NotFound() throws Exception {
        when(cartService.findUserCart(999L)).thenThrow(new RuntimeException("Cart not found"));

        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testAddItemToCart_Success() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        when(cartItemService.addCartItem(1L, 1L, 2)).thenReturn(testCartItem);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity", is(2)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testAddItemToCart_InvalidQuantity() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(0);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testAddItemToCart_InsufficientStock() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(100);

        when(cartItemService.addCartItem(1L, 1L, 100))
                .thenThrow(new RuntimeException("Insufficient stock"));

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testRemoveItemFromCart_Success() throws Exception {
        doNothing().when(cartItemService).removeCartItem(1L);

        mockMvc.perform(delete("/cart_items/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testRemoveItemFromCart_NotFound() throws Exception {
        doThrow(new RuntimeException("Cart item not found"))
                .when(cartItemService).removeCartItem(999L);

        mockMvc.perform(delete("/cart_items/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testUpdateCartItem_Success() throws Exception {
        testCartItem.setQuantity(5);
        when(cartItemService.updateCartItem(1L, 5)).thenReturn(testCartItem);

        mockMvc.perform(put("/cart_items/1")
                        .param("quantity", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(5)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testUpdateCartItem_InvalidQuantity() throws Exception {
        mockMvc.perform(put("/cart_items/1")
                        .param("quantity", "-5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testClearCart_Success() throws Exception {
        when(cartService.clearCart(1L)).thenReturn(testCart);

        mockMvc.perform(delete("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetCartTotal_Success() throws Exception {
        when(cartService.getCartTotalPrice(1L)).thenReturn(2000L);

        mockMvc.perform(get("/cart/total")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPrice", is(2000)));
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetCartItemCount_Success() throws Exception {
        when(cartService.getCartItemCount(1L)).thenReturn(1);

        mockMvc.perform(get("/cart/item-count")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.itemCount", is(1)));
    }

    @Test
    public void testGetUserCart_Unauthorized() throws Exception {
        mockMvc.perform(get("/cart")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void testAddItemToCart_Unauthorized() throws Exception {
        AddItemRequest request = new AddItemRequest();
        request.setProductId(1L);
        request.setQuantity(2);

        mockMvc.perform(post("/cart/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user@example.com", roles = "USER")
    public void testGetCartSummary_Success() throws Exception {
        List<CartItem> items = new ArrayList<>();
        items.add(testCartItem);
        testCart.setCartItems(items);

        when(cartService.findUserCart(1L)).thenReturn(testCart);
        when(cartService.getCartTotalPrice(1L)).thenReturn(2000L);
        when(cartService.getCartItemCount(1L)).thenReturn(1);

        mockMvc.perform(get("/cart/summary")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}
