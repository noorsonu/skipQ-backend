package in.skipQ.shop.service;

import in.skipQ.shop.dto.ShopStatusResponseDto;
import in.skipQ.shop.dto.UpdateStatusRequest;
import in.skipQ.user.model.User;

import java.util.UUID;

public interface ShopStatusService {

    ShopStatusResponseDto updateStatus(UUID shopId, UpdateStatusRequest request, User currentUser);

    ShopStatusResponseDto getStatus(UUID shopId);
}

