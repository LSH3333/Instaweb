package web.instaweb.domain;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 이미지
    @OneToMany(mappedBy = "page")
    private List<Image> images = new ArrayList<>();


    protected Page() {
    }

    public Page(Long id, String title, String content, LocalDateTime createdTime, List<MultipartFile> files) throws IOException {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;

        for(MultipartFile file : files) {
            addImage(file);
        }
    }

    public void changeAll(Long id, String title, String content, LocalDateTime createdTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }

    private void addImage(MultipartFile file) throws IOException {
        Image image = new Image(file, this);
        images.add(image);
    }


    public void delete() {

    }

}
