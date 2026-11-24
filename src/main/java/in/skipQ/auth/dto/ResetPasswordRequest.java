package in.skipQ.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    private String phone;

    private String email;

    @NotBlank(message = "New password is required")
    private String newPassword;

    private String googleId; // Set if verified via Google (Option B)

    private String role; // CUSTOMER or MERCHANT
}
