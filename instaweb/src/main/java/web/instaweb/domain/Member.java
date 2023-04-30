package web.instaweb.domain;

import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

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

    // todo : loginId, name 중복 불가능하도록
    @NotEmpty(message = "loginId 필수입니다")
    private String loginId;
    @NotEmpty(message = "name 필수입니다")
    private String name;
    @NotEmpty(message = "password 필수입니다")
    private String password;

    // Member 가 갖고 있는 Page 들 리스트
    @OneToMany(mappedBy = "member")
    private List<Page> pages = new ArrayList<>();


    public void addPage(Page page) {
        pages.add(page);
    }
}
