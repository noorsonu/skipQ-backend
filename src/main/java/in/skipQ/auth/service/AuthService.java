package in.skipQ.auth.service;

import in.skipQ.auth.dto.AuthResponse;
import in.skipQ.auth.dto.DirectLoginRequest;
import in.skipQ.auth.dto.GoogleLoginRequest;
import in.skipQ.auth.dto.SendOtpRequest;
import in.skipQ.auth.dto.SendOtpResponse;
import in.skipQ.auth.dto.VerifyOtpRequest;
import in.skipQ.user.model.User;

public interface AuthService {

    SendOtpResponse sendOtp(SendOtpRequest request);

    AuthResponse verifyOtp(VerifyOtpRequest request);

    AuthResponse googleLogin(GoogleLoginRequest request);

    AuthResponse directLogin(DirectLoginRequest request);

    User getCurrentUser(String token);
}

