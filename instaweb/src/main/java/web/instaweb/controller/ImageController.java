//package web.instaweb.controller;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.core.io.ByteArrayResource;
//import org.springframework.core.io.Resource;
//import org.springframework.http.*;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.ModelAndView;
//import web.instaweb.domain.Image;
//import web.instaweb.domain.Page;
//import web.instaweb.service.ImageService;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.ByteArrayInputStream;
//import java.io.InputStream;
//import java.util.Base64;
//import java.util.List;
//
//@Controller
//@RequiredArgsConstructor
//public class ImageController {
//
//    private final ImageService imageService;
//
//
//    /**
//     * 이미지 폼으로
//     */
//    @GetMapping("/images/new")
//    public String create() {
//        return "images/image-form";
//    }
//
//    /**
//     * 이미지 업로드 (db에 byte 형식으로 저장)
//     */
//    @PostMapping("/images/new")
//    public String uploadImage(@RequestParam("image") MultipartFile file) {
//
//        imageService.save(file);
//
//        return "redirect:/";
//    }
//
//    /**
//     * 이미지 목록
//     */
//    @GetMapping("/images")
//    public String list(Model model) {
//        List<Image> images = imageService.findAll();
//        model.addAttribute("images", images);
//        return "/images/imageList";
//    }
//
//    /**
//     * 이미지 출력 (다운로드)
//     *
//     * byte 타입으로 저장된 이미지 가져와서 인코딩 후 모델에 추가
//     */
//    @GetMapping("/images/{id}/download")
//    public ModelAndView downloadImage(@PathVariable Long id, ModelAndView mav) {
//        Image image = imageService.findOne(id);
//        byte[] bytes = image.getImage();
//
//        mav.addObject("img", Base64.getEncoder().encodeToString(bytes));
//        mav.setViewName("images/imageView");
//        return mav;
//    }
//
//
//}
