package com.pfernand.monitor.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@JsonTypeName(value = "emailParameters")
@JsonTypeInfo(
        include = JsonTypeInfo.As.WRAPPER_OBJECT,
        use = JsonTypeInfo.Id.NAME
)
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Email {
    private String from;
    private String to;
    private String subject;
    private String body;
    @Builder.Default
    private LocalDateTime sentTime = LocalDateTime.now();
}
