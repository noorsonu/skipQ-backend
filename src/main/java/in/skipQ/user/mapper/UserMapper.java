package in.skipQ.user.mapper;

import in.skipQ.user.dto.UserProfileResponseDto;
import in.skipQ.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "preferredZoneId", source = "preferredZone.id")
    @Mapping(target = "preferredZoneName", source = "preferredZone.name")
    UserProfileResponseDto toProfileDto(User user);
}

