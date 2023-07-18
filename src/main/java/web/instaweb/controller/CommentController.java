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

        Comment comment = new Comment(commentContent);
        commentService.save(comment, Long.parseLong(pageId), loginMember.getId());


        return ResponseEntity.status(HttpStatus.OK).body("");
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
    public ResponseEntity<String> delete(@PathVariable("commentId") Long commentId,
                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                         HttpServletRequest request) {
        System.out.println("CommentController.delete = " + commentId);
        String message = "";
        Comment comment = commentService.findOne(commentId);

        if(!Objects.equals(comment.getMember().getId(), loginMember.getId())) {
//            return "error/showError.html";
            message = "Failed to upload files";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }

        commentService.delete(commentId);

        return ResponseEntity.status(HttpStatus.OK).body("");
    }

}
