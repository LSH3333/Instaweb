package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.SessionConst;
import web.instaweb.domain.Image;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;
import web.instaweb.form.PageForm;
import web.instaweb.form.PageListForm;
import web.instaweb.service.ImageService;
import web.instaweb.service.MemberService;
import web.instaweb.service.PageService;

import javax.servlet.http.HttpServletRequest;
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
     *
     * 작성폼에서 유저가 작성한 내용들 저장은 ajax 로 처리, 수정폼이랑 같은 ajax 사용, handleFileUpload() 에서 처리*
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
            page.setCreatedTime(LocalDateTime.now());
        }


        // Member - Page 연결
        page.setMember(member);
        member.addPage(page);
        pageService.savePage(page);
        // member 가 작성중 상태인 page id 기억해놓음
        memberService.setMemberWritingPageId(member.getId(), page.getId());
        PageForm pageForm = new PageForm(page.getId(), page.getCreatedTime());
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

        // Page 작성 성공 시 해당 member 의 writingPageId 는 null 로 되돌림
        Member loginMember = memberService.findOne((Long) request.getAttribute("loginMemberId"));
        memberService.setMemberWritingPageId(loginMember.getId(), null);

        return "redirect:/" + request.getAttribute("loginMemberId") + "/pages";
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
     * ajax request from pageList.html
     * 이 member 가 작성한 page 들 중 beginIdx 부터 cnt 개 찾는다
     *
     * @return : pages = member 에 속한 페이지들, images = 페이지의 첫번째 이미지, nextBeginIdx = 다음 request 에 여기부터 탐색 시작
     */
    @ResponseBody
    @GetMapping("/pages/ajaxReq")
    public Map<String, ?> loadPagesAndImages(@RequestParam int beginIdx, @RequestParam Long memberId) {
        int cnt = 10; // 최대 10개의 Page 가져오기 시도
        System.out.println("loadPagesAndImages = " + beginIdx + ' ' + cnt);
        // 로그인 되있는 Member 가 갖는 Page 들
        Member member = memberService.findOne(memberId);

        PagesAndEndIdxDto pagesAndEndIdxDto = member.getCntPagesFromIdx(beginIdx, cnt);
        // 클라이언트로 보낼 page 목록
        List<Page> pages = pagesAndEndIdxDto.getRetPages();
        // 다음 요청때 page 에서 탐색 시작할 idx
        int nextBeginIdx = pagesAndEndIdxDto.getEndIdx();

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

        List<String> nextBeginIdxList = new ArrayList<>();
        nextBeginIdxList.add(Integer.toString(nextBeginIdx));
        ret.put("nextBeginIdx", nextBeginIdxList);


        return ret;
    }

    /**
     * 존재하는 모든 page 의 목록 (login 안해도 볼수있음)
     */
    @GetMapping("/allPages")
    public String allList(Model model) {
        return "/pages/home";
    }

    @ResponseBody
    @GetMapping("/allPages/ajaxReq")
    public Map<String, ?> loadAllPagesAndImages(@RequestParam int beginIdx)  {
        int cnt = 10;

        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.findRange(beginIdx, cnt);
        List<Page> pages = pagesAndEndIdxDto.getRetPages();
        // 다음 요청때 page 에서 탐색 시작할 idx
        int nextBeginIdx = pagesAndEndIdxDto.getEndIdx();

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

        List<String> nextBeginIdxList = new ArrayList<>();
        nextBeginIdxList.add(Integer.toString(nextBeginIdx));
        ret.put("nextBeginIdx", nextBeginIdxList);

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

    // view 의 내용과 이미지는 ajax 로 받아서 동적으로 디스플레이한다, 수정폼에서도 사용
    @ResponseBody
    @GetMapping("/view/ajaxReq")
    public Map<String,?> viewPageSendData(@RequestParam long pageId) {
        System.out.println("viewPageSendData");
        Map<String, List<?>> ret = new HashMap<>();

        Page page = pageService.findOne(pageId);

        // images, imgIdxList
        List<Image> imageList = page.getImages();
        List<String> images = new ArrayList<>();
        List<String> imgUUIDList = new ArrayList<>();

        for (Image image : imageList) {
            images.add(image.generateBase64Image());
            imgUUIDList.add(image.getUUID());
        }

        // content
        List<String> content = new ArrayList<>();
        content.add(page.getContent());

        ret.put("content", content);
        ret.put("images", images);
        ret.put("imgUUIDList", imgUUIDList);

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

        PageForm form = new PageForm(page.getId(), page.getCreatedTime());
        form.setId(page.getId());
        form.setTitle(page.getTitle());
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

    /**
     * 작성폼, 수정폼에서 데이터 서버로 보내오면 처리함
     */
    @PostMapping("/pages/upload")
    public ResponseEntity<String> handleFileUpload(@RequestParam("pageId") String pageId,
                                                    @RequestParam(value="files", required = false) List<MultipartFile> files,
                                                    @RequestParam(value="uuids", required = false) List<String> uuids,
                                                    @RequestParam("title") String title,
                                                    @RequestParam("content") String content,
                                                    @RequestParam(value="createdTime", required = false) String createdTime,
                                                    @RequestParam("writingDone") boolean writingDone,
                                                    @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        String message = "";
        Page page = pageService.findOne(Long.parseLong(pageId));
        Long memberId = loginMember.getId();
        System.out.println("handleFileUpload");
        System.out.println("writingDone = " + writingDone);
        try {
            // title, content, createdTime 저장

            String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime localDateTime = LocalDateTime.parse(createdTime, formatter);

            pageService.updatePage(Long.parseLong(pageId), title, content, localDateTime, writingDone);

            imageService.deletePagesAllImages(Long.parseLong(pageId));

            // 이미지 파일은 없을수도 있음
            if(files != null) {
                int i = 0;
                for (MultipartFile file : files) {
                    String uuid = uuids.get(i);
                    Image image = new Image(file);
                    image.setImgUUID(uuid);
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
