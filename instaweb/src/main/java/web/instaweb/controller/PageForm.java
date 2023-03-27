package web.instaweb.controller;

import lombok.Getter;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;

@Getter
public class PageForm {
    private Long id;

    // 제목
    @NotEmpty(message = "제목은 필수입니다")
    private String title;

    // 내용
    private String content;

    // 작성자
    @NotEmpty(message = "작성자는 필수입니다")
    private String writer;


}
