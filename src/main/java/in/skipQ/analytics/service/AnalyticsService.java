package in.skipQ.analytics.service;

import in.skipQ.analytics.dto.AdminFunnelReportDto;
import in.skipQ.analytics.dto.AdminZoneReportDto;
import in.skipQ.analytics.dto.RecordEventRequest;
import in.skipQ.analytics.dto.ShopPeriodReportDto;
import in.skipQ.user.model.User;

import java.io.PrintWriter;
import java.time.Instant;
import java.util.UUID;

public interface AnalyticsService {

    void recordEvent(RecordEventRequest request, User currentUser);

    ShopPeriodReportDto getShopPeriodReport(UUID shopId, int days, User merchant);

    AdminFunnelReportDto getFunnelReport(UUID zoneId, Instant start, Instant end, User admin);

    AdminZoneReportDto getZoneReport(UUID zoneId, User admin);

    void exportTokensCsv(UUID shopId, UUID zoneId, Instant start, Instant end, User user, PrintWriter writer);

    void exportShopsCsv(UUID zoneId, User admin, PrintWriter writer);
}

