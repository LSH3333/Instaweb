package web.instaweb.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

@Entity
@Table
@Getter
@Setter // validation 위해서 필요함
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
}
