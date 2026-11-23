package in.skipQ.shop.service;

import in.skipQ.shop.dto.CreateShopRequest;
import in.skipQ.shop.dto.ShopDetailDto;
import in.skipQ.shop.dto.ShopSummaryDto;

import java.util.List;
import java.util.UUID;

public interface ShopService {

    List<ShopSummaryDto> discoverShops(UUID zoneId, UUID categoryId, Boolean openOnly, String q);

    List<ShopSummaryDto> discoverShops(UUID zoneId, UUID categoryId, Boolean openOnly, String q,
                                       Double lat, Double lng, Double radiusKm, String sortBy);

    ShopDetailDto getShopById(UUID id);

    ShopDetailDto getShopBySlug(String slug);

    ShopDetailDto createShop(CreateShopRequest request);

    List<ShopSummaryDto> getShopsByPhone(String phone);
}
