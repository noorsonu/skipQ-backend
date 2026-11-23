package in.skipQ.dashboard.service;

import in.skipQ.dashboard.dto.DailyShopSummaryDto;
import in.skipQ.dashboard.dto.MerchantShopDashboardDto;
import in.skipQ.user.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShopDashboardService {

    List<MerchantShopDashboardDto> getMyShops(User merchant);

    DailyShopSummaryDto getDailySummary(UUID shopId, LocalDate date, User merchant);
}

