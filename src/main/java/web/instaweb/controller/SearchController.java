package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import web.instaweb.SessionConst;
import web.instaweb.domain.Image;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;
import web.instaweb.form.PageListForm;
import web.instaweb.service.ImageService;
import web.instaweb.service.PageService;
import web.instaweb.singletonBean.NoImgProvider;

import javax.servlet.http.HttpServletRequest;
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
     * 모든 page 대상 검색
     * 엔터 누르면 searchResultList 로 navigate
     */
    @GetMapping("/search/resultList")
    public String searchResultList() {
        return "search/searchResultList";
    }

    /**
     * 로그인한 (나의) member 의 글 대상 검색
     */
    @GetMapping("/search/resultMineList")
    public String searchResultMineList() {
        return "search/searchResultAllList";
    }


    /**
     * 모든 Member 의 모든 Page 대상 검색
     * searchQuery 문자열을 포함하는 page.title or page.content 를 갖는 page 를 beginIdx 부터 리턴함
     */
    @ResponseBody
    @GetMapping("/search/searchAll")
    public Map<String,?> searchAll(@RequestParam int beginIdx, @RequestParam("searchQuery") String searchQuery) {

        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.findSearchQuery(beginIdx, 10, searchQuery);
        List<Page> foundPages = pagesAndEndIdxDto.getRetPages();

        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Object> pageListForms = new ArrayList<>();

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
     * 로그인한 Member 의 Page 대상 검색
     */
    @ResponseBody
    @GetMapping("/search/searchMine")
    public Map<String,?> searchMine(@RequestParam int beginIdx, @RequestParam("searchQuery") String searchQuery,
                                    @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        Long memberId = loginMember.getId();

        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.findMineSearchQuery(beginIdx, 10, searchQuery, memberId);
        List<Page> foundPages = pagesAndEndIdxDto.getRetPages();

        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Object> pageListForms = new ArrayList<>();

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
    private List<Page> searchFromAllMember(String searchQuery, int beginIdx) {
        // 모든 Member 들의 모든 Page, 즉 존재하는 모든 Page
        List<Page> allPages = pageService.findAll();
        // query 조건 충족하는 페이지들 담김
        List<Page> foundPages = new ArrayList<>();

        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.findSearchQuery(beginIdx, 10, searchQuery);
        List<Page> pages = pagesAndEndIdxDto.getRetPages();

        // page 의 title, content 중에 searchQuery 포함하면 foundPages 에 담는다
//        for (Page page : allPages) {
//            if(!page.getWritingDone()) continue; // 작성중인 Page 제외
//            if (page.getTitle().contains(searchQuery) || page.getContent().contains(searchQuery)) {
//                foundPages.add(page);
//            }
//        }
//        int i;
//        for(i = beginIdx; i < allPages.size(); i++) {
//            Page page = allPages.get(i);
//            if(!page.getWritingDone()) continue; // 작성중인 Page 제외
//            if (page.getTitle().contains(searchQuery) || page.getContent().contains(searchQuery)) {
//                foundPages.add(page);
//                if(foundPages.size() >= 10) break;
//            }
//        }



        return foundPages;
    }
}
