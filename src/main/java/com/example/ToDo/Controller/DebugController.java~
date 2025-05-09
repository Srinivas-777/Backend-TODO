package com.example.ToDo.Controller;

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
public class DebugController {

    private static final Logger LOGGER = Logger.getLogger(DebugController.class.getName());

    @PostMapping("/token")
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
                response.put("signature", parts[2].substring(0, 10) + "...");
            }

            response.put("success", true);
        } catch (Exception e) {
            LOGGER.severe("Error debugging token: " + e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return ResponseEntity.ok(response);
    }
}
