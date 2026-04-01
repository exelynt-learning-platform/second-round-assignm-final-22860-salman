package com.shopwavefusion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopwavefusion.exception.CartItemException;
import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.CartItem;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.CartItemRepository;
import com.shopwavefusion.repository.CartRepository;
import com.shopwavefusion.repository.ProductRepository;
import com.shopwavefusion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CartServiceImplementation cartService;

    @InjectMocks
    private CartItemServiceImplementation cartItemService;

    private User testUser;
    private Cart testCart;
    private Product testProduct;
    private CartItem testCartItem;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");

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
        testCartItem.setDiscountedPrice(1600);
    }

    @Test
    public void testCreateCart_Success() throws UserException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.createCart(testUser);

        assertNotNull(result);
        assertEquals(testUser.getId(), result.getUser().getId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testFindUserCart_Success() throws UserException {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        Cart result = cartService.findUserCart(1L);

        assertNotNull(result);
        assertEquals(1L, result.getUser().getId());
        verify(cartRepository, times(1)).findByUserId(1L);
    }

    @Test
    public void testFindUserCart_NotFound() {
        when(cartRepository.findByUserId(999L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> cartService.findUserCart(999L));
    }

    @Test
    public void testAddItemToCart_Success() throws UserException, ProductException, CartItemException {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        CartItem result = cartItemService.addCartItem(1L, 1L, 2);

        assertNotNull(result);
        assertEquals(2, result.getQuantity());
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    public void testAddItemToCart_InsufficientStock() throws UserException, ProductException {
        testProduct.setQuantity(1);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        assertThrows(CartItemException.class, () -> cartItemService.addCartItem(1L, 1L, 5));
    }

    @Test
    public void testRemoveCartItem_Success() throws CartItemException {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        doNothing().when(cartItemRepository).delete(testCartItem);

        cartItemService.removeCartItem(1L);

        verify(cartItemRepository, times(1)).delete(testCartItem);
    }

    @Test
    public void testRemoveCartItem_NotFound() {
        when(cartItemRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(CartItemException.class, () -> cartItemService.removeCartItem(999L));
    }

    @Test
    public void testUpdateCartItem_Success() throws CartItemException {
        when(cartItemRepository.findById(1L)).thenReturn(Optional.of(testCartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(testCartItem);

        CartItem result = cartItemService.updateCartItem(1L, 5);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).save(any(CartItem.class));
    }

    @Test
    public void testGetCartTotal_Success() throws UserException {
        testCart.setCartItems(List.of(testCartItem));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        long total = cartService.getCartTotalPrice(1L);

        assertTrue(total > 0);
    }

    @Test
    public void testClearCart_Success() throws UserException {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(testCart);

        Cart result = cartService.clearCart(1L);

        assertNotNull(result);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    public void testCalculateCartTotal_WithMultipleItems() {
        List<CartItem> items = new ArrayList<>();

        CartItem item1 = new CartItem();
        item1.setPrice(1000);
        item1.setQuantity(2);

        CartItem item2 = new CartItem();
        item2.setPrice(500);
        item2.setQuantity(1);

        items.add(item1);
        items.add(item2);

        long total = items.stream().mapToLong(item -> item.getPrice() * item.getQuantity()).sum();

        assertEquals(2500, total);
    }

    @Test
    public void testValidateQuantity_Valid() {
        testProduct.setQuantity(10);
        assertTrue(cartItemService.isValidQuantity(testProduct, 5));
    }

    @Test
    public void testValidateQuantity_Invalid() {
        testProduct.setQuantity(2);
        assertFalse(cartItemService.isValidQuantity(testProduct, 5));
    }

    @Test
    public void testGetCartItemCount_Success() throws UserException {
        testCart.setCartItems(List.of(testCartItem));
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        int count = cartService.getCartItemCount(1L);

        assertEquals(1, count);
    }

    @Test
    public void testGetCartItemCount_Empty() throws UserException {
        testCart.setCartItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(testCart));

        int count = cartService.getCartItemCount(1L);

        assertEquals(0, count);
    }
}
