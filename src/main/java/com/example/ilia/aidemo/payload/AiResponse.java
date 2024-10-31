package com.example.ilia.aidemo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiResponse {
    private String language;
    private String output;
    private Boolean commentsOn;
}
