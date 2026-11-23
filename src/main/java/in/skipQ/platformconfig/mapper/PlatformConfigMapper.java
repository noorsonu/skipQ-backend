package in.skipQ.platformconfig.mapper;

import in.skipQ.platformconfig.dto.FeatureFlagDto;
import in.skipQ.platformconfig.dto.PlatformConfigDto;
import in.skipQ.platformconfig.model.FeatureFlag;
import in.skipQ.platformconfig.model.PlatformConfig;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface PlatformConfigMapper {

    PlatformConfigDto toDto(PlatformConfig config);

    List<PlatformConfigDto> toConfigDtoList(List<PlatformConfig> configs);

    FeatureFlagDto toDto(FeatureFlag flag);

    List<FeatureFlagDto> toFlagDtoList(List<FeatureFlag> flags);
}

