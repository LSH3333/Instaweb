package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Member;
import web.instaweb.repository.MemberRepository;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class LoginService {

    private final MemberRepository memberRepository;

    // 일치시 일치하는 Member 리턴, else null 리턴
    public Member login(String loginId, String password) {
        return memberRepository.findByLoginId(loginId)
                .filter(m -> m.getPassword().equals(password))
                .orElse(null);
    }

}
