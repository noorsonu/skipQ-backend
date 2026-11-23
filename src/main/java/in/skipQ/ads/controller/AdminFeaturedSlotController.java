package in.skipQ.ads.controller;

import in.skipQ.ads.dto.FeaturedSlotDto;
import in.skipQ.ads.service.FeaturedSlotService;
import in.skipQ.common.dto.ApiResponse;
import in.skipQ.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/featured-slots")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "M10 Featured Listings - Admin", description = "Admin monitoring of sponsored slots and performance rollups (BRD Section 21)")
public class AdminFeaturedSlotController {

    private final FeaturedSlotService featuredSlotService;

    @GetMapping
    @Operation(summary = "Admin: List featured slots by date range", description = "Returns all sponsored slots across platform within the requested date window")
    public ResponseEntity<ApiResponse<List<FeaturedSlotDto>>> listAllSlots(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal User admin) {
        List<FeaturedSlotDto> slots = featuredSlotService.listAllSlots(startDate, endDate, admin);
        return ResponseEntity.ok(ApiResponse.ok(slots, "Featured slots fetched successfully"));
    }
}

