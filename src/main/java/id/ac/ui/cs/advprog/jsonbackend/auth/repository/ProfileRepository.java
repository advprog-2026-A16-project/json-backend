package id.ac.ui.cs.advprog.jsonbackend.auth.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProfileRepository extends JpaRepository<Profile, UUID> {

    Optional<Profile> findByUserId(UUID userId);
    boolean existsByUsername(String username);
    @Query("SELECT p FROM Profile p JOIN FETCH p.user u WHERE u.accountStatus = :status")
    List<Profile> findAllByUserAccountStatus(@Param("status") AccountStatus status);
}