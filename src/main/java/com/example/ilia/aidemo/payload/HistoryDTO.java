package com.example.ilia.aidemo.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HistoryDTO {
    String conversionId;
    String user_id;
    String code;
    String code_language;
    String file_url;
}
