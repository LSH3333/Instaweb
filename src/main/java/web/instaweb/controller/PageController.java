package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;
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
import web.instaweb.singletonBean.NoImgProvider;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Controller
@RequiredArgsConstructor
public class PageController {

    private final PageService pageService;
    private final ImageService imageService;
    private final MemberService memberService;
    private final NoImgProvider noImgProvider;


    /**
     * 글 작성 폼
     *
     * 비어있는 PageForm 만들어 놓고, 실제 내용은 프론트에서 ajax 로 보내오면 업데이트하는 방식으로 처리
     * 현재 로그인한 Member 가 작성중인 글이 있는 경우에는 해당 내용을 토대로 PageForm 생성, 아닐 경우 새로 생성
     */
    @GetMapping("{memberId}/pages/new")
    public String createForm(Model model, @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                             HttpServletRequest request, @PathVariable("memberId") Long memberId) {
        // 영속성 Member 필요하기 때문에 조회해옴, 여기서 lazy fetch 에 의해 member.pages 는 가져오지 않는다
        Member member = memberService.findOne(loginMember.getId());

        // 글 작성폼에서 ajax request 로 이미지 저장할때 id 가 필요하기 때문에, 아무것도 없는 Page 를 여기서 미리 만든다


        PageForm pageForm;
        // Member 가 작성 중인 Page 가 있는 경우, 가져와서 클라이언트로 보냄
        if(member.getWritingPageId() != null) {
            Page page = pageService.findOne(member.getWritingPageId());
            pageForm = new PageForm(page.getId(), page.getCreatedTime(), page.getTitle(), page.getContent(), member.getWritingPageId());
        }
        // 없는 경우 Page 를 새로 생성
        else {
            // member 와 연관관계 맺은 상태인 page
            Page page = pageService.createPageForMember(member);
            pageForm = new PageForm(page.getId(), page.getCreatedTime(), null, null, null);
        }

        model.addAttribute("form", pageForm);

        return "pages/createPageForm";
    }

    /**
     * 글 작성
     * 페이지폼에서 작성 후 submit 버튼
     *
     * 폼에서 작성된 내용 저장은 ajax로 handleFileUpload() 에서 처리
     * 글 작성 후에는 로그인 맴버의 작성목록 으로 리다이렉트
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
     * 작성 목록
     * {memberId} 를 갖는 Member 의 글 목록
     */
    @GetMapping("{memberId}/pages")
    public String list(Model model, @PathVariable("memberId") Long memberId,
                       @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) throws IOException {

        model.addAttribute("memberId", memberId);
        model.addAttribute("memberName", memberService.findOne(memberId).getName());
        return "pages/pageList";
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
        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.getCntPagesFromIdxInMemberPage(beginIdx, cnt, memberId);
        return wrapPagesAndImages(pagesAndEndIdxDto);
    }

    /**
     * 홈
     * 존재하는 모든 page 의 목록 (login 안해도 볼수있음)
     */
    @GetMapping("/")
    public String home(
            @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
            Model model) {

        if(loginMember == null) {
            return "home";
        }
        model.addAttribute("loginMemberId", loginMember.getId());
        model.addAttribute("member", loginMember);
        return "home";
    }


    /**
     * ajax request from home.html
     * db에 존재하는 모든 Page 10개씩 클라이언트로 보냄 (모든 Member)
     */
    @ResponseBody
    @GetMapping("/allPages/ajaxReq")
    public Map<String, ?> loadAllPagesAndImages(@RequestParam int beginIdx)  {
        int cnt = 10;
        PagesAndEndIdxDto pagesAndEndIdxDto = pageService.findRange(beginIdx, cnt);
        return wrapPagesAndImages(pagesAndEndIdxDto);
    }

    /**
     * loadAllPagesAndImages(), loadPagesAndImages() 에서 사용
     * 클라이언트에서 홈과 작성목록 에서 작성된 page 들을 보여주기 위해서 cnt 개의 page 요청하면
     * 해당하는 page 들 찾아서 Map 에 포장함
     */
    private Map<String, List<?>> wrapPagesAndImages(PagesAndEndIdxDto pagesAndEndIdxDto) {
        Map<String, List<?>> ret = new HashMap<>();

        List<Page> pages = pagesAndEndIdxDto.getRetPages();
        // 다음 요청때 page 에서 탐색 시작할 idx
        int nextBeginIdx = pagesAndEndIdxDto.getEndIdx();


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
            List<Image> pageImages = imageService.findAllImageFromPage(page.getId());
            String base64Image;
            if (!pageImages.isEmpty()) {
                base64Image = pageImages.get(0).generateBase64Image();
            }
            else {
                // 페이지에 이미지가 하나도 없을 경우 "no-img.png" 디스플레이 하도록함
                base64Image = Base64.encodeBase64String(noImgProvider.getNoImgFile());
            }
            images.add(base64Image);
        }

        List<String> nextBeginIdxList = new ArrayList<>();
        nextBeginIdxList.add(Integer.toString(nextBeginIdx));

        ret.put("pages", pageListForms);
        ret.put("images", images);
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

    /**
     * ajax request from pageView.html, updatePageForm.html, createPageForm.html
     * Page 의 저장되있는 내용들 ajax 로 보냄
     */
    @ResponseBody
    @GetMapping("/view/ajaxReq")
    public Map<String,?> viewPageSendData(@RequestParam long pageId) {
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

        // title
        List<String> title = new ArrayList<>();
        title.add(page.getTitle());

        // content
        List<String> content = new ArrayList<>();
        content.add(page.getContent());

        ret.put("title", title);
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

        PageForm form = new PageForm(page.getId(), page.getCreatedTime(), page.getTitle(), null, null);
        // 수정을 위해서 PageForm 에는 Image 형으로도 저장 가능
        form.setByteImages(page.getImages());

        model.addAttribute("form", form);

        return "pages/updatePageForm";
    }


    /**
     * 글 수정
     * 실제 데이터 db 업로드는 ajax req 에 의해 handleFileUpload() 에서 이루어짐
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
