package in.skipQ.ads.service;

import in.skipQ.ads.dto.BookFeaturedSlotRequest;
import in.skipQ.ads.dto.FeaturedSlotDto;
import in.skipQ.ads.dto.SlotAvailabilityDto;
import in.skipQ.user.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface FeaturedSlotService {

    FeaturedSlotDto bookFeaturedSlot(BookFeaturedSlotRequest request, User currentUser);

    SlotAvailabilityDto getSlotAvailability(UUID zoneId, UUID categoryId, LocalDate date);

    List<FeaturedSlotDto> getShopBookedSlots(UUID shopId, User currentUser);

    List<FeaturedSlotDto> listAllSlots(LocalDate start, LocalDate end, User admin);

    void recordClick(UUID slotId);

    void recordImpressions(Collection<UUID> shopIds);

    Set<UUID> getActiveFeaturedShopIds(UUID zoneId, UUID categoryId);
}

