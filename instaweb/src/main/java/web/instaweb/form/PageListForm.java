package web.instaweb.form;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class PageListForm {

    private Long id;

    // 제목
    private String title;

    // 내용
    private String content;
}
