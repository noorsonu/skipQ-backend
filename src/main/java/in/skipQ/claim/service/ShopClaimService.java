package in.skipQ.claim.service;

import in.skipQ.claim.dto.ClaimResponseDto;
import in.skipQ.claim.dto.ReviewClaimRequest;
import in.skipQ.claim.dto.SubmitClaimRequest;
import in.skipQ.claim.model.ClaimStatus;
import in.skipQ.user.model.User;

import java.util.List;
import java.util.UUID;

public interface ShopClaimService {

    ClaimResponseDto submitClaim(UUID shopId, SubmitClaimRequest request, User applicant);

    List<ClaimResponseDto> getMyClaims(User applicant);

    List<ClaimResponseDto> getClaimsByStatus(ClaimStatus status, User admin);

    ClaimResponseDto reviewClaim(UUID claimId, ReviewClaimRequest request, User admin);
}

