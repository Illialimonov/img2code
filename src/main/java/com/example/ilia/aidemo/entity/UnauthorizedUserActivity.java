package com.example.ilia.aidemo.entity;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "unauthorizedUserActivity")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnauthorizedUserActivity {

    @Id
    String id;
    @NotNull
    String fingerprint;
    @NotNull
    LocalDateTime localDateTime;

}
