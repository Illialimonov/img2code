package com.example.ilia.aidemo;

import com.example.ilia.aidemo.auth.AuthenticationService;
import com.example.ilia.aidemo.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@RestController

public class GitHubOAuthController {

    @Autowired
    private final AuthenticationService authenticationService;


    @Value("${github.client-id}")
    private String clientId;

    @Value("${github.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public GitHubOAuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/github")
    public ResponseEntity<?> githubCallback(@RequestPart("code") String code) {
        String accessToken = getAccessToken(code);
        if (accessToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve access token");
        }

        // Use the access token to get the user's email
        String email = getUserEmail(accessToken);
        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Failed to retrieve user email");
        }

        // Process user registration or authentication with the email
        return authenticationService.loginWithGithub(email, getUserIdFromAccessToken(accessToken));
    }

    private String getAccessToken(String code) {
        String accessTokenUri = "https://github.com/login/oauth/access_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> requestBody = Map.of(
                "client_id", clientId,
                "client_secret", clientSecret,
                "code", code
        );

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(accessTokenUri, HttpMethod.POST, requestEntity, Map.class);
        Map<String, Object> body = response.getBody();

        if (body != null && body.containsKey("access_token")) {
            return (String) body.get("access_token");
        }
        return null;
    }

    private String getUserEmail(String accessToken) {
        String userEmailUri = "https://api.github.com/user/emails";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(userEmailUri, HttpMethod.GET, requestEntity, List.class);
        List<Map<String, Object>> emails = response.getBody();

        if (emails != null) {
            for (Map<String, Object> emailData : emails) {
                if (Boolean.TRUE.equals(emailData.get("primary")) && Boolean.TRUE.equals(emailData.get("verified"))) {
                    return (String) emailData.get("email");
                }
            }
        }
        return null;
    }

    public Long getUserIdFromAccessToken(String accessToken) {
        // Set up headers with the access token for authorization
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        // Make the request
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(
                "https://api.github.com/user",
                HttpMethod.GET,
                entity,
                Map.class
        );

        // Extract the userId from the response
        Map<String, Object> userAttributes = response.getBody();
        if (userAttributes != null && userAttributes.containsKey("id")) {
            return ((Number) userAttributes.get("id")).longValue(); // GitHub ID as a Long
        }

        throw new RuntimeException("Unable to retrieve user ID from GitHub");
    }
}

