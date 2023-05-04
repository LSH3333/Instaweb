package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.SessionConst;
import web.instaweb.domain.Image;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.GetImageDto;
import web.instaweb.form.PageForm;
import web.instaweb.form.PageListForm;
import web.instaweb.service.ImageService;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;
import web.instaweb.service.PageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final MemberService memberService;

    // Page 에 이미지 하나도 없는 경우 띄울 이미지
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
     * createPageForm.html 에서 사용자가 선택, 변경한 이미지의 순서를 기억하려면 form 이 서버로 전달되기 전에 ajax request 로 Image 객체 만들어야 하기 때문에
     * createPageForm 으로 넘어가기 전에 Page 객체 미리 만들어서 id 생성해놓고, 생성된 id 값도 PageForm 에 포함시켜서 전달
     */
    @GetMapping("/pages/new")
    public String createForm(Model model, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                             HttpServletRequest request) {
        // 영속성 Member 필요하기 때문에 조회해옴
        Member member = memberService.findOne(loginMember.getId());
        // 글 작성폼에서 ajax request 로 이미지 저장할때 id 가 필요하기 때문에, 아무것도 없는 Page 를 여기서 미리 만든다
        PageForm pageForm = new PageForm();
        Page page = new Page();
        // Member - Page 연결
        page.setMember(member);
        member.addPage(page);
        pageService.savePage(page);
        pageForm.setId(page.getId());


        model.addAttribute("form", pageForm);
        return "pages/createPageForm";
    }

    /**
     * 글 작성
     * 페이지폼에서 작성 후 submit 버튼
     */
    @PostMapping("/pages/new")
    public String create(@Valid @ModelAttribute("form") PageForm form, BindingResult result)  {
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

        pageService.updatePage(form.getId(), form.getTitle(), form.getContent(), LocalDateTime.now());

        return "redirect:/";
    }


    /**
     * createPageForm.html 에서 보낸 ajax request
     * 전달받은 file 로 Image 객체 만들고 Page 와 연관관계 맺음
     * 전달받은 file 은 유저가 선택한 순서 대로 담겨오게끔 클라이언트에서 전달하기 때문에 순서대로 imgIdx 부여 한다
     * @param pageId : page 의 Id
     * @param files : 글 작성폼에서 보낸 유저가 선택한 파일들, 유저가 선택한 순서대로 담겨서 오기 때문에 그냥 순서대로 imgIdx 부여 하면됨
     */
    @PostMapping("/pages/uploadImages")
    public ResponseEntity<String> handleFileUpload(@RequestParam("pageId") String pageId, @RequestParam MultipartFile[] files) {
        System.out.println("handleFileUpload");

        String message = "";
        Page page = pageService.findOne(Long.parseLong(pageId));

        try {
            long imgIdx = 0;
            for (MultipartFile file : files) {
                // Get file bytes
//                byte[] bytes = file.getBytes();
                Image image = new Image(file);
                image.setImgIdx(imgIdx++);
                page.addImage(image);
                image.setPage(page); // image - page 연결
                imageService.saveImage(image);
            }

            message = "Files uploaded successfully!";
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (IOException e) {
            message = "Failed to upload files: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
    }

    // 글 작성 
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 페이지 리스트

    /**
     * {memberId} 의 글 목록
     */
    @GetMapping("{memberId}/pages")
    public String list(Model model, @PathVariable("memberId") Long memberId,
                       @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) throws IOException {

        getNoImgFile();

        // 영속성 Member 필요하기 때문에 조회해옴
        Member member = memberService.findOne(memberId);
        List<Page> pages = member.getPages();

        // 특정 member 의 글목록과 로그인 정보가 다르다면 글쓰기 버튼 안보이 도록 함
        if(loginMember.getId().equals(memberId)) {
            model.addAttribute("loggedIn", true);
        } else {
            model.addAttribute("loggedIn", false);
        }

        model.addAttribute("pages", pages);
        model.addAttribute("memberId", memberId);
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
    public Map<String, ?> loadPagesAndImages(@RequestParam int beginIdx, @RequestParam int cnt, @RequestParam Long memberId) {
        // 로그인 되있는 Member 가 갖는 Page 들
        Member member = memberService.findOne(memberId);
        List<Page> pages = member.getCntPagesFromIdx(beginIdx, cnt);

        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Object> pageListForms = new ArrayList<>();
        for (Page page : pages) {
            PageListForm pageListForm = new PageListForm();
            pageListForm.setId(page.getId());
            pageListForm.setTitle(page.getTitle());
            pageListForm.setContent(page.getContent());
            pageListForm.setMemberId(page.getMember().getId());
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
     * 존재하는 모든 page 의 목록
     */
    @GetMapping("/allPages")
    public String allList(Model model) {
        List<Page> pages = pageService.findAll();
        model.addAttribute("pages", pages);
        return "/pages/allPageList";
    }
    @ResponseBody
    @GetMapping("/allPages/ajaxReq")
    public Map<String, ?> loadAllPagesAndImages(@RequestParam int beginIdx, @RequestParam int cnt)  {

        List<Page> pages = pageService.findRange(beginIdx, cnt);

        Map<String, List<?>> ret = new HashMap<>();

        // page
        List<Object> pageListForms = new ArrayList<>();
        for (Page page : pages) {
            PageListForm pageListForm = new PageListForm();
            pageListForm.setId(page.getId());
            pageListForm.setTitle(page.getTitle());
            pageListForm.setContent(page.getContent());
            pageListForm.setMemberId(page.getMember().getId());
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
    @GetMapping("/{memberId}/pages/{pageId}")
    public String viewPage(@PathVariable("memberId") Long memberId, @PathVariable("pageId") Long pageId, Model model) {
        Page page = pageService.findOne(pageId);
        // page 주인이 member 인지 확인
        if(!page.getMember().getId().equals(memberId)) {
            return "error/showError.html";
        } 
        model.addAttribute("page", page);
        return "pages/pageView";
    }

    // 페이지 리스트
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 글 수정 폼
     * 새로운 폼을 만들어서 "updatePageForm" 에 전달
     */
    @GetMapping("/pages/{id}/edit")
    public String updatePageForm(@PathVariable("id") Long id, Model model,
                                 @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        Page page = pageService.findOne(id);

        // Page 의 주인인 Member 와 현재 로그인된 Member 가 다르다면 글 수정 권리 없음
        if (!Objects.equals(loginMember.getId(), page.getMember().getId())) {
            return "error/showError.html";
        }


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
     * 전달받은 폼을 기반으로 Page update
     * 이 과정에서 변경 감지 후 update 된다
     * Image handleEditImagesRequest() 에서 업데이트 된다
     */
    @PostMapping("/pages/{id}/edit")
    public String updatePage(@PathVariable("id") Long id, @Valid @ModelAttribute("form") PageForm form, BindingResult result,
                             @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        if (result.hasErrors()) {
            return "pages/updatePageForm";
        }
        Long memberId = loginMember.getId();
        pageService.updatePage(id, form.getTitle(), form.getContent(), form.getCreatedTime());

        return "redirect:/" + memberId + "/pages";
    }

    /**
     * updatePageForm.html 에서 전달 받은 요청, Page 에 속한 Image 들을 추가, 수정, 제거 한다
     * @param pageId
     * @param editedImgIdList : Page 에 속한 Image 의 id 리스트
     * @param editedImgSrcList : Image 들의 src, Base64 String 형식으로 전달 받음
     * @return : response entity
     */
    @PostMapping("/pages/editImages")
    public ResponseEntity<String> editImages(@RequestParam String pageId,
                                             @RequestParam("imgId") List<String> editedImgIdList,
                                             @RequestParam("imgSrc") List<String> editedImgSrcList) {

        Page page = pageService.findOne(Long.parseLong(pageId));
        List<Image> existedImages = page.getImages(); // 수정폼 이전 Page 에 존재하던 Image 들

        // 수정폼에 Image 가 하나도 없는 경우
        if(editedImgIdList.get(0).equals("empty")) {
            // 기존에 Page 에 존재하던 모든 이미지들 제거
            for (Image existedImage : existedImages) {
                imageService.deleteImage(existedImage.getId());
            }
        }
        else {
            // 수정폼에서 삭제한 이미지 삭제 처리
            // 수정폼 이전의 이미지들과, 수정폼에서 수정된 이미지들 비교해서, 수정폼 이전의 이미지가 삭제됐는지 판단
            for (Image existedImage : existedImages) {
                // 기존에 존재하던 Image 가 삭제된 경우
                if (isImgDeleted(editedImgIdList, existedImage)) {
                    imageService.deleteImage(existedImage.getId());
                }
            }

            // Page 에 속한 Image 추가, 수정
            int imgIdx = 0;
            for(int i = 0; i < editedImgIdList.size(); i++) {
                String imgId = editedImgIdList.get(i);
                String imgSrc = editedImgSrcList.get(i); // imgSrc 는 base64String

                // 수정 전에는 페이지에 없던 이미지, 새로 생긴 이미지
                if(imgId.equals("added-img")) {
                    System.out.println("added-img");
                    Image image = new Image();
                    image.setImage(java.util.Base64.getDecoder().decode(imgSrc));
                    image.setImgIdx(imgIdx++);
                    page.addImage(image);
                    image.setPage(page);
                    imageService.saveImage(image);
                }
                // 수정 전에도 페이지에 있던 이미지
                else {
                    imageService.updateImage(Long.parseLong((imgId)), imgSrc, imgIdx++);
                }
            }
        }

        String message = "Files uploaded successfully!";
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

    /**
     * @param editedImgIdList : 수정폼에 존재하는 Image 들의 id 리스트
     * @param existedImg : 수정폼 이전에 Page 에 존재한 Image 중 한 개
     * @return : true = existedImg 가 제거됨
     */
    private boolean isImgDeleted(List<String> editedImgIdList, Image existedImg) {
        for (String editImgId : editedImgIdList) {
            if(editImgId.equals("added-img")) continue;
            if(Long.parseLong(editImgId) == existedImg.getId()) {
                return false;
            }
        }
        return true;
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
     * Page 삭제
     */
    @GetMapping("/pages/{id}/delete")
    public String deletePage(@PathVariable("id") Long id, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        Long memberId = memberService.findOne(loginMember.getId()).getId();
        // 현재 로그인된 Member 가 삭제하려는 Page 의 주인이 아니라면 삭제할수 없음
        if (!pageService.findOne(id).getMember().getId().equals(loginMember.getId())) {
            return "error/showError.html";
        }
        pageService.deletePage(id);
        return "redirect:/" + memberId + "/pages";
    }



}
