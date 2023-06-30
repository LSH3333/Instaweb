package web.instaweb.domain;

import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;
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
//    @Lob
//    @Column(length = 1000)
    @Column(columnDefinition = "TEXT")
    private String content;

    // 작성일
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime createdTime;

    // 이미지
    // orphanRemoval : Image 는 Page 없이 존재할수 없다
    @OneToMany(mappedBy = "page", orphanRemoval = true, fetch = FetchType.LAZY)
    //@OrderBy("imgIdx asc") // Image 의 imgIdx 기준 오름차순
    private List<Image> images = new ArrayList<>();

    // 이 Page 를 소유하는 Member
    // 여기서 ManyToOne은 그냥 EAGER 로 처리, fetch 해봐야 한개이기 때문에
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "member_id")
    private Member member;

    private boolean writingDone;

    public boolean getWritingDone() {
        return this.writingDone;
    }

    public Page() {
    }

    public Page(String title, String content, LocalDateTime createdTime, boolean writingDone)  {
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
    }


    public void changeAll(Long id, String title, String content, LocalDateTime createdTime, boolean writingDone) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdTime = createdTime;
        this.writingDone = writingDone;
    }

    public void addImage(Image image) {
        images.add(image);
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public void setCreatedTime(LocalDateTime time) {this.createdTime = time;}

    public void delete() {}

}
