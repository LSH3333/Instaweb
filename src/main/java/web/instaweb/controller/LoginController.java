package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;
import web.instaweb.form.LoginForm;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final MemberService memberService;

    /**
     * 로그인
     */
    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm form) {
        return "login/loginForm";
    }

    /**
     * 로그인에 오류 있을시 반려 없을시 로그인 (세션에 등록)
     */
    @PostMapping("/login")
    public String login(@Valid @ModelAttribute("loginForm") LoginForm form,
                        BindingResult bindingResult,
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "login/loginForm";
        }


        Member loginMember = loginService.login(form.getLoginId(), form.getPassword());

        if(loginMember == null) {
            bindingResult.reject("loginFail", "아이디 또는 비밀번호가 맞지 않습니다");
            return "login/loginForm";
        }

        // request 에 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, loginMember);

        // 로그인 성공 -> request 가 온 url 로 되돌아가도록 리다이렉트 처리
        return "redirect:" + redirectURL;
    }

    /**
     * 로그아웃
     * 세션 파기
     */
    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        // false 시 session 없으면 null 반환
        HttpSession session = request.getSession(false);
        if(session != null) {
            session.invalidate(); // 세션 파기
        }

        return "redirect:/"; // 홈 으로 리다이렉트
    }

    /**
     * Guest login
     */
    @GetMapping("/loginGuest")
    public String loginGuest(@RequestParam(defaultValue = "/") String redirectURL, HttpServletRequest request) {
        Member guest = memberService.registerNewGuest();

        // request 에 세션 있으면 있는 세션 반환, 없으면 신규 세션 생성
        HttpSession session = request.getSession();
        session.setAttribute(SessionConst.LOGIN_MEMBER, guest);

        // 로그인 성공 -> request 가 온 url 로 되돌아가도록 리다이렉트 처리
        return "redirect:" + redirectURL;
    }
}
