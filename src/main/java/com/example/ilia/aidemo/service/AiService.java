package com.example.ilia.aidemo.service;

import com.example.ilia.aidemo.entity.UnauthorizedUserActivity;
import com.example.ilia.aidemo.entity.User;
import com.example.ilia.aidemo.entity.AuthorizedUserActivity;
import com.example.ilia.aidemo.payload.AiResponse;
import com.example.ilia.aidemo.payment.PaymentService;
import com.example.ilia.aidemo.repository.AuthorizedUserActivityRepository;
import com.example.ilia.aidemo.repository.UnauthorizedUserActivityRepository;
import com.example.ilia.aidemo.repository.UserRepository;
import com.example.ilia.aidemo.util.*;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.*;
import com.stripe.exception.StripeException;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class AiService {
    private final GoogleStorageService storageService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthorizedUserActivityRepository authorizedUserActivityRepository;
    private final UnauthorizedUserActivityRepository unauthorizeduserActivityRepository;
    private final PaymentService paymentService;


    public AiResponse photoToCodeFROMURL(MultipartFile url, Optional<String> comments, Optional<String> fingerprint, Authentication authentication) throws IOException, StripeException, InterruptedException {
        boolean ifComments = false;
        AiResponse aiResponse = new AiResponse();
        double fileSizeInMB = (double) url.getSize() / (1024 * 1024);


        String originalFileName = storageService.uploadFileToGCS(url);

        System.out.println(comments);
        if(comments.isPresent()) ifComments = comments.get().equalsIgnoreCase("1");

        String prompt = "Analyze the provided image and convert any code present into actual programming code with correct formatting." + "For the very first sentence of your output, put the name of the detected programming language as follows 'language: Java (for example)'" + " If no recognizable code is found, return the message: 'No code was recognized.'" + " Ensure that the output matches the structure, syntax, and style of the identified programming language.";
        if (ifComments) prompt += ". Also provide in-code comments for the code";

        // Create Vertex AI client with credentials
        try (VertexAI vertexAI = new VertexAI("double-vehicle-437122-a2", "us-central1")) {
            GenerativeModel model = new GenerativeModel("gemini-1.5-flash-002", vertexAI);
            GenerateContentResponse response = model.generateContent(ContentMaker.fromMultiModalData(PartMaker.fromMimeTypeAndData("image/png", urlToByteArray(originalFileName)), prompt));

            String output = ResponseHandler.getText(response);
            output = croppedOutput(output);
            String language = extractLanguage(output);

            //authorized
            if (authentication != null && authentication.isAuthenticated()) {
                User user = userService.getCurrentUser();
                //update db accordingly to the status
                Subscription subscription = paymentService.getSubscriptionFromJWT();

                handleSubscriptionStatus(subscription, fileSizeInMB, ifComments);

                if (user.getCredits() >= 3) {
                    user.setCredits(user.getCredits() - 3);
                    userRepository.save(user);
                    String file_url = convertToURLEncoded(originalFileName);
                    String code_url = convertToURLEncoded(storageService.uploadStringToGCS(output, System.currentTimeMillis() + "_code"));
                    AuthorizedUserActivity authorizedUserActivity = new AuthorizedUserActivity();
                    authorizedUserActivity.setUserId(user.getId());
                    authorizedUserActivity.setCodeLanguage(language);
                    authorizedUserActivity.setCodeUrl(code_url);
                    authorizedUserActivity.setFileUrl(file_url);


                    authorizedUserActivityRepository.save(authorizedUserActivity);
                } else {
                    throw new InsufficientCreditsException("You do not have enough credits");
                }

                //unauthorized
            } else {
                System.out.println("unauthorized user");
                //TODO check if more than x conversions already?
                if (fingerprint.isEmpty()) {
                    throw new FingerprintNotProvidedException("Fingerprint should be provided for the unauthorized users");
                }

                if (getDailyUnauthorizedActivitiesByVisitorID(fingerprint.get()) >= 5) {
                    throw new ExceededFreeConversionsException("Error: Free Trial Limit Reached. It seems you have exhausted your free trial credits. To continue enjoying our services, you have two options: 1. Register Now: Sign up to receive 10 additional credits for free and continue using our features! 2. Subscribe: Choose a subscription plan to unlock unlimited conversions and gain access to premium features. Don’t miss out! Register Now or Subscribe Here");
                }

                UnauthorizedUserActivity unauthorizedUserActivity = new UnauthorizedUserActivity();
                unauthorizedUserActivity.setFingerprint(fingerprint.get());
                unauthorizedUserActivity.setLocalDateTime(LocalDateTime.now());
                unauthorizeduserActivityRepository.save(unauthorizedUserActivity);
                storageService.deleteImageFromBucket(originalFileName);
                log.info("The photo was deleted from the bucket.");
            }



            aiResponse.setCommentsOn(ifComments);
            aiResponse.setOutput(removeFirstTwoLines(output));
            aiResponse.setLanguage(language);


            // Process unrecognized content
            if (language.equals("Language not found") && authentication != null && authentication.isAuthenticated()) {
                User user = userService.getCurrentUser();
                user.setCredits(user.getCredits() + 3);
                userRepository.save(user);
                System.out.println("Your credits were returned ");
                throw new LanguageNotRecognizedError("Programming Language was not recognized. Your credits were returned");
            }

            return aiResponse;
        }
    }

    private String croppedOutput(String input) {
        String target = "```";
        int lastIndex = input.lastIndexOf(target);
        if (lastIndex == -1) {
            return input; // No triple backticks found
        }
        return input.substring(0, lastIndex) + input.substring(lastIndex + target.length());
    }


    private void handleSubscriptionStatus(Subscription subscription, double fileSizeInMB, boolean ifComments) throws StripeException, InterruptedException {
        String planName;
        if (subscription==null) {
            planName = "Free";
        } else {
            String productId = subscription.getItems().getData().get(0).getPrice().getProduct();
            planName = Product.retrieve(productId).getMetadata().get("name");
        }



        //TODO in db do not set tier manually, but rather extract it from the subscription

        //Handle file sizes
        if (planName.equals("Free") && fileSizeInMB > 2){
            throw new FileSizeTooLargeException("Free tier does not allow to upload files larger than 2 MB! Attached file size is: " + fileSizeInMB);
        } else if (planName.equals("Premium") && fileSizeInMB > 5) {
            throw new FileSizeTooLargeException("Premium tier does not allow to upload files larger than 5 MB! Attached file size is: " + fileSizeInMB);
        } else if (planName.equals("Pro") && fileSizeInMB > 10) {
            throw new FileSizeTooLargeException("Pro tier does not allow to upload files larger than 10 MB! Attached file size is: " + fileSizeInMB);
        }

        //Handle ifComments
        if (planName.equals("Free") && ifComments) {
            throw new FeatureIsNotAllowedException("The in-code comments feature is available exclusively to higher-tier users");
        }

        if (planName.equals("Free")) {
            Thread.sleep(13000);
        }

        if (planName.equals("Premium")) {
            System.out.println("are waiting here?");
            Thread.sleep(5000);
        }
    }

    public Map<String, String> getRemainingConversions(String visitorId) {
        HashMap<String, String> result = new HashMap<>();
        String count = String.valueOf(5 - getDailyUnauthorizedActivitiesByVisitorID(visitorId));
        result.put("conversionsLeft", count);
        return result;
    }

    private int getDailyUnauthorizedActivitiesByVisitorID(String fingerprint) {

        LocalDateTime now = LocalDateTime.now();

        LocalDateTime todayBeginning = now.withHour(0).withMinute(0).withSecond(0).withNano(0);

        LocalDateTime todayEnding = now.withHour(23).withMinute(59).withSecond(59);


        return unauthorizeduserActivityRepository.countByLocalDateTimeBetweenAndFingerprint(todayBeginning, todayEnding, fingerprint);
    }

    private String convertToURLEncoded(String s) {
        String url = URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
        return "https://storage.googleapis.com/photo2code_codes/" + url;
    }

    public String extractLanguage(String code) {
        // Split the code into lines
        String[] lines = code.split("\n");

        // Check the first line for the language
        if (lines.length > 0) {
            String firstLine = lines[0].trim();
            if (firstLine.startsWith("language:")) {
                return firstLine.substring(9).trim(); // Extract and return the language
            }
        }

        return "Language not found"; // Default message if language is not specified
    }

    private static String removeFirstTwoLines(String input) {
        // Split the input string into lines
        String[] lines = input.split("\n");

        // Check if there are at least 3 lines
        if (lines.length <= 2) {
            return ""; // Return an empty string if there are 2 or fewer lines
        }

        // Create a StringBuilder to hold the remaining lines
        StringBuilder result = new StringBuilder();

        // Append lines from the 3rd line to the end
        for (int i = 3; i < lines.length; i++) {
            result.append(lines[i]);
            // Append a newline if it's not the last line
            if (i < lines.length - 1) {
                result.append("\n");
            }
        }

        return result.toString();
    }

    public byte[] urlToByteArray(String imageUrl) throws IOException {
        imageUrl = convertToURLEncoded(imageUrl);
        URL url = new URL(imageUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setDoOutput(true);

        // Check if the response code is HTTP_OK (200)
        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to fetch image: " + connection.getResponseCode());
        }

        // Read the image into a byte array
        try (InputStream inputStream = connection.getInputStream(); ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }


}
