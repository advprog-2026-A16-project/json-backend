package id.ac.ui.cs.advprog.jsonbackend.inventory.repository;

import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByJastiperId(UUID jastiperId);

    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String nameKeyword,
                                                                                   String descriptionKeyword);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") UUID id);
}
