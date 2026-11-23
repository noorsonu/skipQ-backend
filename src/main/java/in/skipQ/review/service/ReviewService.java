package in.skipQ.review.service;

import in.skipQ.review.dto.*;
import in.skipQ.user.model.User;

import java.util.List;
import java.util.UUID;

public interface ReviewService {

    ReviewDto submitReview(CreateReviewRequest request, User currentUser);

    ShopRatingSummaryDto getShopRatingSummary(UUID shopId);

    ReviewDto replyToReview(UUID shopId, UUID reviewId, OwnerReplyRequest request, User ownerUser);

    void reportAbuse(UUID reviewId, ReportAbuseRequest request);

    List<ReviewDto> listReportedReviews(User admin);

    ReviewDto moderateReview(UUID reviewId, ModerateReviewRequest request, User admin);
}

