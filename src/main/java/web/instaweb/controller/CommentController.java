package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.instaweb.SessionConst;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;
import web.instaweb.service.CommentService;
import web.instaweb.service.MemberService;
import web.instaweb.service.PageService;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;


@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PageService pageService;
    private final MemberService memberService;

    @PostMapping("/comment/upload")
    public ResponseEntity<String> submit(@RequestParam("pageId") String pageId,
                                         @RequestParam("comment") String commentContent,
                                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        String message = "";

        Comment comment = new Comment(commentContent);
        commentService.save(comment, Long.parseLong(pageId), loginMember.getId());


        return ResponseEntity.status(HttpStatus.OK).body(message);
    }


    @ResponseBody
    @GetMapping("/comment/getComments")
    public Map<String, ?> commentList() {

        Map<String, List<?>> ret = new HashMap<>();
        List<Comment> comments = commentService.findAll();

        List<String> commentContentList = new ArrayList<>();
        List<String> commentNameList = new ArrayList<>();
        List<LocalDateTime> commentCreatedTimeList = new ArrayList<>();
        List<Long> commentIdList = new ArrayList<>();

        for (Comment comment : comments) {
            commentContentList.add(comment.getCommentContent());
            commentNameList.add(comment.getMember().getName());
            commentCreatedTimeList.add(comment.getCreatedTime());
            commentIdList.add(comment.getId());
        }

        ret.put("commentContentList", commentContentList);
        ret.put("commentNameList", commentNameList);
        ret.put("commentCreatedTimeList", commentCreatedTimeList);
        ret.put("commentIdList", commentIdList);
        return ret;
    }

    @GetMapping("/comment/delete/{commentId}")
    public String delete(@PathVariable("commentId") Long commentId,
                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                         HttpServletRequest request) {
        Comment comment = commentService.findOne(commentId);

        if(!Objects.equals(comment.getMember().getId(), loginMember.getId())) {
            return "error/showError.html";
        }

        commentService.delete(commentId);

        // 여기서 referer = /comment/delete/3 이 옴.
        // 따라서 이렇게 말고 memberId, pageId 를 받아서 그냥 uri 만들어서 리다이렉트해야할듯 
        String referer = request.getRequestURI();
        System.out.println("referer = " + referer);
        return "redirect:" + referer;
    }

}
