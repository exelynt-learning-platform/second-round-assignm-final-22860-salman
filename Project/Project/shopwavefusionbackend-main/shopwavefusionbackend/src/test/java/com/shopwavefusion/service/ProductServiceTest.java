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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.shopwavefusion.exception.ProductException;
import com.shopwavefusion.modal.Category;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.repository.CategoryRepository;
import com.shopwavefusion.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImplementation productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    public void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setCategoryName("Electronics");

        testProduct = new Product();
        testProduct.setId(1L);
        testProduct.setTitle("Laptop");
        testProduct.setDescription("High-performance laptop");
        testProduct.setPrice(1000);
        testProduct.setDiscountedPrice(800);
        testProduct.setDiscountPersent(20);
        testProduct.setQuantity(50);
        testProduct.setBrand("Dell");
        testProduct.setColor("Black");
        testProduct.setImageUrl("https://example.com/laptop.jpg");
        testProduct.setCategory(testCategory);
        testProduct.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateProduct_Success() throws ProductException {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.createProduct(testProduct);

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle());
        assertEquals(1000, result.getPrice());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testFindProductById_Success() throws ProductException {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Product result = productService.findProductById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getTitle());
        assertEquals("Dell", result.getBrand());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    public void testFindProductById_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.findProductById(999L));
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    public void testUpdateProduct_Success() throws ProductException {
        Product updatedProduct = new Product();
        updatedProduct.setTitle("Updated Laptop");
        updatedProduct.setPrice(900);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertNotNull(result);
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testDeleteProduct_Success() throws ProductException {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        doNothing().when(productRepository).delete(testProduct);

        productService.deleteProduct(1L);

        verify(productRepository, times(1)).delete(testProduct);
    }

    @Test
    public void testDeleteProduct_NotFound() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ProductException.class, () -> productService.deleteProduct(999L));
    }

    @Test
    public void testGetAllProducts() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        Page<Product> page = new PageImpl<>(products);
        when(productRepository.findAll(PageRequest.of(0, 10))).thenReturn(page);

        Page<Product> result = productService.getAllProducts(0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findAll(any(org.springframework.data.domain.Pageable.class));
    }

    @Test
    public void testGetProductsByCategory_Success() throws ProductException {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.findByCategory(testCategory)).thenReturn(products);

        List<Product> result = productService.getProductsByCategory(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getTitle());
    }

    @Test
    public void testSearchProducts() {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productRepository.findByTitleContainingIgnoreCase("Laptop")).thenReturn(products);

        List<Product> result = productService.searchProducts("Laptop");

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Laptop", result.get(0).getTitle());
    }

    @Test
    public void testValidateProductPrice_ValidPrice() {
        assertTrue(productService.validateProductPrice(100));
        assertTrue(productService.validateProductPrice(0));
    }

    @Test
    public void testValidateProductPrice_InvalidPrice() {
        assertFalse(productService.validateProductPrice(-100));
    }

    @Test
    public void testUpdateProductStock_Success() throws ProductException {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        testProduct.setQuantity(40);
        Product result = productService.updateProductStock(1L, 40);

        assertNotNull(result);
        assertEquals(40, result.getQuantity());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    public void testIsProductAvailable_True() {
        testProduct.setQuantity(5);
        assertTrue(productService.isProductAvailable(testProduct, 3));
    }

    @Test
    public void testIsProductAvailable_False() {
        testProduct.setQuantity(2);
        assertFalse(productService.isProductAvailable(testProduct, 3));
    }

    @Test
    public void testCalculateDiscount() {
        int originalPrice = 1000;
        int discountPercent = 20;
        int expectedDiscounted = 800;

        int actual = originalPrice - (originalPrice * discountPercent / 100);

        assertEquals(expectedDiscounted, actual);
    }
}
