package in.skipQ.ads.mapper;

import in.skipQ.ads.dto.FeaturedSlotDto;
import in.skipQ.ads.model.FeaturedSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FeaturedSlotMapper {

    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneName", source = "zone.name")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "shopId", source = "shop.id")
    @Mapping(target = "shopName", source = "shop.name")
    FeaturedSlotDto toDto(FeaturedSlot slot);

    List<FeaturedSlotDto> toDtoList(List<FeaturedSlot> slots);
}

