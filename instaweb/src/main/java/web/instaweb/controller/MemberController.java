package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import web.instaweb.domain.Member;
import web.instaweb.repository.MemberRepository;

import javax.validation.Valid;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/members/register")
    public String registerForm(@ModelAttribute("member") Member member) {
        return "/members/registerForm";
    }

    @PostMapping("members/register")
    public String register(@Valid @ModelAttribute("member") Member member, BindingResult result) {
        if (result.hasErrors()) {
            System.out.println("member hasErrors");
            List<ObjectError> allErrors = result.getAllErrors();
            for (ObjectError allError : allErrors) {
                System.out.println(allError.getDefaultMessage());
            }
            return "/members/registerForm";
        }

        System.out.println(member.getName() + " " + member.getLoginId() + " " + member.getPassword());

        memberRepository.save(member);
        return "redirect:/";
    }

}
