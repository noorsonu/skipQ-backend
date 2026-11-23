package in.skipQ.user.service;

import in.skipQ.common.exception.BadRequestException;
import in.skipQ.common.exception.ResourceNotFoundException;
import in.skipQ.notification.repository.DeviceRepository;
import in.skipQ.queue.model.Token;
import in.skipQ.queue.model.TokenStatus;
import in.skipQ.queue.repository.TokenRepository;
import in.skipQ.user.dto.DeleteAccountRequest;
import in.skipQ.user.dto.UpdateUserProfileRequest;
import in.skipQ.user.dto.UserProfileResponseDto;
import in.skipQ.user.mapper.UserMapper;
import in.skipQ.user.model.User;
import in.skipQ.user.repository.UserRepository;
import in.skipQ.zone.model.Zone;
import in.skipQ.zone.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ZoneRepository zoneRepository;
    private final DeviceRepository deviceRepository;
    private final TokenRepository tokenRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponseDto getProfile(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("This account has been deleted.");
        }

        return userMapper.toProfileDto(user);
    }

    @Override
    @Transactional
    public UserProfileResponseDto updateProfile(User currentUser, UpdateUserProfileRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("Cannot update a deleted account.");
        }

        if (request.getName() != null) {
            user.setName(request.getName().trim());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getLanguage() != null) {
            user.setLanguage(request.getLanguage());
        }

        if (request.getPreferredZoneId() != null) {
            Zone zone = zoneRepository.findById(request.getPreferredZoneId())
                    .orElseThrow(() -> new ResourceNotFoundException("Zone", "id", request.getPreferredZoneId()));
            user.setPreferredZone(zone);
        }

        User updatedUser = userRepository.save(user);
        log.info("Profile updated for user: {}", user.getId());

        return userMapper.toProfileDto(updatedUser);
    }

    @Override
    @Transactional
    public void deleteAccount(User currentUser, DeleteAccountRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", currentUser.getId()));

        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new BadRequestException("Account is already deleted.");
        }

        log.info("Processing DPDP Right to Erasure for user: {}. Reason: {}", user.getId(), request.getReason());

        // 1. Cancel active queue tokens
        List<Token> activeTokens = tokenRepository.findByUserIdAndStatusInOrderByJoinedAtDesc(
                user.getId(),
                List.of(TokenStatus.WAITING, TokenStatus.CALLED)
        );
        for (Token token : activeTokens) {
            token.setStatus(TokenStatus.CANCELLED);
            tokenRepository.save(token);
            log.info("Active token {} cancelled due to account deletion", token.getId());
        }

        // 2. Delete device push tokens
        deviceRepository.deleteByUserId(user.getId());

        // 3. Anonymize PII and soft delete under DPDP
        user.setPhone(null);
        user.setName("Deleted User");
        user.setEmail(null);
        user.setPreferredZone(null);
        user.setDeleted(true);
        user.setDeletedAt(Instant.now());

        userRepository.save(user);
        log.info("User {} account erased and anonymized successfully under DPDP Act 2023", user.getId());
    }
}
