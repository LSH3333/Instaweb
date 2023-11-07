package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.instaweb.SessionConst;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Member;
import web.instaweb.service.CommentService;

import javax.servlet.http.HttpServletRequest;
import java.util.*;


@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * ajax 요청, loginMember 가 작성한 pageId 를 갖는 Page 의 Comment 저장
     * @param pageId : 댓글이 달린 Page 의 pageId
     * @param commentContent : 작성한 댓글 내용
     * @param loginMember : 댓글을 작성한, 로그인 되어 있는 Member
     * @return : 저장 성공, HttpStatus OK
     */
    @PostMapping("/comment/upload")
    public ResponseEntity<String> submit(@RequestParam("pageId") String pageId,
                                         @RequestParam("comment") String commentContent,
                                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember) {

        Comment comment = new Comment(commentContent);
        commentService.save(comment, Long.parseLong(pageId), loginMember.getId());

        return ResponseEntity.status(HttpStatus.OK).body("Files uploaded successfully!");
    }


    /**
     * ajax 요청 받으면 Page 에 속한 Comment 들 정보 wrap 해서 클라이언트로 보냄
     */
    @ResponseBody
    @GetMapping("/comment/getComments/{pageId}")
    public Map<String, ?> getComments(@PathVariable("pageId") Long pageId) {
        return commentService.getWrappedComment(pageId);
    }

    @GetMapping("/comment/delete/{commentId}")
    public ResponseEntity<String> delete(@PathVariable("commentId") Long commentId,
                         @SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember,
                         HttpServletRequest request) {

        String message = "";
        Comment comment = commentService.findOne(commentId);

        // 로그인 안된 상태 혹은 댓글의 주인이 아니면 에러
        if(loginMember == null || !Objects.equals(comment.getMember().getId(), loginMember.getId())) {
            message = "Failed to upload files";
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(message);
        }

        commentService.delete(commentId);

        message = "Files uploaded successfully!";
        return ResponseEntity.status(HttpStatus.OK).body(message);
    }

}
