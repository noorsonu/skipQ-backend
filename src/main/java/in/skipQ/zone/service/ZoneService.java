package in.skipQ.zone.service;

import in.skipQ.zone.dto.CreateZoneRequest;
import in.skipQ.zone.dto.ZoneResponseDto;

import java.util.List;
import java.util.UUID;

public interface ZoneService {

    List<ZoneResponseDto> getAllActiveZones();

    ZoneResponseDto getZoneById(UUID id);

    ZoneResponseDto createZone(CreateZoneRequest request);
}

