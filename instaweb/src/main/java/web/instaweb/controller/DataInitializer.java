package web.instaweb.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import web.instaweb.domain.Image;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.service.ImageService;
import web.instaweb.service.MemberService;
import web.instaweb.service.PageService;

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

    private byte[] imgFile;

    private void getNoImgFile() throws IOException {
        // Load the image file from the resources/static folder
        Resource resource = new ClassPathResource("static/unityImg" +
                ".png");
        InputStream inputStream = resource.getInputStream();
        imgFile = inputStream.readAllBytes();
        inputStream.close();
    }

    private byte[] noImgFile;

    private void getNoImgFile2() throws IOException {
        // Load the image file from the resources/static folder
        Resource resource = new ClassPathResource("static/no-img" +
                ".png");
        InputStream inputStream = resource.getInputStream();
        noImgFile = inputStream.readAllBytes();
        inputStream.close();
    }

    /**
     * dataCnt 개의 데이터 서버 시작전 미리 저장해놓음
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        getNoImgFile();
        Member member = memberService.registerNewMember("testLoginId", "1234", "testName");

        int dataCnt = 20;
        for(int i = 0; i < dataCnt; i++) {
            MakePage(Integer.toString(i), imgFile, member);
        }

    }

    private void MakePage(String num, byte[] imageBytes, Member member) {
        String title = "This is title of " + num + " Page";
        String content = "Lorem ipsum dolor sit amet consectetur adipisicing elit. Hic cumque obcaecati odit dicta quis facere eos ratione veniam assumenda similique quibusdam rem illo, sit nobis! Repellendus suscipit quae corporis eaque deleniti sapiente quibusdam, eum nulla vel maiores aut eius magnam sed modi assumenda tenetur doloremque maxime iste quisquam rerum rem? Dolores asperiores laudantium aliquam ex ipsum sapiente praesentium minima. Eveniet quibusdam iure beatae sequi facere nostrum error facilis in vel ratione eaque nam aut officia maiores, magnam officiis! Aperiam voluptate corrupti nostrum eius asperiores ab, a voluptates dolorem facilis tenetur quo repellat quidem rem explicabo sapiente aliquid at odio. Provident!";
        Page page = new Page(title, content, LocalDateTime.now());
        page.setMember(member);
        pageService.savePage(page);

        Image image = new Image();
        image.setPage(page);
        image.setImage(imageBytes);
        imageService.saveImage(image);

    }
}
