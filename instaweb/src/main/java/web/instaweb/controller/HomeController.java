package web.instaweb.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;

@Controller
public class HomeController {

    /**
     * @param loginMember : Spring 이 제공하는 @SessionAttribute 사용,
     *                    해당 request 의 세션에 로그인 정보 없으면 null
     * @return : 로그인 정보 없으면 "home", 있으면 "loginHome" 뷰 리턴
     */
    @GetMapping("/")
    public String home(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
            Model model) {

        if(loginMember == null) {
            return "home";
        }

        model.addAttribute("member", loginMember);
        return "loginHome";
    }

}
