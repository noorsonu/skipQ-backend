package in.skipQ.zone.service;

import in.skipQ.common.exception.ResourceNotFoundException;
import in.skipQ.zone.dto.CreateZoneRequest;
import in.skipQ.zone.dto.ZoneResponseDto;
import in.skipQ.zone.mapper.ZoneMapper;
import in.skipQ.zone.model.Zone;
import in.skipQ.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ZoneServiceImpl implements ZoneService {

    private final ZoneRepository zoneRepository;
    private final ZoneMapper zoneMapper;

    @Override
    @Transactional(readOnly = true)
    public List<ZoneResponseDto> getAllActiveZones() {
        log.debug("Fetching all active zones");
        return zoneRepository.findByActiveTrue()
                .stream()
                .map(zoneMapper::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ZoneResponseDto getZoneById(UUID id) {
        log.debug("Fetching zone with id: {}", id);
        Zone zone = zoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", id));
        return zoneMapper.toDto(zone);
    }

    @Override
    @Transactional
    public ZoneResponseDto createZone(CreateZoneRequest request) {
        log.info("Creating new zone: {} in {}", request.getName(), request.getCity());
        if (zoneRepository.existsByNameIgnoreCaseAndCityIgnoreCase(request.getName().trim(), request.getCity().trim())) {
            throw new in.skipQ.common.exception.DuplicateResourceException("Zone", "name and city", request.getName() + " in " + request.getCity());
        }
        Zone zone = zoneMapper.toEntity(request);
        Zone savedZone = zoneRepository.save(zone);
        log.info("Zone created successfully with id: {}", savedZone.getId());
        return zoneMapper.toDto(savedZone);
    }
}

