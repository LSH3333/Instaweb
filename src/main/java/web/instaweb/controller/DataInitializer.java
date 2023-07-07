package web.instaweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import web.instaweb.SessionConst;
import web.instaweb.domain.Image;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.service.ImageService;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;
import web.instaweb.service.PageService;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private PageService pageService;
    @Autowired
    private ImageService imageService;
    @Autowired
    private MemberService memberService;
    @Autowired
    private LoginService loginService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // "aaaa" member 임의 등록
        memberService.registerNewMember("aaaa", "aaaa", "aaaa");
        Member loginMember = loginService.login("aaaa", "aaaa");

    }

//    private void MakePage(String num, byte[] imageBytes, Member member) {
//        String title = "This is title of " + num + " Page";
//        String content = "Lorem ipsum dolor sit amet consectetur adipisicing elit. Hic cumque obcaecati odit dicta quis facere eos ratione veniam assumenda similique quibusdam rem illo, sit nobis! Repellendus suscipit quae corporis eaque deleniti sapiente quibusdam, eum nulla vel maiores aut eius magnam sed modi assumenda tenetur doloremque maxime iste quisquam rerum rem? Dolores asperiores laudantium aliquam ex ipsum sapiente praesentium minima. Eveniet quibusdam iure beatae sequi facere nostrum error facilis in vel ratione eaque nam aut officia maiores, magnam officiis! Aperiam voluptate corrupti nostrum eius asperiores ab, a voluptates dolorem facilis tenetur quo repellat quidem rem explicabo sapiente aliquid at odio. Provident!";
//        Page page = new Page(title, content, LocalDateTime.now(), true);
//        page.setMember(member);
//        pageService.savePage(page);
//
//        Image image = new Image();
//        image.setPage(page);
//        image.setImage(imageBytes);
//        imageService.saveImage(image);
//
//    }
}
