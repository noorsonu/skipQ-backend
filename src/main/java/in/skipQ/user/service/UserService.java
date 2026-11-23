package in.skipQ.user.service;

import in.skipQ.user.dto.DeleteAccountRequest;
import in.skipQ.user.dto.UpdateUserProfileRequest;
import in.skipQ.user.dto.UserProfileResponseDto;
import in.skipQ.user.model.User;

public interface UserService {

    UserProfileResponseDto getProfile(User currentUser);

    UserProfileResponseDto updateProfile(User currentUser, UpdateUserProfileRequest request);

    void deleteAccount(User currentUser, DeleteAccountRequest request);
}

