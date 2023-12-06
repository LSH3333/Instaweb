package web.instaweb.service.OAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import web.instaweb.domain.Member;
import web.instaweb.dto.OAuth.OAuthUserInfoDto;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;

import javax.servlet.http.HttpServletRequest;

@Service
public abstract class OAuthLoginService implements OAuthService {

    private final MemberService memberService;
    private final LoginService loginService;

    @Autowired
    public OAuthLoginService(MemberService memberService, LoginService loginService) {
        this.memberService = memberService;
        this.loginService = loginService;
    }

    /**
     * OAuthGoogleService, OAuthKakaoService 등에서 로그인 로직은 동일하기 때문에 여기서 구현
     * 하지만 loginID, userName 을 얻는 방식은 서로 다르기 때문에 자식 클래스에서 구현
     */
    public Member login(OAuthUserInfoDto oAuthUserInfoDto, HttpServletRequest request) {
        String loginID = getLoginId(oAuthUserInfoDto);
        String userName = getUserName(oAuthUserInfoDto);

        // 존재하지 않는 맴버 -> 회원가입 진행 -> 로그인 진행
        if(!memberService.checkLoginIdDuplication(loginID)) {
            // 회원가입 진행
            Member registeredMember = memberService.registerNewMember(loginID, loginID, userName);
            // 로그인 진행 성공
            if (loginService.loginOAuth(registeredMember)) {
                loginService.regSession(request, registeredMember);
                return registeredMember;
            }
        }
        // 존재하는 맴버 -> 로그인 진행
        else {
            Member member = memberService.getMemberWithLoginId(loginID);
            // 로그인 진행 성공
            if (loginService.loginOAuth(member)) {
                loginService.regSession(request, member);
                return member;
            }
        }
        return null;
    }

}
