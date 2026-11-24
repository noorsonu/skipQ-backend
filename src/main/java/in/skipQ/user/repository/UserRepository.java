package in.skipQ.user.repository;

import in.skipQ.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByPhone(String phone);

    Optional<User> findByEmail(String email);

    boolean existsByPhone(String phone);

    boolean existsByEmail(String email);

    long countByRole(in.skipQ.user.model.UserRole role);

    long countByDeletedFalse();

    long countByBlockedTrue();
}
