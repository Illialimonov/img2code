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
