package web.instaweb.form;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.Image;

import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class PageForm {
    // Page id
    private Long id;

    // 제목
    @NotEmpty(message = "제목은 필수입니다")
    private String title;

    // 내용
    private String content;

    // 이미지 파일은 폼에서는 MultipartFile 로 받아 byte 로 변환해 저장한다
    private List<MultipartFile> images;
    // 수정 폼을 위해서 Image 형으로도 저장할수 있도록 함
    private List<Image> byteImages;

    // 시간
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdTime;

    private List<String> imageOrder;

}
