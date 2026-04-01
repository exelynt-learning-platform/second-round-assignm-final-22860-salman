package com.shopwavefusion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shopwavefusion.exception.UserException;
import com.shopwavefusion.modal.Cart;
import com.shopwavefusion.modal.User;
import com.shopwavefusion.repository.CartRepository;
import com.shopwavefusion.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CartService cartService;

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private UserServiceImplementation userService;

    private User testUser;

    @BeforeEach
    public void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword123");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setMobile("1234567890");
        testUser.setRole("ROLE_USER");
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testFindUserById_Success() throws UserException {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        User result = userService.findUserById(1L);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindUserById_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userService.findUserById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    public void testFindUserByEmail_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);

        User result = userService.findUserByEmail("test@example.com");

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("test@example.com");
    }

    @Test
    public void testFindUserByEmail_NotFound() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(null);

        assertThrows(UserException.class, () -> userService.findUserByEmail("nonexistent@example.com"));
        verify(userRepository, times(1)).findByEmail("nonexistent@example.com");
    }

    @Test
    public void testUpdateUser_Success() throws UserException {
        User updatedUser = new User();
        updatedUser.setFirstName("Jane");
        updatedUser.setLastName("Smith");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void testUpdateUser_UserNotFound() {
        User updatedUser = new User();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(UserException.class, () -> userService.updateUser(999L, updatedUser));
    }

    @Test
    public void testValidateUserPassword_ValidPassword() throws UserException {
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";
        testUser.setPassword(encodedPassword);

        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean isValid = userService.validateUserPassword("test@example.com", rawPassword);

        assertTrue(isValid);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testValidateUserPassword_InvalidPassword() throws UserException {
        String rawPassword = "wrongPassword";
        String encodedPassword = "encodedPassword123";
        testUser.setPassword(encodedPassword);

        when(userRepository.findByEmail("test@example.com")).thenReturn(testUser);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(false);

        boolean isValid = userService.validateUserPassword("test@example.com", rawPassword);

        assertFalse(isValid);
        verify(passwordEncoder, times(1)).matches(rawPassword, encodedPassword);
    }

    @Test
    public void testCreateUser_Success() {
        User newUser = new User();
        newUser.setEmail("newuser@example.com");
        newUser.setPassword("rawPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setMobile("9876543210");

        when(passwordEncoder.encode("rawPassword")).thenReturn("encodedNewPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(cartRepository.save(any(Cart.class))).thenReturn(new Cart());

        User result = userService.createUser(newUser);

        assertNotNull(result);
        assertEquals("newuser@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
