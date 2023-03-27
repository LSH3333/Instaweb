package web.instaweb.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table
@Getter
public class Page {

    @Id @GeneratedValue
    @Column(name = "page_id")
    private Long id;

    // 제목
    private String title;

    // 내용
    private String content;

    // 작성자
    private String writer;

    // 작성일
    private LocalDateTime createdDate;


    public Page(String title, String content, String writer, LocalDateTime createdDate) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.createdDate = createdDate;
    }

}
