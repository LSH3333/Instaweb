package web.instaweb.controller;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class PageForm {
    private Long id;

    // 제목
    @NotEmpty(message = "제목은 필수입니다")
    private String title;

    // 내용
    private String content;

    // 이미지 파일은 폼에서는 MultipartFile 로 받아 byte 로 변환해 저장한다
    private List<MultipartFile> images;

    // 시간
    private LocalDateTime createdTime;
}
