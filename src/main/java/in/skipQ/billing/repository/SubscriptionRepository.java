package in.skipQ.billing.repository;

import in.skipQ.billing.model.PlanCode;
import in.skipQ.billing.model.Subscription;
import in.skipQ.billing.model.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByShopId(UUID shopId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    List<Subscription> findByPlanCode(PlanCode planCode);

    boolean existsByShopId(UUID shopId);
}

