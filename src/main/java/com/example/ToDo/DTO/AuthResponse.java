package com.example.ToDo.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response after successful login or registration")
public class AuthResponse {
     @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
     private String jwt;

     @Schema(description = "Indicates if the authentication was successful", example = "true")
     private boolean status;
}
