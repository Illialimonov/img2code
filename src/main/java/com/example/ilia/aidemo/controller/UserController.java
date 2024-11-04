package com.example.ilia.aidemo.controller;

import com.example.ilia.aidemo.entity.AuthorizedUserActivity;
import com.example.ilia.aidemo.entity.User;
import com.example.ilia.aidemo.payload.ContactUsDTO;
import com.example.ilia.aidemo.payload.HistoryDTO;
import com.example.ilia.aidemo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @DeleteMapping("/self-delete")
    public ResponseEntity<?> selfDestruct() {
        return userService.selfDestruct();
    }

    @GetMapping("/history")
    public List<HistoryDTO> getHistory() {
        return userService.getHistory();
    }

    @GetMapping("/currentusername")
    public String currentUserName() {
        return userService.getCurrentUser().getEmail();
    }

    @GetMapping("/cookie")
    public String yourMethod(@CookieValue(name = "jwt", required = false) String jwtToken) {
        if (jwtToken == null) {
            // Handle the case where the JWT cookie is not present
            throw new RuntimeException("JWT token is missing");
        }

        // Use the JWT token (e.g., validate it, parse it, etc.)
        return "Token retrieved from cookie: " + jwtToken;
    }

    @PostMapping("/contact-us")
    public ResponseEntity<?> contact(@RequestBody ContactUsDTO contactUsDTO) {
        return userService.contactUs(contactUsDTO);
    }

    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        userService.deleteUser(id);
    }
}
