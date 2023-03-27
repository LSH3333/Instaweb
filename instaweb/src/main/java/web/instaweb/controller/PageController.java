package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import web.instaweb.domain.Page;
import web.instaweb.service.PageService;

import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;

    /**
     * 글 작성 폼
     */
    @GetMapping("/pages/new")
    public String createForm(Model model) {
        model.addAttribute("pageForm", new PageForm());
        return "pages/createPageForm";
    }

    /**
     * 페이지폼에서 작성 후 작성 버튼
     */
    @PostMapping("/pages/new")
    public String create(@Valid PageForm form, BindingResult result) {
        // 오류 발생 시 글 작성 폼으로 되돌아감
        if (result.hasErrors()) {
            return "pages/createPageForm";
        }




        return "redirect:/";
    }


}
