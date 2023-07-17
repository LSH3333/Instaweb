package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.repository.CommentRepository;

import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PageService pageService;
    private final MemberService memberService;

    public void save(Comment comment, Long pageId, Long memberId) {

        Page page = pageService.findOne(pageId);
        Member member = memberService.findOne(memberId);

        // comment - page 연관관계
        comment.setPage(page);
        page.addComment(comment);

        // comment - member 연관관계
        comment.setMember(member);
        member.addComment(comment);

        commentRepository.save(comment);
    }

    public List<Comment> findAll() {
        return commentRepository.findAll();
    }

    public Comment findOne(Long id) {
        return commentRepository.findOne(id);
    }

    public void delete(Long id) {
        commentRepository.delete(id);
    }
}
