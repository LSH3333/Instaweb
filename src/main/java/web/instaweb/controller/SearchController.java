package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.instaweb.domain.Page;
import web.instaweb.service.PageService;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class SearchController {

    private final PageService pageService;

    @PostMapping("/search/searchAll")
    public ResponseEntity<String> searchAll(@RequestParam("searchQuery") String searchQuery) {
        System.out.println("searchAll");
        System.out.println("searchQuery = " + searchQuery);

        List<Page> foundPages = searchFromAllMember(searchQuery);
        for (Page foundPage : foundPages) {
            System.out.println(foundPage.getTitle());
        }

        return null;
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
            if (page.getTitle().contains(searchQuery) || page.getContent().contains(searchQuery)) {
                foundPages.add(page);
            }
        }

        return foundPages;
    }
}
