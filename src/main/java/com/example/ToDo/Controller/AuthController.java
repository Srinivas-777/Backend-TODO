package com.example.ToDo.Controller;

import com.example.ToDo.DTO.AuthResponse;
import com.example.ToDo.DTO.LoginRequest;
import com.example.ToDo.DTO.SignUpRequest;
import com.example.ToDo.Exception.UserAlreadyFoundException;
import com.example.ToDo.Exception.UserNotFoundException;
import com.example.ToDo.Helper.MyAnalyzer;
import com.example.ToDo.JWT_PACKAGE.JwtTokenProvider;
import com.example.ToDo.Model.User;
import com.example.ToDo.Repository.UserRepository;
import com.example.ToDo.ServiceInterface.UserDetailsService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.Value;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.logging.Logger;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "API endpoints for user authentication")
public class AuthController {


    private String CLIENT_ID = "753342194804-dq979uo88caihvhnivsk3pbdgcfike1h.apps.googleusercontent.com";

    private static final Logger LOGGER = Logger.getLogger(AuthController.class.getName());
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final MyAnalyzer myAnalyzer;


    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, UserDetailsService userDetailsService, MyAnalyzer myAnalyzer) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.myAnalyzer = myAnalyzer;
    }

    @PostMapping("/signup")
    @Operation(summary = "Register a new user", description = "Creates a new user account with email and password")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "409", description = "Email is already in use")
    })
    public ResponseEntity<AuthResponse> signUp(@RequestBody SignUpRequest user) throws UserAlreadyFoundException, UserNotFoundException {
        String email = user.getEmail();
        email = myAnalyzer.stem(email);
        String password = user.getPassword();
        String phoneNumber = user.getPhoneNumber();

        if (userRepository.existsByEmail(email))
            throw new UserAlreadyFoundException("Email Is Already Used With Another Account");

        User createdUser = new User();
        createdUser.setEmail(email);
        createdUser.setPassword(passwordEncoder.encode(password));
        createdUser.setPhoneNumber(phoneNumber);

        User savedUser = userRepository.save(createdUser);

        Authentication authentication = new UsernamePasswordAuthenticationToken(email, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse(token, true);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping("/signin")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<AuthResponse> signIn(@RequestBody LoginRequest loginRequest) throws UserNotFoundException {
        String username = loginRequest.getEmail();
        username = myAnalyzer.stem(username);
        String password = loginRequest.getPassword();

        Authentication authentication = authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = jwtTokenProvider.generateToken(authentication);
        AuthResponse authResponse = new AuthResponse(token, true);

        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }

    @PostMapping(value = "/google-signup", consumes = "text/plain")
    @Operation(summary = "Register with Google", description = "Creates a new user account using Google authentication")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully with Google",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid Google token"),
            @ApiResponse(responseCode = "409", description = "Email is already in use"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<?> googleSignUp(@RequestBody String idTokenString) {
        LOGGER.info("Received Google signup request with token: " + idTokenString.substring(0, Math.min(20, idTokenString.length())) + "...");

        try {
            // Process the token and extract the user info
            GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);
            if (payload == null) {
                LOGGER.warning("Token verification failed for Google signup");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, false));
            }

            // Extract email from payload
            String email = payload.getEmail();
            LOGGER.info("Google signup - Email extracted: " + email);

            // Apply stemming if necessary
            if (myAnalyzer != null) {
                email = myAnalyzer.stem(email);
                LOGGER.info("Google signup - Email after stemming: " + email);
            }

            // Check if user already exists
            if (userRepository.existsByEmail(email)) {
                LOGGER.info("Google signup - User already exists with email: " + email);
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new AuthResponse(null, false));
            }

            // Create new user
            User newUser = new User();
            newUser.setEmail(email);
            // Generate a random password for this user
            String randomPassword = "google-" + System.currentTimeMillis();
            newUser.setPassword(passwordEncoder.encode(randomPassword));
            // You may want to extract more info from the payload like name, picture, etc.

            // Save the user
            userRepository.save(newUser);
            LOGGER.info("Google signup - New user created with email: " + email);

            // Create authentication and generate token
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new AuthResponse(token, true));

        } catch (Exception e) {
            LOGGER.severe("Error in Google signup: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, false));
        }
    }

    @PostMapping(value = "/google-signin", consumes = "text/plain")
    @Operation(summary = "Login with Google", description = "Authenticates a user with Google and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful with Google",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid Google token"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Server error")
    })
    public ResponseEntity<?> googleSignIn(@RequestBody String idTokenString) {
        LOGGER.info("Received Google signin request");

        try {
            // Process the token and extract the user info
            GoogleIdToken.Payload payload = verifyGoogleToken(idTokenString);
            if (payload == null) {
                LOGGER.warning("Token verification failed for Google signin");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new AuthResponse(null, false));
            }

            // Extract email from payload
            String email = payload.getEmail();
            LOGGER.info("Google signin - Email extracted: " + email);

            // Apply stemming if necessary
            if (myAnalyzer != null) {
                email = myAnalyzer.stem(email);
                LOGGER.info("Google signin - Email after stemming: " + email);
            }

            // Check if user exists
            if (!userRepository.existsByEmail(email)) {
                LOGGER.info("Google signin - User not found with email: " + email);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new AuthResponse(null, false));
            }

            // Create authentication and generate token
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtTokenProvider.generateToken(authentication);
            LOGGER.info("Google signin - Successful for email: " + email);

            return ResponseEntity.status(HttpStatus.OK)
                    .body(new AuthResponse(token, true));

        } catch (Exception e) {
            LOGGER.severe("Error in Google signin: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AuthResponse(null, false));
        }
    }

    /**
     * Helper method to verify Google ID token
     */
    private GoogleIdToken.Payload verifyGoogleToken(String idTokenString) throws GeneralSecurityException, IOException {
        LOGGER.info("Verifying Google token");

        try {
            // Remove any whitespace or quotes that might be in the token string
            idTokenString = idTokenString.trim();
            if (idTokenString.startsWith("\"") && idTokenString.endsWith("\"")) {
                idTokenString = idTokenString.substring(1, idTokenString.length() - 1);
            }

            LOGGER.info("Token length: " + idTokenString.length());

            // Create the verifier
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(CLIENT_ID))
                    // You can add additional checks here if needed
                    .build();


            // Verify the token
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken != null) {
                Payload payload = idToken.getPayload();
                LOGGER.info("Token verified successfully for user: " + payload.getEmail());
                return payload;
            } else {
                LOGGER.warning("Invalid ID token");
                return null;
            }
        } catch (IllegalArgumentException e) {
            LOGGER.severe("Invalid token format: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            LOGGER.severe("Error verifying token: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    private Authentication authenticate(String username, String password) throws UserNotFoundException {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (userDetails == null) {
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid username or password");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}