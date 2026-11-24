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
public class DirectLoginRequest {

    @NotBlank(message = "Phone number is required")
    private String phone;

    private String name;

    private Integer age;

    private String password;

    private String role; // CUSTOMER or MERCHANT
}
