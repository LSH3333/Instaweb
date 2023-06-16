package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import web.instaweb.domain.Member;
import web.instaweb.service.MemberService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    /**
     * 회원 가입
     */
    @GetMapping("/members/register")
    public String registerForm(@ModelAttribute("member") Member member) {
        return "members/registerForm";
    }

    /**
     * 회원가입 에서 사용자가 작성 한 내용 검토 후 에러 있다면 반려함
     * 에러 없을시 Member 등록 후 리다이렉트
     */
    @PostMapping("members/register")
    public String register(@Valid @ModelAttribute("member") Member member, BindingResult bindingResult) {
        System.out.println("--register");
        // member loginId, name 중복 체크
        if (memberService.checkLoginIdDuplication(member)) {
            bindingResult.addError(new FieldError("member", "loginId", member.getLoginId(), false, null, null, "이미 존재하는 아이디 입니다."));
        }
        if (memberService.checkNameDuplication(member)) {
            bindingResult.addError(new FieldError("member", "name", member.getName(), false, null, null,"이미 존재하는 이름 입니다."));
        }
        // member loginId, name, password 길이 체크
        if(member.getLoginId().length() < 4 || member.getLoginId().length() > 10) {
            bindingResult.addError(new FieldError("member", "loginId", member.getLoginId(), false, null, null,"아이디는 4글자 이상 10글자 이하여야 합니다."));
        }
        if(member.getName().length() < 3 || member.getName().length() > 10) {
            bindingResult.addError(new FieldError("member", "name", member.getName(), false, null, null,"이름은 3글자 이상 10글자 이하여야 합니다."));
        }
        if(member.getPassword().length() < 4 || member.getLoginId().length() > 10) {
            bindingResult.addError(new FieldError("member", "password", member.getPassword(), false, null, null,"비밀번호는 4글자 이상 10글자 이하여야 합니다."));
        }
        // member loginId, name 은 영어,숫자만 가능
        if(!CheckAllEngOrDigit(member.getLoginId())) {
            bindingResult.addError(new FieldError("member", "loginId", member.getLoginId(), false, null, null, "아이디는 영어,숫자만 가능합니다"));
        }
        if(!CheckAllEngOrDigit(member.getName())) {
            bindingResult.addError(new FieldError("member", "name", member.getName(), false, null, null, "이름은 영어,숫자만 가능합니다"));
        }

        if (bindingResult.hasErrors()) {
            return "members/registerForm";
        }

        memberService.saveMember(member);
        return "redirect:/";
    }

    /**
     * str 에 영어가 아닌 레터가 하나라도 포함되어 있으면 false 리턴
     */
    boolean CheckAllEngOrDigit(String str) {
        for(int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);

            // 영어도 아니고, 숫자도 아니면 return false
            if (Character.UnicodeBlock.of(ch) != Character.UnicodeBlock.BASIC_LATIN && !Character.isDigit(ch)) {
                return false;
            }
        }
        return true;
    }


}
