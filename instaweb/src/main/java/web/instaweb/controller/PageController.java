package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.Image;
import web.instaweb.domain.Page;
import web.instaweb.service.ImageService;
import web.instaweb.service.PageService;

import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final ImageService imageService;

    /**
     * 글 작성 폼
     */
    @GetMapping("/pages/new")
    public String createForm(Model model) {
        model.addAttribute("form", new PageForm());
        return "pages/createPageForm";
    }

    /**
     * 글 작성
     * 페이지폼에서 작성 후 작성 버튼
     */
    @PostMapping("/pages/new")
    public String create(@Valid PageForm form, BindingResult result) throws IOException {
        // 오류 발생 시 글 작성 폼으로 되돌아감
        if (result.hasErrors()) {
            return "pages/createPageForm";
        }

        // 이미지 객체들 먼저 만들고
        List<MultipartFile> files = form.getImages();
        List<Image> images = new ArrayList<>();
        for (MultipartFile file : files) {
            Image image = new Image(file);
            images.add(image);
        }

        // 페이지 객체 만들고
        // id 는 jpa 가 생성하도록함
        Page page = new Page(form.getTitle(), form.getContent(), LocalDateTime.now());
        // 페이지 객체 먼저 영속성 객체로 만든 후 이미지 객체와 연결 (그렇지 않으면 이미지 객체가 페이지 객체의 pk 값 모르는 문제 발생)
        pageService.savePage(page);

        // 페이지와 이미지들 연결 후 레포지토리에 저장
        for (Image image : images) {
            image.setPage(page);
            page.addImage(image);
            imageService.saveImage(image);
        }


        return "redirect:/";
    }

    /**
     * 글 목록
     */
    @GetMapping("/pages")
    public String list(Model model) {
        List<Page> pages = pageService.findAll();
        model.addAttribute("pages", pages);
        return "/pages/pageList";
    }

    /**
     * ajax
     * request 받으면 모든 페이지와 base64 string 으로 인코딩된 이미지를 리턴한다
     */
    @ResponseBody
    @GetMapping("/pages/ajaxReq")
    public Map<String, ?> loadPagesAndImages() {
        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Page> pages = pageService.findAll();
        List<Object> pageListForms = new ArrayList<>();
        for (Page page : pages) {
            PageListForm pageListForm = new PageListForm();
            pageListForm.setId(page.getId());
            pageListForm.setTitle(page.getTitle());
            pageListForm.setContent(page.getContent());
            pageListForms.add(pageListForm);
        }


        // images
        // 각 페이지의 첫번째 이미지(존재한다면)를 base64 로 인코딩 후 리스트에 저장
        List<String> images = new ArrayList<>();
        for (Page page : pages) {
            List<Image> pageImages = page.getImages();
            if (!pageImages.isEmpty()) {
                String base64Image = pageImages.get(0).generateBase64Image();
                images.add(base64Image);
            }
        }

        ret.put("pages", pageListForms);
        ret.put("images", images);

        return ret;
    }

    /**
     *
     * @param beginIdx : 가져올 페이지 시작 인덱스
     * @param cnt : beginIdx 부터 몇개 가져올지
     * @return : beginIdx 부터 cnt 개의 페이지 레포지토리에서 리턴
     */
    @ResponseBody
    @GetMapping("/pages/ajaxReq2")
    public Map<String, ?> loadPagesAndImages2(@RequestParam int beginIdx, @RequestParam int cnt) {
        Map<String, List<?>> ret = new HashMap<>();

        System.out.println("beginIdx :"  + beginIdx + ' ' + "cnt : " + cnt);

        // page
        List<Page> pages = pageService.findRange(beginIdx, cnt);
        List<Object> pageListForms = new ArrayList<>();
        for (Page page : pages) {
            PageListForm pageListForm = new PageListForm();
            pageListForm.setId(page.getId());
            pageListForm.setTitle(page.getTitle());
            pageListForm.setContent(page.getContent());
            pageListForms.add(pageListForm);
        }


        // images
        // 각 페이지의 첫번째 이미지(존재한다면)를 base64 로 인코딩 후 리스트에 저장
        List<String> images = new ArrayList<>();
        for (Page page : pages) {
            List<Image> pageImages = page.getImages();
            if (!pageImages.isEmpty()) {
                String base64Image = pageImages.get(0).generateBase64Image();
                images.add(base64Image);
            }
        }

        System.out.println("pages.size : " + pages.size());
        System.out.println("images.size : " + images.size());

        ret.put("pages", pageListForms);
        ret.put("images", images);

        return ret;
    }

    /**
     * 글 보기
     */
    @GetMapping("/pages/{id}/view")
    public String viewPage(@PathVariable("id") Long pageId, Model model) {
        Page page = pageService.findOne(pageId);
        model.addAttribute("page", page);
        return "pages/pageView";
    }


    /**
     * 글 수정 폼
     * 새로운 폼을 만들어서 "updatePageForm" 에 전달
     *
     * todo : ajax 이용해 이미지 변경->디스플레이 기능 구현 필요
     */
    @GetMapping("/pages/{id}/edit")
    public String updatePageForm(@PathVariable("id") Long id, Model model) {
        Page page = pageService.findOne(id);

        // 1. 뷰에서는 저장된 이미지를 보여줘야함
        // Page 에는 Image 형으로 저장되어있음, 폼은 MultiPartFile 형식 -> 폼에 Image 형으로도 저장할수 있도록 선언

        PageForm form = new PageForm();
        form.setId(page.getId());
        form.setTitle(page.getTitle());
        form.setContent(page.getContent());
        form.setCreatedTime(page.getCreatedTime());
        // 수정을 위해서 PageForm 에는 Image 형으로도 저장 가능
        form.setByteImages(page.getImages());

        model.addAttribute("form", form);
        return "pages/updatePageForm";
    }

    /**
     * 글 수정
     * 전달받은 폼을 기반으로 새로운 Page 객체를 만들고 db 에 저장한다
     * 이 과정에서 변경 감지 후 update 된다
     */
    @PostMapping("/pages/{id}/edit")
    public String updatePage(@PathVariable("id") Long id, @ModelAttribute("form") PageForm form) {

        // 2. 이미지 눌러서 수정할수 있어야함

        // 여기서 form.getCreatedTime() = null
        System.out.println("PageController.updatePage : " + form.getCreatedTime());

        pageService.updatePage(id, form.getTitle(), form.getContent(), form.getCreatedTime());

        return "redirect:/pages";
    }

    /**
     * 글 삭제
     */
    @GetMapping("/pages/{id}/delete")
    public String deletePage(@PathVariable("id") Long id) {
        pageService.deletePage(id);
        return "redirect:/pages";
    }

    /**
     * 이미지 삭제
     * todo : 수정 폼에서 삭제 될수 있도록 변경 필요 
     */
//    @GetMapping("/pages/{imageId}/delete")
//    public String deleteImage(@PathVariable("imageId") Long imageId) {
//        imageService.deleteImage(imageId);
//        return "";
//    }
}
