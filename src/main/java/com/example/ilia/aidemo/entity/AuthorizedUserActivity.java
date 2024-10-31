package com.example.ilia.aidemo.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "authorizedUserActivity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizedUserActivity {

    @Id
    String conversionId;

    @NotNull
    String userId;
    @NotNull
    String fileUrl;
    @NotNull
    String codeUrl;
    @NotNull
    String codeLanguage;

}
