package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.view.RedirectView;
import web.instaweb.domain.Image;
import web.instaweb.domain.Page;
import web.instaweb.form.PageListForm;
import web.instaweb.service.ImageService;
import web.instaweb.service.PageService;
import web.instaweb.singletonBean.NoImgProvider;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final PageService pageService;
    private final ImageService imageService;
    private final NoImgProvider noImgProvider;



    /**
     * 엔터 누르면 searchResultList 로 navigate
     */
    @GetMapping("/search/resultList")
    public String searchResultList() {
        System.out.println("searchResultList");
        return "search/searchResultList";
    }


    @ResponseBody
    @GetMapping("/search/searchAll")
    public Map<String,?> searchAll(@RequestParam int beginIdx, @RequestParam("searchQuery") String searchQuery) {
        System.out.println("searchAll");
        System.out.println("searchQuery = " + searchQuery);

        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Object> pageListForms = new ArrayList<>();
        List<Page> foundPages = searchFromAllMember(searchQuery);
        for (Page page : foundPages) {
            PageListForm pageListForm = new PageListForm(page.getId(), page.getTitle(), page.getContent(), page.getMember().getId(), page.getMember().getName(), page.getCreatedTime());
            pageListForms.add(pageListForm);
        }

        // images
        // 각 페이지의 첫번째 이미지(존재한다면)를 base64 로 인코딩 후 리스트에 저장
        List<String> images = new ArrayList<>();
        for (Page page : foundPages) {
            List<Image> pageImages = imageService.findAllImageFromPage(page.getId());
            String base64Image;
            if (!pageImages.isEmpty()) {
                base64Image = pageImages.get(0).generateBase64Image();
            }
            else {
                // 페이지에 이미지가 하나도 없을 경우 "no-img.png" 디스플레이 하도록함
                base64Image = Base64.encodeBase64String(noImgProvider.getNoImgFile());
            }
            images.add(base64Image);
        }

        // next begin idx
        List<String> nextBeginIdxList = new ArrayList<>();
        nextBeginIdxList.add(Integer.toString(beginIdx+pageListForms.size()));

        ret.put("pages", pageListForms);
        ret.put("images", images);
        ret.put("nextBeginIdx", nextBeginIdxList);

        return ret;
    }

    /**
     * title,content 에 searchQuery 문자열이 포함되는 page 들 리턴함
     */
    private List<Page> searchFromAllMember(String searchQuery) {
        // 모든 Member 들의 모든 Page, 즉 존재하는 모든 Page
        List<Page> allPages = pageService.findAll();
        // query 조건 충족하는 페이지들 담김
        List<Page> foundPages = new ArrayList<>();

        // page 의 title, content 중에 searchQuery 포함하면 foundPages 에 담는다
        for (Page page : allPages) {
            if(!page.getWritingDone()) continue; // 작성중인 Page 제외
            if (page.getTitle().contains(searchQuery) || page.getContent().contains(searchQuery)) {
                foundPages.add(page);
            }
        }

        return foundPages;
    }
}
