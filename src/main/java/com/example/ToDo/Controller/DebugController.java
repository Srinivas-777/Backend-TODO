package com.example.ToDo.Controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Temporary controller for debugging authentication issues.
 * IMPORTANT: Remove this in production!
 */
@RestController
@RequestMapping("/debug")
@CrossOrigin(origins = "*")
@Tag(name = "Debug", description = "Debugging endpoints (REMOVE IN PRODUCTION)")
public class DebugController {

    private static final Logger LOGGER = Logger.getLogger(DebugController.class.getName());

    @PostMapping("/token")
    @Operation(summary = "Debug JWT token",
            description = "Decodes and returns information about a JWT token for debugging purposes. NOT FOR PRODUCTION USE.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token decoded successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "object"))),
            @ApiResponse(responseCode = "500", description = "Error decoding token")
    })
    public ResponseEntity<?> debugToken(@RequestBody String token) {
        LOGGER.info("Debug token received. Length: " + token.length());

        Map<String, Object> response = new HashMap<>();
        response.put("tokenLength", token.length());

        try {
            // Remove any surrounding quotes
            token = token.trim();
            if (token.startsWith("\"") && token.endsWith("\"")) {
                token = token.substring(1, token.length() - 1);
                response.put("trimmedQuotes", true);
            }

            // Split into parts
            String[] parts = token.split("\\.");
            response.put("parts", parts.length);

            if (parts.length == 3) {
                // Decode header
                String header = new String(Base64.getUrlDecoder().decode(parts[0]));
                response.put("header", header);

                // Decode payload
                String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
                response.put("payload", payload);

                // We don't decode the signature
                response.put("signature", parts[2].substring(0, Math.min(10, parts[2].length())) + "...");
            }

            response.put("success", true);
        } catch (Exception e) {
            LOGGER.severe("Error debugging token: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("errorType", e.getClass().getName());
            response.put("stackTrace", e.getStackTrace()[0].toString());
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Health check",
            description = "Simple health check endpoint to verify the API is running")
    @ApiResponse(responseCode = "200", description = "API is running")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", System.currentTimeMillis());

        return ResponseEntity.ok(response);
    }
}