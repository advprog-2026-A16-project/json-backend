package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerWebMvcTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AdminProductController controller = new AdminProductController(productService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void updateProductForAdminReturnsBadRequestWhenPayloadInvalid() throws Exception {
        authenticateAsAdmin();
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/admin/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "",
                                  "description": "",
                                  "price": 0,
                                  "stock": -1,
                                  "originCountry": "",
                                  "purchaseDate": null,
                                  "jastiperId": null
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());

        verify(productService, never()).update(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getAllProductsForAdminReturnsForbiddenWhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/products"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin access required"));

        verify(productService, never()).findAll();
    }

    @Test
    void deleteProductForAdminReturnsForbiddenWhenUnauthenticated() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/admin/products/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Admin access required"));

        verify(productService, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private void authenticateAsAdmin() {
        User user = new User("admin@test.com", "secret", Role.ADMIN);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
