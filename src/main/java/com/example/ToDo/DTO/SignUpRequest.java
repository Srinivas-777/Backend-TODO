
// SignUpRequest.java
package com.example.ToDo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Sign up request payload")
public class SignUpRequest {
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    private String email;

    @Schema(description = "User's password", example = "password123", required = true)
    private String password;

    @Schema(description = "User's phone number", example = "+1234567890")
    private String phoneNumber;
}