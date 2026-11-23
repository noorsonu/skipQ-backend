package in.skipQ.zone.mapper;

import in.skipQ.zone.dto.CreateZoneRequest;
import in.skipQ.zone.dto.ZoneResponseDto;
import in.skipQ.zone.model.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    ZoneResponseDto toDto(Zone entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Zone toEntity(CreateZoneRequest request);
}
