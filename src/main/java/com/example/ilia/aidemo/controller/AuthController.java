package com.example.ilia.aidemo.controller;

import com.example.ilia.aidemo.auth.*;
import com.example.ilia.aidemo.payload.CredetialsField;
import com.example.ilia.aidemo.payload.NewPassDTO;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
public class AuthController {
    private final AuthenticationService service;

    @Operation(summary = "before: /refreshtoken")
    @PostMapping("/refresh-token")
    public AuthenticationResponse refreshToken(@RequestBody RefreshTokenRequestDTO refreshTokenRequestDTO) {
        return service.refreshToken(refreshTokenRequestDTO);
    }


    @GetMapping("/get-ip")
    public String getUserIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");

        if (ipAddress == null || ipAddress.isEmpty()) {
            ipAddress = request.getRemoteAddr();
        }

        return "User IP Address: " + ipAddress;
    }

    @PostMapping("/google")
    public ResponseEntity<?> loginWithGoogle(@RequestPart("credentials") String credentials) throws Exception {
        System.out.println(credentials);
        return service.loginWithGoogle(credentials);
    }


    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        service.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Account successfully created");
    }

    @Operation(summary = "before: authenticate")
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @Operation(summary = "send the code to email to change the password. Then insert to /changepassword below.")
    @PostMapping("/reset")
    public ResponseEntity<HttpStatus> sendResetToken(@RequestBody ResetRequest request) {
        service.sendPassToken(request.getEmail());
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<HttpStatus> changePassword(@RequestBody NewPassDTO request) {
        service.changePass(request);
        return ResponseEntity.ok(HttpStatus.OK);
    }
}
