package in.skipQ.review.service;

import in.skipQ.common.exception.BadRequestException;
import in.skipQ.common.exception.DuplicateResourceException;
import in.skipQ.common.exception.ForbiddenException;
import in.skipQ.common.exception.ResourceNotFoundException;
import in.skipQ.queue.model.Token;
import in.skipQ.queue.model.TokenStatus;
import in.skipQ.queue.repository.TokenRepository;
import in.skipQ.review.dto.*;
import in.skipQ.review.mapper.ReviewMapper;
import in.skipQ.review.model.Review;
import in.skipQ.review.repository.ReviewRepository;
import in.skipQ.shop.model.Shop;
import in.skipQ.shop.repository.ShopRepository;
import in.skipQ.user.model.User;
import in.skipQ.user.model.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final TokenRepository tokenRepository;
    private final ShopRepository shopRepository;
    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public ReviewDto submitReview(CreateReviewRequest request, User currentUser) {
        Token token = null;
        Shop shop = null;

        if (request.getTokenId() != null) {
            token = tokenRepository.findById(request.getTokenId()).orElse(null);
            if (token != null) {
                shop = token.getShop();
            }
        }

        if (shop == null && request.getShopId() != null) {
            shop = shopRepository.findById(request.getShopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", request.getShopId()));
        }

        if (shop == null) {
            throw new BadRequestException("Valid shopId or tokenId is required to submit a review.");
        }

        String reviewerName = "Verified Customer";
        if (currentUser != null && currentUser.getName() != null && !currentUser.getName().isBlank()) {
            reviewerName = currentUser.getName();
        } else if (request.getCustomerName() != null && !request.getCustomerName().isBlank()) {
            reviewerName = request.getCustomerName().trim();
        } else if (token != null && token.getCustomerName() != null && !token.getCustomerName().isBlank()) {
            reviewerName = token.getCustomerName();
        }

        String tagsStr = reviewMapper.tagListToString(request.getTags());

        // FR-1106: If review already exists for this token, allow updating review/comment
        if (token != null) {
            Optional<Review> existingOpt = reviewRepository.findByTokenId(token.getId());
            if (existingOpt.isPresent()) {
                Review existing = existingOpt.get();
                if (request.getStars() != null) {
                    existing.setStars(request.getStars());
                }
                if (request.getText() != null) {
                    existing.setText(request.getText().trim());
                }
                if (request.getTags() != null) {
                    existing.setTags(tagsStr);
                }
                if (!reviewerName.isBlank()) {
                    existing.setCustomerName(reviewerName);
                }
                Review updated = reviewRepository.save(existing);
                log.info("Review updated for shop {} on token {} with {} stars", shop.getId(), token.getId(), existing.getStars());
                return reviewMapper.toDto(updated);
            }
        }

        Review review = Review.builder()
                .shop(shop)
                .token(token)
                .user(currentUser != null ? currentUser : (token != null ? token.getUser() : null))
                .customerName(reviewerName)
                .stars(request.getStars())
                .text(request.getText() != null ? request.getText().trim() : null)
                .tags(tagsStr)
                .verifiedToken(token != null)
                .hidden(false)
                .abuseReported(false)
                .build();

        Review saved = reviewRepository.save(review);
        log.info("Review submitted for shop {}: {} stars by {}", shop.getId(), request.getStars(), reviewerName);
        return reviewMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ShopRatingSummaryDto getShopRatingSummary(UUID shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        List<Review> activeReviews = reviewRepository.findByShopIdAndHiddenFalseOrderByCreatedAtDesc(shopId);
        long reviewCount = reviewRepository.countActiveReviewsByShopId(shopId);
        Double avg = reviewRepository.getAverageStarsByShopId(shopId);

        // Initial rating is 0.0 when no reviews exist; live calculated average up to max 5.0
        boolean isRatingVisible = reviewCount > 0;
        Double roundedAvg = 0.0;
        if (isRatingVisible && avg != null) {
            roundedAvg = Math.min(5.0, Math.max(1.0, Math.round(avg * 10.0) / 10.0));
        }

        // FR-1107: Automatic ETA accuracy score
        String etaAccuracy = computeEtaAccuracy(shopId);

        return ShopRatingSummaryDto.builder()
                .shopId(shop.getId())
                .averageRating(roundedAvg)
                .reviewCount(reviewCount)
                .isRatingVisible(isRatingVisible)
                .etaAccuracyScore(etaAccuracy)
                .reviews(reviewMapper.toDtoList(activeReviews))
                .build();
    }

    @Override
    @Transactional
    public ReviewDto replyToReview(UUID shopId, UUID reviewId, OwnerReplyRequest request, User ownerUser) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new ResourceNotFoundException("Shop", "id", shopId));

        verifyShopOwner(shop, ownerUser);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getShop().getId().equals(shopId)) {
            throw new BadRequestException("Review does not belong to shop " + shop.getName());
        }

        // FR-1103: Update/Post official shopkeeper reply
        review.setReply(request.getReply().trim());
        review.setReplyAt(Instant.now());
        Review updated = reviewRepository.save(review);
        log.info("Shopkeeper replied to review {} for shop {}", reviewId, shopId);
        return reviewMapper.toDto(updated);
    }

    @Override
    @Transactional
    public void reportAbuse(UUID reviewId, ReportAbuseRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        // FR-1104: Report abuse into the admin queue - Hidden while pending
        review.setAbuseReported(true);
        review.setAbuseReason(request.getReason().trim());
        review.setHidden(true);
        reviewRepository.save(review);
        log.warn("Review {} reported for abuse: {}. Temporarily hidden.", reviewId, request.getReason());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewDto> listReportedReviews(User admin) {
        validateAdmin(admin);
        List<Review> reported = reviewRepository.findByAbuseReportedTrueOrderByCreatedAtDesc();
        return reviewMapper.toDtoList(reported);
    }

    @Override
    @Transactional
    public ReviewDto moderateReview(UUID reviewId, ModerateReviewRequest request, User admin) {
        validateAdmin(admin);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if ("APPROVE".equalsIgnoreCase(request.getAction())) {
            review.setHidden(false);
            review.setAbuseReported(false);
            log.info("Admin {} approved review {}. Review is now visible.", admin.getPhone(), reviewId);
        } else {
            review.setHidden(true);
            review.setAbuseReported(false);
            log.info("Admin {} hid review {}. Review remains hidden.", admin.getPhone(), reviewId);
        }

        Review saved = reviewRepository.save(review);
        return reviewMapper.toDto(saved);
    }

    private String computeEtaAccuracy(UUID shopId) {
        Instant thirtyDaysAgo = Instant.now().minus(Duration.ofDays(30));
        List<Token> completedTokens = tokenRepository.findByShopIdAndJoinedAtBetween(shopId, thirtyDaysAgo, Instant.now())
                .stream()
                .filter(t -> t.getStatus() == TokenStatus.COMPLETED && t.getServedAt() != null && t.getJoinedAt() != null)
                .toList();

        if (completedTokens.isEmpty()) {
            return "87% accurate"; // Default cold-start baseline (BRD FR-1107)
        }

        long accurateCount = completedTokens.stream().filter(t -> {
            long actualWaitMin = Duration.between(t.getJoinedAt(), t.getServedAt()).toMinutes();
            int estimatedWaitMin = t.getEstimatedWaitMin() != null ? t.getEstimatedWaitMin() : 15;
            long error = Math.abs(actualWaitMin - estimatedWaitMin);
            return error <= 8; // Within median error tolerance (BRD Section 5.4)
        }).count();

        int percentage = (int) Math.round(((double) accurateCount / completedTokens.size()) * 100);
        percentage = Math.max(70, Math.min(98, percentage));
        return percentage + "% accurate";
    }

    private void verifyShopOwner(Shop shop, User user) {
        if (user == null) {
            // Permit merchant operations console reply
            return;
        }
        if (user.getRole() == UserRole.ADMIN) {
            return;
        }
        if (shop.getOwner() != null && !shop.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException("You are not the registered owner of this shop");
        }
    }

    private void validateAdmin(User admin) {
        if (admin == null || admin.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Admin privileges required to access this resource");
        }
    }
}

