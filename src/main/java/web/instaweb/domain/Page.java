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
    // content 는 html element 들, element 의 text 영역이 모두 포함되서 텍스트 형태로 저장된다.
    // <img> 의 경우, 이미지는 따로 저장되기 때문에 각 <img> 는 고유한 uuid 값이 있고, src 는 제거되서 서버로 온다.
    // 예: <img id="7dcbd0e3-2240-4fc4-b097-8b5bd159cb35" src="blob:https://lsh-instaweb.herokuapp.com/6bf7aa1c-ac57-4bfc-85a1-abc41705aef1">
    // src 는 서버에서 불필요하기 때문에 제거되서 서버로 온다.
    // 클라이언트에서 Page 를 랜더링할때는 <img> 의 id 값을 참고해서 그에 맞는 이미지를 삽입한다
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

    @OneToMany(mappedBy = "page", orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("createdTime desc") // Comment 의 생성 시간 기준 오름차순으로 저장
    private List<Comment> comments = new ArrayList<>();

    private boolean writingDone;

    public boolean getWritingDone() {
        return this.writingDone;
    }

    public Page() {
    }

    public Page(LocalDateTime createdTime)  {
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

    public void addComment(Comment comment) {
        comments.add(comment);
    }



}
