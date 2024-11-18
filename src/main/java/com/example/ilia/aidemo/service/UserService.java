package com.example.ilia.aidemo.service;

import com.example.ilia.aidemo.entity.AuthorizedUserActivity;
import com.example.ilia.aidemo.entity.User;
import com.example.ilia.aidemo.payload.ContactUsDTO;
import com.example.ilia.aidemo.payload.HistoryDTO;
import com.example.ilia.aidemo.repository.AuthorizedUserActivityRepository;
import com.example.ilia.aidemo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorizedUserActivityRepository authorizedUserActivityRepository;

    @Autowired
    private final SendEmailService sendEmailService;

    public UserService(SendEmailService sendEmailService) {
        this.sendEmailService = sendEmailService;
    }

    public User getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (User) authentication.getPrincipal();
            return userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        } catch (RuntimeException e) {
            throw new ClassCastException("Not user found!");
        }
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User getUserById(String id) {
        return userRepository.findById(id).orElse(null);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

    public List<HistoryDTO> getHistory() {
        User user = getCurrentUser();
        List<AuthorizedUserActivity> userActivityList= authorizedUserActivityRepository.findAllByUserId(user.getId());
        return userActivityList.stream().map(this::convertToDTO).toList();
    }

    private HistoryDTO convertToDTO(AuthorizedUserActivity authorizedUserActivity) {
        HistoryDTO historyDTO = new HistoryDTO();
        historyDTO.setConversionId(authorizedUserActivity.getConversionId());
        historyDTO.setUser_id(authorizedUserActivity.getUserId());
        historyDTO.setCode_language(authorizedUserActivity.getCodeLanguage());
        historyDTO.setFile_url(authorizedUserActivity.getFileUrl());
        historyDTO.setCode(readTextFileFromUrl(authorizedUserActivity.getCodeUrl()));
        return historyDTO;
    }

    public static String readTextFileFromUrl(String fileUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new URL(fileUrl).openStream(), StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public ResponseEntity<?> selfDestruct() {
        User user = getCurrentUser();
        userRepository.delete(user);
        return ResponseEntity.ok("The account was deleted");
    }

    public ResponseEntity<?> contactUs(ContactUsDTO contactUsDTO) {
        String message = contactUsDTO.getMessage();
        sendEmailService.sendEmailToContactUs(contactUsDTO.getSenderEmail(), message);
        return ResponseEntity.ok("The message was sent");
    }
}
