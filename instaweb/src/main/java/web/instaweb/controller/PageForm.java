package web.instaweb.controller;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter @Setter
public class PageForm {
    private Long id;

    // 제목
    @NotEmpty(message = "제목은 필수입니다")
    private String title;

    // 내용
    private String content;

    // 시간
    private LocalDateTime createdTime;
}
