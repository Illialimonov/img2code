package com.example.ilia.aidemo.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document(collection="password_reset_tokens")
public class PasswordResetTokens {
    @Id
    private String token_id;

    @DBRef
    private User owner;

    private String token;

    private Date expired_at;

}
