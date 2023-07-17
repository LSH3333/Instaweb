package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Page;
import web.instaweb.service.CommentService;
import web.instaweb.service.PageService;


@Controller
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final PageService pageService;

    @PostMapping("/comment/upload")
    public ResponseEntity<String> submit(@RequestParam("pageId") String pageId, @RequestParam("comment") String commentContent) {
        String message = "";

        System.out.println("comment = " + commentContent);
        Page page = pageService.findOne(Long.parseLong(pageId));

        Comment comment = new Comment(commentContent);
        commentService.save(comment, page);


        return ResponseEntity.status(HttpStatus.OK).body(message);

    }
}
