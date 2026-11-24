package in.skipQ.auth.service;

import in.skipQ.auth.dto.AuthResponse;
import in.skipQ.auth.dto.SendOtpRequest;
import in.skipQ.auth.dto.SendOtpResponse;
import in.skipQ.auth.dto.VerifyOtpRequest;
import in.skipQ.auth.jwt.JwtUtil;
import in.skipQ.auth.otp.OtpStore;
import in.skipQ.common.exception.BadRequestException;
import in.skipQ.common.exception.ResourceNotFoundException;
import in.skipQ.common.exception.UnauthorizedException;
import in.skipQ.user.model.User;
import in.skipQ.user.model.UserRole;
import in.skipQ.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final OtpStore otpStore;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    public SendOtpResponse sendOtp(SendOtpRequest request) {
        String phone = request.getPhone().trim();
        log.info("Sending OTP request for phone: {}", phone);

        String otp = otpStore.generateAndStore(phone);

        User user = userRepository.findByPhone(phone).orElse(null);
        boolean isRegistered = (user != null && !Boolean.TRUE.equals(user.getDeleted()));
        String existingName = user != null ? user.getName() : null;
        Integer existingAge = user != null ? user.getAge() : null;

        return SendOtpResponse.builder()
                .message("Verification code dispatched successfully to +91 " + phone)
                .expiresInSeconds(otpStore.ttlSeconds())
                .registered(isRegistered)
                .name(existingName)
                .age(existingAge)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        String phone = request.getPhone().trim();
        String otp = request.getOtp().trim();
        log.info("Verifying OTP for phone: {}", phone);

        boolean isValid = otpStore.verify(phone, otp);
        if (!isValid) {
            throw new UnauthorizedException("Invalid or expired OTP. Please request a new one.");
        }

        // Check if user exists (Login) or is fresh (Register)
        boolean isNewUser = false;
        User user = userRepository.findByPhone(phone).orElse(null);

        if (user == null) {
            log.info("Phone {} not registered. Auto-registering new user.", phone);
            user = User.builder()
                    .phone(phone)
                    .name(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : null)
                    .age(request.getAge())
                    .role(UserRole.CUSTOMER)
                    .language("hi")
                    .blocked(false)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        } else {
            boolean updated = false;
            if (request.getName() != null && !request.getName().isBlank() && (user.getName() == null || user.getName().isBlank())) {
                user.setName(request.getName().trim());
                updated = true;
            }
            if (request.getAge() != null && user.getAge() == null) {
                user.setAge(request.getAge());
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
            if (Boolean.TRUE.equals(user.getBlocked())) {
                throw new UnauthorizedException("Your account has been suspended. Please contact support.");
            } else if (Boolean.TRUE.equals(user.getDeleted())) {
                throw new UnauthorizedException("This account has been deleted. Please register with a new number or contact support.");
            }
        }

        String token = jwtUtil.generateToken(user);
        log.info("User {} authenticated successfully. New user: {}", user.getId(), isNewUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .phone(user.getPhone())
                .name(user.getName())
                .age(user.getAge())
                .role(user.getRole())
                .newUser(isNewUser)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }
        String jwt = token.substring(7);
        if (!jwtUtil.isValid(jwt)) {
            throw new UnauthorizedException("Expired or invalid JWT token");
        }
        UUID userId = jwtUtil.extractUserId(jwt);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        if (Boolean.TRUE.equals(user.getDeleted())) {
            throw new UnauthorizedException("Account has been deleted.");
        }
        if (Boolean.TRUE.equals(user.getBlocked())) {
            throw new UnauthorizedException("Account has been suspended.");
        }
        return user;
    }

    @Override
    @Transactional
    public AuthResponse googleLogin(in.skipQ.auth.dto.GoogleLoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Google login request for email: {}", email);

        User user = userRepository.findByEmail(email).orElse(null);
        boolean isNewUser = false;

        // If not found by email, check if phone was provided and matches
        if (user == null && request.getPhone() != null && !request.getPhone().isBlank()) {
            String cleanPhone = request.getPhone().replaceAll("\\D", "");
            if (cleanPhone.length() >= 10) {
                cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
                user = userRepository.findByPhone(cleanPhone).orElse(null);
                if (user != null && (user.getEmail() == null || user.getEmail().isBlank())) {
                    user.setEmail(email);
                    user = userRepository.save(user);
                }
            }
        }

        if (user == null) {
            log.info("Email {} not registered. Auto-registering new user via Google.", email);
            UserRole role = UserRole.CUSTOMER;
            if (request.getRole() != null && (request.getRole().equalsIgnoreCase("MERCHANT") || request.getRole().equalsIgnoreCase("SHOP_OWNER"))) {
                role = UserRole.SHOP_OWNER;
            }
            String cleanPhone = null;
            if (request.getPhone() != null && !request.getPhone().isBlank()) {
                cleanPhone = request.getPhone().replaceAll("\\D", "");
                if (cleanPhone.length() >= 10) cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            }
            user = User.builder()
                    .email(email)
                    .phone(cleanPhone)
                    .name(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : "Google User")
                    .role(role)
                    .language("hi")
                    .blocked(false)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        } else {
            boolean updated = false;
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                user.setEmail(email);
                updated = true;
            }
            if (request.getName() != null && !request.getName().isBlank() && (user.getName() == null || user.getName().isBlank())) {
                user.setName(request.getName().trim());
                updated = true;
            }
            if (request.getRole() != null && (request.getRole().equalsIgnoreCase("MERCHANT") || request.getRole().equalsIgnoreCase("SHOP_OWNER")) && user.getRole() != UserRole.SHOP_OWNER) {
                user.setRole(UserRole.SHOP_OWNER);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
            if (Boolean.TRUE.equals(user.getBlocked())) {
                throw new UnauthorizedException("Your account has been suspended. Please contact support.");
            } else if (Boolean.TRUE.equals(user.getDeleted())) {
                throw new UnauthorizedException("This account has been deleted. Please contact support.");
            }
        }

        String token = jwtUtil.generateToken(user);
        log.info("User {} authenticated successfully via Google. New user: {}", user.getId(), isNewUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .role(user.getRole())
                .newUser(isNewUser)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse directLogin(in.skipQ.auth.dto.DirectLoginRequest request) {
        String phone = null;
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String cleanPhone = request.getPhone().replaceAll("\\D", "");
            if (cleanPhone.length() >= 10) {
                phone = cleanPhone.substring(cleanPhone.length() - 10);
            }
        }
        String email = null;
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            email = request.getEmail().trim().toLowerCase();
        }

        if (phone == null && email == null) {
            throw new in.skipQ.common.exception.BadRequestException("Please enter a valid 10-digit mobile number or email address.");
        }
        log.info("Direct login request for phone: {}, email: {}", phone, email);

        User user = null;
        if (email != null) {
            user = userRepository.findByEmail(email).orElse(null);
        }
        if (user == null && phone != null) {
            user = userRepository.findByPhone(phone).orElse(null);
        }
        boolean isNewUser = false;

        if (user == null) {
            log.info("Identifier phone={}, email={} not registered. Auto-registering new user via direct login.", phone, email);
            UserRole role = UserRole.CUSTOMER;
            if (request.getRole() != null && (request.getRole().equalsIgnoreCase("MERCHANT") || request.getRole().equalsIgnoreCase("SHOP_OWNER"))) {
                role = UserRole.SHOP_OWNER;
            }

            String defaultName = request.getName() != null && !request.getName().isBlank() 
                    ? request.getName().trim() 
                    : (phone != null ? "Customer (" + phone.substring(phone.length() - 4) + ")" : email.split("@")[0]);
            user = User.builder()
                    .phone(phone)
                    .name(defaultName)
                    .age(request.getAge())
                    .email(email)
                    .password(request.getPassword() != null ? request.getPassword().trim() : null)
                    .role(role)
                    .language("hi")
                    .blocked(false)
                    .build();
            user = userRepository.save(user);
            isNewUser = true;
        } else {
            // Verify password if user already has a password set
            if (user.getPassword() != null && !user.getPassword().isBlank()) {
                if (request.getPassword() == null || request.getPassword().isBlank()) {
                    throw new UnauthorizedException("Password is required to sign in.");
                }
                if (!user.getPassword().equals(request.getPassword().trim())) {
                    throw new UnauthorizedException("Incorrect password. Please try again or click 'Forgot Password'.");
                }
            } else if (request.getPassword() != null && !request.getPassword().isBlank()) {
                // First-time password set for legacy / previously passwordless user
                user.setPassword(request.getPassword().trim());
            }

            boolean updated = false;
            if (phone != null && user.getPhone() == null) {
                user.setPhone(phone);
                updated = true;
            }
            if (email != null && user.getEmail() == null) {
                user.setEmail(email);
                updated = true;
            }
            if (request.getName() != null && !request.getName().isBlank() && (user.getName() == null || user.getName().isBlank())) {
                user.setName(request.getName().trim());
                updated = true;
            }
            if (request.getAge() != null && user.getAge() == null) {
                user.setAge(request.getAge());
                updated = true;
            }
            if (request.getEmail() != null && !request.getEmail().isBlank() && (user.getEmail() == null || user.getEmail().isBlank())) {
                user.setEmail(request.getEmail().trim().toLowerCase());
                updated = true;
            }
            if (request.getRole() != null && (request.getRole().equalsIgnoreCase("MERCHANT") || request.getRole().equalsIgnoreCase("SHOP_OWNER")) && user.getRole() != UserRole.SHOP_OWNER) {
                user.setRole(UserRole.SHOP_OWNER);
                updated = true;
            }
            if (updated) {
                user = userRepository.save(user);
            }
            if (Boolean.TRUE.equals(user.getBlocked())) {
                throw new UnauthorizedException("Your account has been suspended. Please contact support.");
            } else if (Boolean.TRUE.equals(user.getDeleted())) {
                throw new UnauthorizedException("This account has been deleted. Please contact support.");
            }
        }

        String token = jwtUtil.generateToken(user);
        log.info("User {} authenticated successfully via direct login. New user: {}", user.getId(), isNewUser);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .role(user.getRole())
                .newUser(isNewUser)
                .build();
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(in.skipQ.auth.dto.ResetPasswordRequest request) {
        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 4) {
            throw new BadRequestException("Password must be at least 4 characters long.");
        }

        User user = null;
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String cleanPhone = request.getPhone().replaceAll("\\D", "");
            if (cleanPhone.length() >= 10) cleanPhone = cleanPhone.substring(cleanPhone.length() - 10);
            user = userRepository.findByPhone(cleanPhone).orElse(null);
        }

        if (user == null && request.getEmail() != null && !request.getEmail().isBlank()) {
            user = userRepository.findByEmail(request.getEmail().trim().toLowerCase()).orElse(null);
        }

        if (user == null) {
            throw new ResourceNotFoundException("User not found with the provided details.");
        }

        // Option B: Google 1-Click recovery verification
        if (request.getGoogleId() != null && !request.getGoogleId().isBlank()) {
            if (request.getEmail() == null || user.getEmail() == null ||
                !request.getEmail().trim().equalsIgnoreCase(user.getEmail().trim())) {
                throw new UnauthorizedException("Google email does not match the registered email for this account.");
            }
            log.info("User {} verified via Google recovery.", user.getId());
        } else {
            // Option A: Email verification match
            if (request.getEmail() == null || request.getEmail().isBlank()) {
                throw new BadRequestException("Registered email address is required to reset password.");
            }
            if (user.getEmail() == null || user.getEmail().isBlank()) {
                // If user had no email registered, allow setting it during this reset
                user.setEmail(request.getEmail().trim().toLowerCase());
            } else if (!user.getEmail().trim().equalsIgnoreCase(request.getEmail().trim())) {
                throw new UnauthorizedException("The email address provided does not match our records for this account.");
            }
        }

        // Update password
        user.setPassword(request.getNewPassword().trim());
        user = userRepository.save(user);
        log.info("Password successfully reset for user: {}", user.getId());

        String token = jwtUtil.generateToken(user);
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .phone(user.getPhone())
                .email(user.getEmail())
                .name(user.getName())
                .age(user.getAge())
                .role(user.getRole())
                .newUser(false)
                .build();
    }
}

