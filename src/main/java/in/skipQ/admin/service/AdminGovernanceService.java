package in.skipQ.admin.service;

import in.skipQ.admin.dto.AdminShopModerationDto;
import in.skipQ.admin.dto.AdminUserModerationDto;
import in.skipQ.admin.dto.ForceShopStatusRequest;
import in.skipQ.admin.dto.PlatformMetricsDto;
import in.skipQ.user.model.User;

import java.util.UUID;

public interface AdminGovernanceService {

    PlatformMetricsDto getPlatformMetrics(User admin);

    AdminShopModerationDto toggleShopActive(UUID shopId, User admin);

    AdminShopModerationDto forceShopStatus(UUID shopId, ForceShopStatusRequest request, User admin);

    AdminUserModerationDto blockUser(UUID userId, User admin);

    AdminUserModerationDto unblockUser(UUID userId, User admin);

    boolean toggleZoneActive(UUID zoneId, User admin);

    boolean toggleCategoryActive(UUID categoryId, User admin);

    void purgeAllData();
}

