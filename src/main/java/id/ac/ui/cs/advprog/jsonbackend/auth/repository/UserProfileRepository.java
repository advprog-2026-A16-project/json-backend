package id.ac.ui.cs.advprog.jsonbackend.auth.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID> {

    Optional<UserProfile> findByUserId(UUID userId);
    boolean existsByUsername(String username);
    @Query("SELECT p FROM UserProfile p JOIN FETCH p.user u WHERE u.accountStatus = :status")
    List<UserProfile> findAllByUserAccountStatus(@Param("status") AccountStatus status);
}