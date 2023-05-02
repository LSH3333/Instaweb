package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import web.instaweb.domain.Member;
import web.instaweb.repository.MemberRepository;
import web.instaweb.service.MemberService;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/members/register")
    public String registerForm(@ModelAttribute("member") Member member) {
        return "/members/registerForm";
    }

    @PostMapping("members/register")
    public String register(@Valid @ModelAttribute("member") Member member, BindingResult bindingResult) {
        // member loginId, name 중복체크 로직
        if (memberService.checkLoginIdDuplication(member)) {
            bindingResult.addError(new FieldError("member", "loginId", member.getLoginId(), false, null, null, "이미 존재하는 loginId 입니다."));
        }
        if (memberService.checkNameDuplication(member)) {
            bindingResult.addError(new FieldError("member", "name", member.getName(), false, null, null,"이미 존재하는 name 입니다."));
        }

        if (bindingResult.hasErrors()) {
            return "/members/registerForm";
        }

        memberService.saveMember(member);
        return "redirect:/";
    }

}
