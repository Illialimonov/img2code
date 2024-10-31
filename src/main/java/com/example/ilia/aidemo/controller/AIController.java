package com.example.ilia.aidemo.controller;

import com.example.ilia.aidemo.payload.AiResponse;
import com.example.ilia.aidemo.service.AiService;
import com.example.ilia.aidemo.service.UserService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@AllArgsConstructor
public class AIController {
    private AiService aiService;

    @PostMapping("/convert")
    public AiResponse convert(@RequestPart("file") MultipartFile file, @RequestPart(value = "comments", required = false) Optional<String> comments, @RequestPart(value = "fingerprint", required = false) Optional<String> fingerprint, Authentication authentication) throws IOException, StripeException, InterruptedException {
        return aiService.photoToCodeFROMURL(file, comments, fingerprint, authentication);
    }

    @Operation(summary = "before: getRemainingConversions")
    @GetMapping("/remaining-conversions")
    public Map<String, String> getRemainingConversions(@RequestParam("visitorId") String visitorId) {
        return aiService.getRemainingConversions(visitorId);
    }


    @GetMapping("/hi")
    public String hi() {
        return "hi!!!";
    }


}
