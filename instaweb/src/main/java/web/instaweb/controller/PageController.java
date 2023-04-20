package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.Image;
import web.instaweb.domain.Page;
import web.instaweb.dto.GetImageDto;
import web.instaweb.service.ImageService;
import web.instaweb.service.PageService;

import javax.validation.Valid;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;

@Controller
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final ImageService imageService;

    private byte[] noImgFile;

    private void getNoImgFile() throws IOException {
        // Load the image file from the resources/static folder
        Resource resource = new ClassPathResource("static/no-img" +
                ".png");
        InputStream inputStream = resource.getInputStream();
        noImgFile = inputStream.readAllBytes();
        inputStream.close();
    }


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
        /**
         * 생성폼에서 이미지를 선택하지 않았다면 form.getImages().get(0).getContentType() = "application/octet-stream" 이 된다
         * 이미지 파일이라면 "image/png" 이런식으로 온다
         * "application/octet-stream" 은 8 비트 단위 binary 라는 의미
         */
        // 생성폼에서 이미지 1개 이상 선택했다면 Image 객체 만듦
        List<Image> images = new ArrayList<>();
        if(!form.getImages().get(0).getContentType().equals("application/octet-stream")){
            List<MultipartFile> files = form.getImages();
            for (MultipartFile file : files) {
                Image image = new Image(file);
                images.add(image);
            }
        }

        // 페이지 객체 만들고
        // id 는 jpa 가 생성하도록함
        Page page = new Page(form.getTitle(), form.getContent(), LocalDateTime.now());
        // 페이지 객체 먼저 영속성 객체로 만든 후 이미지 객체와 연결 (그렇지 않으면 이미지 객체가 페이지 객체의 pk 값 모르는 문제 발생)
        pageService.savePage(page);

        // 페이지와 이미지들 연결 후 레포지토리에 저장
        long imgIdx = 0L; // 선택한 이미지의 순서 저장
        for (Image image : images) {
            image.setPage(page);
            image.setImgIdx(imgIdx++);
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
     *
     * ajax, request from pageList.html
     * request 받으면 모든 페이지와 base64 string 으로 인코딩된 이미지를 리턴한다
     * @param beginIdx : 가져올 페이지 시작 인덱스
     * @param cnt : beginIdx 부터 몇개 가져올지
     * @return : beginIdx 부터 cnt 개의 페이지 레포지토리에서 리턴
     */
    @ResponseBody
    @GetMapping("/pages/ajaxReq")
    public Map<String, ?> loadPagesAndImages(@RequestParam int beginIdx, @RequestParam int cnt) throws IOException {
        Map<String, List<?>> ret = new HashMap<>();
        getNoImgFile();

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
            String base64Image;
            if (!pageImages.isEmpty()) {
                base64Image = pageImages.get(0).generateBase64Image();
            }
            else {
                // 페이지에 이미지가 하나도 없을 경우 "no-img.png" 디스플레이 하도록함
                base64Image = Base64.encodeBase64String(noImgFile);
            }
            images.add(base64Image);
        }

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
     * Image 는 editImages() 에서 업데이트 된다
     */
    @PostMapping("/pages/{id}/edit")
    public String updatePage(@PathVariable("id") Long id, @ModelAttribute("form") PageForm form) {

        // 여기서 createdTime 의 second 부분 소실됨 
        System.out.println("PageController.updatePage : " + form.getCreatedTime());

        pageService.updatePage(id, form.getTitle(), form.getContent(), form.getCreatedTime());

        return "redirect:/pages";
    }

    /**
     * updatePageForm.html 에서 submit 누를시 XmlHttpRequest 가 보내온 수정된 이미지들 정보
     * 디비에 반영
     */
//    @ResponseBody
//    @PostMapping("/pages/editImages")
//    public void editImages(@RequestBody String data) throws JsonProcessingException {
//
//        // JsonString 으로 온 data 를 iterate 하면서 key, value 처리
//        ObjectMapper objectMapper = new ObjectMapper();
//        // data 는 JSON 형식으로 전달 받았음 {id : imgSrc}
//        JsonNode jsonNode = objectMapper.readTree(data);
//
//        if (jsonNode.isObject()) {
//            Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields();
//            while (fields.hasNext()) {
//                Map.Entry<String, JsonNode> entry = fields.next();
//                String id = entry.getKey(); // key
//                String image = entry.getValue().asText(); // value
//
//                // 수정폼에서 이미지가 제거된 상태라면 image src = 'deleted' 로 받음
//                System.out.println("id = " + id + "," + "image src = " + image);
//                if (image.equals("deleted")) {
//                    imageService.deleteImage(Long.parseLong(id));
//                }
//                else {
//                    imageService.updateImage(Long.parseLong(id), image);
//                }
//            }
//        }
//    }

    @PostMapping("/pages/editImages")
    public ResponseEntity<String> handleEditImagesRequest(@RequestBody Map<String, Object> jsonData) {
        // Do something with the received JSON data
        for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
            String id = entry.getKey();
            Object imageDataObj = entry.getValue();
            // Check if the image data is an array
            if (imageDataObj instanceof List) {
                List<Object> imageDataList = (List<Object>) imageDataObj;
                System.out.println("imageDataList.size() = " + imageDataList.size());
                // Extract the image data and order from the array
                String imageData = (String) imageDataList.get(0);
                String imgIdx = (String) imageDataList.get(1);
                System.out.println("handleEditImagesRequest \n" + id + '\n' + imageData + '\n' + imgIdx);

                // 삭제된 이미지
                if (imageData.equals("deleted")) {
                    imageService.deleteImage(Long.parseLong(id));
                }
                else {
                    imageService.updateImage(Long.parseLong(id), imageData, Long.parseLong(imgIdx));
                }
            } else {
                System.out.println("not instance of List");
            }
        }
        // Return a response indicating success
        return ResponseEntity.ok("Edit Images Request Handled Successfully");
    }


    /**
     * 글 수정 폼(updatePageForm.html) 에서  요청
     * 특정 id 페이지에 저장된 모든 이미지들 리턴
     * 순환참조 오류 때문에 GetImageDto 로 반환
     */
    @ResponseBody
    @GetMapping("/pages/requestImages")
    public List<GetImageDto> requestImages(@RequestParam Long id) {
        Page page = pageService.findOne(id);
        List<GetImageDto> imageDtoList = new ArrayList<>();
        List<Image> images = page.getImages();
        for (Image image : images) {
            GetImageDto getImageDto = new GetImageDto(image);
            imageDtoList.add(getImageDto);
        }
        return imageDtoList;
    }



    /**
     * 글 삭제
     */
    @GetMapping("/pages/{id}/delete")
    public String deletePage(@PathVariable("id") Long id) {
        pageService.deletePage(id);
        return "redirect:/pages";
    }



}
