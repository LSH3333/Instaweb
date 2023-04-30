//package web.instaweb.controller;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.ApplicationArguments;
//import org.springframework.boot.ApplicationRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.core.io.Resource;
//import org.springframework.stereotype.Component;
//import web.instaweb.domain.Image;
//import web.instaweb.domain.Page;
//import web.instaweb.service.ImageService;
//import web.instaweb.service.PageService;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.time.LocalDateTime;
//
//@Component
//public class DataInitializer implements ApplicationRunner {
//
//    @Autowired
//    private PageService pageService;
//    @Autowired
//    private ImageService imageService;
//
//    private byte[] imgFile;
//
//    private void getNoImgFile() throws IOException {
//        // Load the image file from the resources/static folder
//        Resource resource = new ClassPathResource("static/unityImg" +
//                ".png");
//        InputStream inputStream = resource.getInputStream();
//        imgFile = inputStream.readAllBytes();
//        inputStream.close();
//    }
//
//    private byte[] noImgFile;
//
//    private void getNoImgFile2() throws IOException {
//        // Load the image file from the resources/static folder
//        Resource resource = new ClassPathResource("static/no-img" +
//                ".png");
//        InputStream inputStream = resource.getInputStream();
//        noImgFile = inputStream.readAllBytes();
//        inputStream.close();
//    }
//
//    /**
//     * dataCnt 개의 데이터 서버 시작전 미리 저장해놓음
//     */
//    @Override
//    public void run(ApplicationArguments args) throws Exception {
//        getNoImgFile();
//
//        int dataCnt = 50;
//        for(int i = 0; i < dataCnt; i++) {
//            MakePage(Integer.toString(i), imgFile);
//        }
//
//    }
//
//
//
//    private void MakePage(String num, byte[] imageBytes) {
//        Page page = new Page("title " + num, "content " + num, LocalDateTime.now());
//        pageService.savePage(page);
//
//        Image image = new Image();
//        image.setPage(page);
//        image.setImage(imageBytes);
//        imageService.saveImage(image);
//
//    }
//}
