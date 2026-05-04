package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InventoryExceptionHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ForbiddenInventoryAccessException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
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
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerWebMvcTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler(), new InventoryExceptionHandler())
                .build();
    }

    @Test
    void reserveStockReturnsBadRequestWhenQuantityNonPositive() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantity must be greater than zero"));

        verifyNoInteractions(productService);
    }

    @Test
    void getAllProductsReturnsBadRequestWhenPageNegative() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                        .queryParam("page", "-1"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void getAllProductsReturnsBadRequestWhenSizeNonPositive() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/products")
                        .queryParam("size", "0"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(productService);
    }

    @Test
    void reserveStockReturnsConflictWhenInsufficientStock() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(productService)
                .reserveStock(id, 5);

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    void reserveStockReturnsNotFoundWhenProductMissing() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException("Product not found with id: " + id))
                .when(productService)
                .reserveStock(id, 1);

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: " + id));
    }

    @Test
    void reserveStockReturnsNoContentWhenSuccess() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "1"))
                .andExpect(status().isNoContent());

        verify(productService, times(1)).reserveStock(id, 1);
    }

    @Test
    void createProductReturnsForbiddenWhenNoAuthenticatedUser() throws Exception {
                mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProductReturnsForbiddenWhenNoAuthenticatedUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteProductReturnsForbiddenWhenNoAuthenticatedUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isForbidden());
    }

    @Test
    void createProductPassesAuthenticatedActorToService() throws Exception {
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("jastiper@test.com").role(Role.JASTIPER).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isCreated());

        verify(productService, times(1)).createAsJastiper(any(), eq(actorId), eq(Role.JASTIPER));
    }

    @Test
    void updateProductReturnsForbiddenWhenServiceRejectsOwnership() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("jastiper@test.com").role(Role.JASTIPER).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
        doThrow(new ForbiddenInventoryAccessException("Jastiper can only update own product"))
                .when(productService).updateAsJastiper(eq(id), any(), eq(actorId), eq(Role.JASTIPER));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Jastiper can only update own product"));
    }

    @Test
    void deleteProductReturnsForbiddenWhenServiceRejectsOwnership() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("jastiper@test.com").role(Role.JASTIPER).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
        doThrow(new ForbiddenInventoryAccessException("Jastiper can only delete own product"))
                .when(productService).deleteAsJastiper(id, actorId, Role.JASTIPER);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Jastiper can only delete own product"));
    }

    @Test
    void createProductReturnsForbiddenWhenActorIsTitipers() throws Exception {
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("titipers@test.com").role(Role.TITIPERS).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
        doThrow(new ForbiddenInventoryAccessException("Only jastiper can manage own products"))
                .when(productService).createAsJastiper(any(), eq(actorId), eq(Role.TITIPERS));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only jastiper can manage own products"));
    }

    @Test
    void updateProductReturnsForbiddenWhenActorIsTitipers() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("titipers@test.com").role(Role.TITIPERS).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
        doThrow(new ForbiddenInventoryAccessException("Only jastiper can manage own products"))
                .when(productService).updateAsJastiper(eq(id), any(), eq(actorId), eq(Role.TITIPERS));

        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"n",
                                  "description":"d",
                                  "price":10,
                                  "stock":1,
                                  "originCountry":"ID",
                                  "purchaseDate":"2026-04-01",
                                  "jastiperId":"11111111-1111-4111-8111-111111111111"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only jastiper can manage own products"));
    }

    @Test
    void deleteProductReturnsForbiddenWhenActorIsTitipers() throws Exception {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        User actor = User.builder().id(actorId).email("titipers@test.com").role(Role.TITIPERS).password("x").build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
        doThrow(new ForbiddenInventoryAccessException("Only jastiper can manage own products"))
                .when(productService).deleteAsJastiper(id, actorId, Role.TITIPERS);

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only jastiper can manage own products"));
    }
}
