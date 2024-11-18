package com.example.ilia.aidemo.auth;


import com.example.ilia.aidemo.config.JwtService;
import com.example.ilia.aidemo.entity.PasswordResetTokens;
import com.example.ilia.aidemo.entity.RefreshToken;
import com.example.ilia.aidemo.entity.User;
import com.example.ilia.aidemo.payload.NewPassDTO;
import com.example.ilia.aidemo.repository.PasswordResetTokensRepository;
import com.example.ilia.aidemo.repository.RefreshTokenRepository;
import com.example.ilia.aidemo.repository.UserRepository;
import com.example.ilia.aidemo.service.RefreshTokenService;
import com.example.ilia.aidemo.service.SendEmailService;
import com.example.ilia.aidemo.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CODE_LENGTH = 6;
    private static final Random RANDOM = new SecureRandom();
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokensRepository passwordResetTokensRepository;
    private final SendEmailService sendEmailService;


    public void register(RegisterRequest input) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }


        User user = new User();
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole("ROLE_USER");
        user.setCredits(10);
        user.setTier("FREE");
        user.setSubscriptionId("none");


        userRepository.save(user);

    }

    public ResponseEntity<?> registerWithBindingResults(RegisterRequest input, BindingResult bindingResult) {
        if (userRepository.existsByEmail(input.getEmail())) {
            throw new RuntimeException("User with this email already exists.");
        }

        if (bindingResult.hasErrors()) {
            // Create a JSON-like structure to hold errors
            HashMap<String, HashMap<String, String>> responseJSON = new HashMap<>();
            HashMap<String, String> errorsJSON = new HashMap<>();

            // Iterate through all errors and add them to the JSON
            bindingResult.getAllErrors().forEach(error -> {
                if (error instanceof FieldError) {
                    // Extract field name and error message
                    String fieldName = ((FieldError) error).getField();
                    String errorMessage = error.getDefaultMessage();
                    errorsJSON.put(fieldName, errorMessage);
                } else {
                    // Handle global errors (not specific to a field)
                    errorsJSON.put(error.getObjectName(), error.getDefaultMessage());
                }
            });

            responseJSON.put("errors", errorsJSON);
            return ResponseEntity.badRequest().body(responseJSON);
        }

        User user = new User();
        user.setEmail(input.getEmail());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        user.setRole("ROLE_USER");
        user.setCredits(10);
        user.setTier("FREE");
        user.setSubscriptionId("none");


        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Account successfully created");
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        if (authentication.isAuthenticated()) {
            var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateToken(user);

            refreshTokenRepository.deleteByOwnerId(user.getId());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());


            HashMap<String, String> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("refreshToken", refreshToken.getToken());
            userDetails.put("email", user.getEmail());
            userDetails.put("role", user.getRole());
            userDetails.put("tier", user.getTier());
            userDetails.put("credits", String.valueOf(user.getCredits()));


            return AuthenticationResponse.builder()
                    .access_token(jwtToken)
                    .refresh_token(refreshToken.getToken())
                    .userDetails(userDetails)
                    .build();

        } else {
            throw new UsernameNotFoundException("invalid user request..!!");
        }

    }

    public AuthenticationResponse OAuthLogin(AuthenticationRequest request) {
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        var jwtToken = jwtService.generateToken(user);

        refreshTokenRepository.deleteByOwnerId(user.getId());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());


        HashMap<String, String> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("refreshToken", refreshToken.getToken());
        userDetails.put("email", user.getEmail());
        userDetails.put("role", user.getRole());
        userDetails.put("tier", user.getTier());
        userDetails.put("credits", String.valueOf(user.getCredits()));


        return AuthenticationResponse.builder()
                .access_token(jwtToken)
                .refresh_token(refreshToken.getToken())
                .userDetails(userDetails)
                .build();


    }

    public ResponseEntity<AuthenticationResponse> authenticateTESTCookies(AuthenticationRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        if (authentication.isAuthenticated()) {
            var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

            var jwtToken = jwtService.generateToken(user);
            refreshTokenRepository.deleteByOwnerId(user.getId());

            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

            HashMap<String, String> userDetails = new HashMap<>();
            userDetails.put("id", user.getId());
            userDetails.put("refreshToken", refreshToken.getToken());
            userDetails.put("email", user.getEmail());
            userDetails.put("role", user.getRole());
            userDetails.put("tier", user.getTier());
            userDetails.put("credits", String.valueOf(user.getCredits()));

            // Create a cookie with the JWT token
            ResponseCookie cookie = ResponseCookie.from("jwt", jwtToken) // Set cookie name and value
                    .httpOnly(true) // Prevents JavaScript access to the cookie
                    .secure(true) // Ensures the cookie is sent only over HTTPS
                    .path("/") // Makes the cookie available on all pages of the website
                    .maxAge(3600) // Sets the cookie to expire in 1 hour
                    .sameSite("Lax") // Helps mitigate CSRF attacks
                    .build();

            // Create the AuthenticationResponse to return
            AuthenticationResponse response = AuthenticationResponse.builder()
                    .access_token(jwtToken)
                    .refresh_token(refreshToken.getToken())
                    .userDetails(userDetails)
                    .build();

            // Return the response along with the cookie in the response headers
            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString()) // Add the cookie to the response
                    .body(response); // Return the response body
        } else {
            throw new UsernameNotFoundException("Invalid user request..!!");
        }

    }


    public AuthenticationResponse refreshToken(RefreshTokenRequestDTO refreshTokenRequestDTO) {
        Optional<RefreshToken> optionalRefreshToken = refreshTokenService.findByToken(refreshTokenRequestDTO.getToken());

        if (optionalRefreshToken.isEmpty()) {
            // If the refresh token is not found, throw an exception
            throw new RuntimeException("Refresh Token is not in DB..!!");
        }

        // Get the refresh token from the Optional
        RefreshToken refreshToken = optionalRefreshToken.get();

        // Verify the expiration of the refresh token
        try {
            refreshToken = refreshTokenService.verifyExpiration(refreshToken);
        } catch (RuntimeException e) {
            // If the refresh token is expired, an exception will be thrown
            throw new RuntimeException(e.getMessage());
        }

        // Get the user information associated with the refresh token
        User user = refreshToken.getOwner();
        // Generate a new access token using the user's username
        String accessToken = jwtService.generateToken(user);


        // Create and return the response containing the new access token and the refresh token
        HashMap<String, String> userDetails = new HashMap<>();
        userDetails.put("id", user.getId());
        userDetails.put("refreshToken", refreshToken.getToken());
        userDetails.put("email", user.getEmail());
        userDetails.put("role", user.getRole());
        userDetails.put("tier", user.getTier());
        userDetails.put("credits", String.valueOf(user.getCredits()));

        //fixed

        return AuthenticationResponse.builder()
                .access_token(accessToken)
                .refresh_token(refreshToken.getToken())
                .userDetails(userDetails)
                .build();
    }

    public ResponseEntity<?> loginWithGoogle(String credentials) throws GeneralSecurityException, IOException {


        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), GsonFactory.getDefaultInstance()).setAudience(Collections.singletonList("345003367363-6rrfjbttfa6370pnvgd20tps0jg6u8aa.apps.googleusercontent.com"
                )) // Use clientId from request
                .build();

        // Verify the credentials (ID token)
        GoogleIdToken idToken;
        try {

            idToken = verifier.verify(credentials);

            // ...rest of your logic
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace(); // This will print the stack trace to the console
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error verifying token: " + e.getMessage());
        }


        if (idToken != null) {

            GoogleIdToken.Payload payload = idToken.getPayload();

            // Get user info from the payload
            String email = payload.getEmail();

            System.out.println(email);


            if (userIsNotInDB(email)) {
                register(new RegisterRequest(credentials, email));
            }


            return ResponseEntity.ok(OAuthLogin((new AuthenticationRequest(email, credentials))));

        }
        return ResponseEntity.badRequest().body("Invalid ID token.");
    }


    public static String generateCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }
        return code.toString();
    }

    public boolean userIsNotInDB(String email) {
        return userRepository.findByEmail(email).isEmpty();
    }


    public ResponseEntity<AuthenticationResponse> loginWithGithub(String email, Long userIdFromAccessToken) {

        if (userIsNotInDB(email)) {
            register(new RegisterRequest(String.valueOf(userIdFromAccessToken), email));
        }

        return ResponseEntity.ok(OAuthLogin(new AuthenticationRequest(email, String.valueOf(userIdFromAccessToken))));

    }

    public void sendPassToken(String email) {
        if (userRepository.findByEmail(email).isEmpty()) {
            throw new RuntimeException("User was not found");
        }

        PasswordResetTokens token = new PasswordResetTokens();
        token.setOwner(userRepository.findByEmail(email).orElseThrow());
        token.setExpired_at(new Date(System.currentTimeMillis() + 300000));
        String resetCode = generateCode();
        token.setToken(resetCode);
        passwordResetTokensRepository.save(token);
        sendEmailService.sendResetPassword(email, resetCode);
    }

    public PasswordResetTokens getLatestToken(NewPassDTO request) {
        List<Optional<PasswordResetTokens>> list = passwordResetTokensRepository.findTopByUserIdOrderByExpiredAtDesc(userRepository.findByEmail(request.getEmail()).orElseThrow().getId());
        return list.get(list.size() - 1).orElseThrow();
    }

    public void changePass(NewPassDTO request) {
        // Retrieve the latest token once
        PasswordResetTokens resetToken = getLatestToken(request);
        System.out.println(resetToken.getToken());

        // Check if the tokens are the same
        if (request.getCode().equals(resetToken.getToken())) {
            // Check if the token is not expired
            if (new Date(System.currentTimeMillis()).before(resetToken.getExpired_at())) {
                // Check if the passwords match
                if (request.getPassword().equals(request.getRepeatedPassword())) {
                    // Find the user by email
                    User user = userRepository.findByEmail(request.getEmail()).orElseThrow(() ->
                            new RuntimeException("User not found"));

                    // Set the new password
                    user.setPassword(passwordEncoder.encode(request.getPassword()));

                    // Save the user
                    userRepository.save(user);

                    passwordResetTokensRepository.deleteAllByOwner(user);

                    // Optionally invalidate the token
                    // resetTokenRepository.delete(resetToken);

                    // Provide success feedback (e.g., log, return value, etc.)
                    System.out.println("Password reset successful");
                } else {
                    throw new RuntimeException("Passwords do not match");
                }
            } else {
                throw new RuntimeException("Token has expired");
            }
        } else {
            throw new RuntimeException("Invalid token");
        }
    }


}
