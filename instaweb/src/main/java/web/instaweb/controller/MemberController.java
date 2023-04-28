package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import web.instaweb.domain.Member;
import web.instaweb.service.MemberService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class MemberController {

    MemberService memberService;

    @GetMapping("/members/register")
    public String registerForm(@ModelAttribute("member") Member member) {
        return "/members/registerForm";
    }

    @PostMapping("members/register")
    public String register(@Valid @ModelAttribute("member") Member member, BindingResult result) {
        if (result.hasErrors()) {
            return "/members/registerForm";
        }

        memberService.saveMember(member);
        return "redirect:/";
    }

}
