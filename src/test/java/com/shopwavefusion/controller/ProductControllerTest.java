package com.shopwavefusion.controller;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopwavefusion.modal.Category;
import com.shopwavefusion.modal.Product;
import com.shopwavefusion.service.ProductService;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProductService productService;

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
        testProduct.setSizes(new HashSet<>());
        testProduct.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testGetAllProducts_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        Page<Product> page = new PageImpl<>(products);
        when(productService.getAllProducts(0, 10)).thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("pageNumber", "0")
                        .param("pageSize", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title", is("Laptop")));
    }

    @Test
    public void testGetProduct_Success() throws Exception {
        when(productService.findProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/products/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Laptop")))
                .andExpect(jsonPath("$.price", is(1000)));
    }

    @Test
    public void testGetProduct_NotFound() throws Exception {
        when(productService.findProductById(999L)).thenThrow(new RuntimeException("Product not found"));

        mockMvc.perform(get("/products/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testSearchProducts_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productService.searchProducts("Laptop")).thenReturn(products);

        mockMvc.perform(get("/products/search")
                        .param("query", "Laptop")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Laptop")));
    }

    @Test
    public void testSearchProducts_EmptyResult() throws Exception {
        List<Product> products = new ArrayList<>();

        when(productService.searchProducts("Nonexistent")).thenReturn(products);

        mockMvc.perform(get("/products/search")
                        .param("query", "Nonexistent")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetProductsByCategory_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productService.getProductsByCategory(1L)).thenReturn(products);

        mockMvc.perform(get("/products/category/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Laptop")));
    }

    @Test
    public void testGetProductsByCategory_Empty() throws Exception {
        List<Product> products = new ArrayList<>();

        when(productService.getProductsByCategory(999L)).thenReturn(products);

        mockMvc.perform(get("/products/category/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetProductsByBrand_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productService.getProductsByBrand("Dell")).thenReturn(products);

        mockMvc.perform(get("/products/brand")
                        .param("brand", "Dell")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].brand", is("Dell")));
    }

    @Test
    public void testGetProductsByColor_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productService.getProductsByColor("Black")).thenReturn(products);

        mockMvc.perform(get("/products/color")
                        .param("color", "Black")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].color", is("Black")));
    }

    @Test
    public void testGetProductsByMinAndMaxPrice_Success() throws Exception {
        List<Product> products = new ArrayList<>();
        products.add(testProduct);

        when(productService.getProductsByPrice(500, 1500)).thenReturn(products);

        mockMvc.perform(get("/products/price")
                        .param("minPrice", "500")
                        .param("maxPrice", "1500")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price", is(1000)));
    }

    @Test
    public void testGetProductsByMinAndMaxPrice_NoResults() throws Exception {
        List<Product> products = new ArrayList<>();

        when(productService.getProductsByPrice(1, 100)).thenReturn(products);

        mockMvc.perform(get("/products/price")
                        .param("minPrice", "1")
                        .param("maxPrice", "100")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void testGetProductStock_Success() throws Exception {
        when(productService.findProductById(1L)).thenReturn(testProduct);

        mockMvc.perform(get("/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(50)));
    }

    @Test
    public void testGetProductStock_OutOfStock() throws Exception {
        Product outOfStockProduct = new Product();
        outOfStockProduct.setId(2L);
        outOfStockProduct.setTitle("Out of Stock Item");
        outOfStockProduct.setQuantity(0);

        when(productService.findProductById(2L)).thenReturn(outOfStockProduct);

        mockMvc.perform(get("/products/2/stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(0)));
    }
}
