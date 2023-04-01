package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.service.ImageService;

@Controller
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;


    @GetMapping("/images/new")
    public String create(@ModelAttribute ImageForm form) {
        return "images/image-form";
    }

    @PostMapping("/images/new")
    public String uploadImage(@RequestParam("image") MultipartFile file) {

        imageService.save(file);

        return "redirect:/";
    }


}
