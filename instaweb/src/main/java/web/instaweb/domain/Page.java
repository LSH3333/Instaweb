package web.instaweb.domain;

import lombok.Getter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

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

    // 작성일
    private LocalDateTime createdTime;


    protected Page() {
    }

    public Page(Long id, String title, String content, LocalDateTime createdTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }

    public void changeAll(Long id, String title, String content, LocalDateTime createdTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }

    public void delete() {

    }

}
