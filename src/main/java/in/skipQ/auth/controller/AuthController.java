package in.skipQ.auth.controller;

import in.skipQ.auth.dto.AuthResponse;
import in.skipQ.auth.dto.SendOtpRequest;
import in.skipQ.auth.dto.SendOtpResponse;
import in.skipQ.auth.dto.VerifyOtpRequest;
import in.skipQ.auth.service.AuthService;
import in.skipQ.common.dto.ApiResponse;
import in.skipQ.user.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import in.skipQ.user.repository.UserRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "M5 Accounts & Auth", description = "Passwordless OTP Login & Registration APIs (BRD Section 16, 24)")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;

    @GetMapping("/check-phone/{phone}")
    @Operation(summary = "Check if customer phone is already registered", description = "Checks whether a customer profile exists for this phone")
    public ResponseEntity<ApiResponse<java.util.Map<String, Object>>> checkPhone(@PathVariable String phone) {
        String clean = phone.replaceAll("\\D", "");
        if (clean.length() > 10) clean = clean.substring(clean.length() - 10);
        User user = userRepository.findByPhone(clean).orElse(null);
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        if (user != null && !Boolean.TRUE.equals(user.getDeleted())) {
            data.put("registered", true);
            data.put("name", user.getName());
            data.put("age", user.getAge());
            data.put("phone", user.getPhone());
        } else {
            data.put("registered", false);
            data.put("name", null);
            data.put("age", null);
            data.put("phone", clean);
        }
        return ResponseEntity.ok(ApiResponse.ok(data, "Phone registration status checked"));
    }

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to mobile number", description = "Generates and sends a 6-digit OTP to the Indian mobile number with 120s expiry")
    public ResponseEntity<ApiResponse<SendOtpResponse>> sendOtp(@Valid @RequestBody SendOtpRequest request) {
        SendOtpResponse response = authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "OTP dispatched successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP & Authenticate", description = "Verifies 6-digit OTP, auto-registers if new, and returns JWT Bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Authentication successful"));
    }

    @PostMapping("/google-login")
    @Operation(summary = "Authenticate with Google", description = "Authenticates or registers user via Google account and returns JWT Bearer token")
    public ResponseEntity<ApiResponse<AuthResponse>> googleLogin(@Valid @RequestBody in.skipQ.auth.dto.GoogleLoginRequest request) {
        AuthResponse response = authService.googleLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Google authentication successful"));
    }

    @PostMapping("/direct-login")
    @Operation(summary = "Direct phone authentication without SMS OTP", description = "Instantly logs in or auto-registers customer/merchant without requiring SMS OTP")
    public ResponseEntity<ApiResponse<AuthResponse>> directLogin(@Valid @RequestBody in.skipQ.auth.dto.DirectLoginRequest request) {
        AuthResponse response = authService.directLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(response, "Direct authentication successful"));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user profile", description = "Decodes JWT and fetches current user details")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String token) {
        User user = authService.getCurrentUser(token);
        return ResponseEntity.ok(ApiResponse.ok(user, "User profile fetched"));
    }
}

