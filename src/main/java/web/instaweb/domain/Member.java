package web.instaweb.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.dto.PagesAndEndIdxDto;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table
@Getter
@Setter // validation 위해서 필요?
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @NotEmpty(message = "loginId 필수입니다")
    private String loginId;
    @NotEmpty(message = "name 필수입니다")
    private String name;
    @NotEmpty(message = "password 필수입니다")
    private String password;

    // Member 가 갖고 있는 Page 들 리스트
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @OrderBy("createdTime desc") // Page 의 생성 시간 기준 오름차순으로 저장
    private List<Page> pages = new ArrayList<>();

    // Member 가 갖고 있는 Comment 들 리스트
    @OneToMany(mappedBy = "member", fetch = FetchType.LAZY)
    @OrderBy("createdTime desc") // Comment 의 생성 시간 기준 오름차순으로 저장
    private List<Comment> comments = new ArrayList<>();

    // Member 가 작성중인 (작성 완료 하지 않은) Page 의 id, 작성중인 page 없다면 null
    private Long writingPageId;


    public void addPage(Page page) {
        pages.add(page);
    }

    public void addComment(Comment comment) {comments.add(comment);}



    /**
     * 이 member 가 작성한 page 들 중 beginIdx 부터 cnt 개 찾는다
     * @param beginIdx
     * @param cnt
     * @return : {다음에 찾기 시작할 idx, 찾은 pages 들}
     */
//    public PagesAndEndIdxDto getCntPagesFromIdx(int beginIdx, int cnt) {
//        List<Page> retPages = new ArrayList<>();
//        int i;
//        for(i = beginIdx; (i < beginIdx+cnt) && (i < pages.size()) ; i++) {
//            if(pages.get(i).getWritingDone()) {
//                retPages.add(pages.get(i));
//            }
//        }
//        return new PagesAndEndIdxDto(i, retPages);
//    }
}
