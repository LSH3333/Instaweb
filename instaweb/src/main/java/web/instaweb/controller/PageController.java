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
import org.springframework.util.MultiValueMap;
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
import java.time.format.DateTimeFormatter;
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
    @GetMapping("{memberId}/pages/new")
    public String createForm(Model model, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                             HttpServletRequest request, @PathVariable("memberId") Long memberId) {
        // 영속성 Member 필요하기 때문에 조회해옴
        Member member = memberService.findOne(loginMember.getId());

        // 글 작성폼에서 ajax request 로 이미지 저장할때 id 가 필요하기 때문에, 아무것도 없는 Page 를 여기서 미리 만든다

        // Member 가 작성 중인 Page 가 있는 경우
        Page page;
        if(member.getWritingPageId() != null) {
            page = pageService.findOne(member.getWritingPageId());
        } // 없는 경우
        else {
            page = new Page();
        }

        PageForm pageForm = new PageForm();
        // Member - Page 연결
        page.setMember(member);
        member.addPage(page);
        pageService.savePage(page);
        pageForm.setId(page.getId());
        // member 가 작성중 상태인 page id 기억해놓음
        memberService.setMemberWritingPageId(member.getId(), page.getId());
        model.addAttribute("form", pageForm);

        return "pages/createPageForm";
    }

    /**
     * 글 작성
     * 페이지폼에서 작성 후 submit 버튼
     */
    @PostMapping("/pages/new")
    public String create(@Valid @ModelAttribute("form") PageForm form, BindingResult result, HttpServletRequest request)  {
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

        // Page 객체는 미리 만들어져 있고, updatePage 로 처리
        pageService.updatePage(form.getId(), form.getTitle(), form.getContent(), LocalDateTime.now(), true);

        // Page 작성 성공 시 해당 member 의 writingPageId 는 null 로 되돌림
        Member loginMember = memberService.findOne((Long) request.getAttribute("loginMemberId"));
        memberService.setMemberWritingPageId(loginMember.getId(), null);

        return "redirect:/" + request.getAttribute("loginMemberId") + "/pages";
    }


    /**
     * createPageForm.html 에서 보낸 ajax request
     * 전달받은 file 로 Image 객체 만들고 Page 와 연관관계 맺음
     * 전달받은 file 은 유저가 선택한 순서 대로 담겨오게끔 클라이언트에서 전달하기 때문에 순서대로 imgIdx 부여 한다
     * @param pageId : page 의 Id
     * @param files : 글 작성폼에서 보낸 유저가 선택한 파일들, 유저가 선택한 순서대로 담겨서 오기 때문에 그냥 순서대로 imgIdx 부여 하면됨
     *
     */
    @PostMapping("/pages/uploadImages")
    public ResponseEntity<String> handleFileUpload(@RequestParam("pageId") String pageId,
                                                   @RequestParam("files") List<MultipartFile> files,
                                                   @RequestParam("imgIdxList") List<String> imgIdxList) {
        System.out.println("handleFileUpload");
        String message = "";
        Page page = pageService.findOne(Long.parseLong(pageId));


        try {
            int i = 0;
            for (MultipartFile file : files) {
                long imgIdx = Long.parseLong(imgIdxList.get(i));
                Image image = new Image(file);
                image.setImgIdx(imgIdx);
                page.addImage(image);
                image.setPage(page); // image - page 연결
                imageService.saveImage(image);
                i++;
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
            if(!page.getWritingDone()) continue; // 작성중인 Page 제외
            PageListForm pageListForm = new PageListForm(page.getId(), page.getTitle(), page.getContent(), page.getMember().getId(), page.getMember().getName(), page.getCreatedTime());
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
            if(!page.getWritingDone()) continue; // 작성중인 Page 제외
            PageListForm pageListForm = new PageListForm(page.getId(), page.getTitle(), page.getContent(), page.getMember().getId(), page.getMember().getName(), page.getCreatedTime());
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

    // view 의 내용과 이미지는 ajax 로 받아서 동적으로 디스플레이한다
    @ResponseBody
    @GetMapping("/view/ajaxReq")
    public Map<String,?> viewGetImages(@RequestParam long pageId) {
        Map<String, List<?>> ret = new HashMap<>();

        Page page = pageService.findOne(pageId);

        // images, imgIdxList
        List<Image> imageList = page.getImages();
        List<String> images = new ArrayList<>();
        List<Long> imgIdxList = new ArrayList<>();
        for (Image image : imageList) {
            images.add(image.generateBase64Image());
            imgIdxList.add(image.getImgIdx());
        }

        // content
        List<String> content = new ArrayList<>();
        content.add(page.getContent());

        ret.put("content", content);
        ret.put("images", images);
        ret.put("imgIdxList", imgIdxList);

        return ret;
    }


    // 페이지 리스트
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 글 수정

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

        return "redirect:/" + memberId + "/pages";
    }

    // todo : 위의 updatePage 없애버리고 아래 ajaxReq 로 모든 정보 받아서 처리하도록 변경해야함, 기존 방식은 컨텐츠, 이미지 따로 처리하는데 컨텐츠에 오류 있어도 이미지가 그냥 서버로 전달되버림
    @PostMapping("/pages/upload")
    public ResponseEntity<String> handleFileUpload2(@RequestParam("pageId") String pageId,
                                                    @RequestParam(value="files", required = false) List<MultipartFile> files,
                                                    @RequestParam(value="imgIdxList", required = false) List<String> imgIdxList,
                                                    @RequestParam("title") String title,
                                                    @RequestParam("content") String content,
                                                    @RequestParam("createdTime") String createdTime,
                                                    @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {
        System.out.println("handleFileUpload2");

        String message = "";
        Page page = pageService.findOne(Long.parseLong(pageId));
        Long memberId = loginMember.getId();

        try {
            // title, content, createdTime 저장
            // Define the pattern of the input string
            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            // Create a DateTimeFormatter based on the pattern
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            // Parse the string to LocalDateTime using the formatter
            LocalDateTime localDateTime = LocalDateTime.parse(createdTime, formatter);

            pageService.updatePage(Long.parseLong(pageId), title, content, localDateTime, true);
            // 이미지 파일은 없을수도 있음
            if(files != null) {
                int i = 0;
                for (MultipartFile file : files) {
                    long imgIdx = Long.parseLong(imgIdxList.get(i));
                    Image image = new Image(file);
                    image.setImgIdx(imgIdx);
                    page.addImage(image);
                    image.setPage(page); // image - page 연결
                    imageService.saveImage(image);
                    i++;
                }
            }

            message = "Files uploaded successfully!";
            return ResponseEntity.status(HttpStatus.OK).body(message);
        } catch (IOException e) {
            message = "Failed to upload files: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }
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
