package web.instaweb.form;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

// pageList, allPageList 에서 Page 들 리스트 보여줄때 데이터 넘길 폼
@Getter @Setter
public class PageListForm {

    private Long id;

    // 제목
    private String title;

    // 내용
    private String content;

    // Page 의 주인인 member 의 id
    private Long memberId;

    private LocalDateTime createdTime;
}
