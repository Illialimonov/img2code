package com.example.ilia.aidemo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {
    private String clientId;
    private String credentials;
}
