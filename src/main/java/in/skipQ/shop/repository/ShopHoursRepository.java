package in.skipQ.shop.repository;

import in.skipQ.shop.model.ShopHours;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShopHoursRepository extends JpaRepository<ShopHours, UUID> {

    List<ShopHours> findByShopIdOrderByWeekdayAsc(UUID shopId);
}

