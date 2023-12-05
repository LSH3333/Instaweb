package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;
import web.instaweb.repository.MemberRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

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

    public boolean loginOAuth(Member member) {
        Member loginMember = memberRepository.findByLoginId(member.getLoginId())
                .filter(m -> m.getPassword().equals(member.getPassword()))
                .orElse(null);

        return loginMember != null;
    }

    // request 에 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
    public void regSession(HttpServletRequest request, Member member) {
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, member);
    }

}
