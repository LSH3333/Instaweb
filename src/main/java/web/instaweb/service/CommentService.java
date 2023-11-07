package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PageService pageService;
    private final MemberService memberService;

    /**
     * Comment 는 Page 와 다대일 관계, Member 와 다대일 관계
     * 트랜잭션 내부인 서비스 영역에서 Page, Member 조회해서 Comment 와 관계 맺은 후 레포지토리를 통해 저장
     * @param comment : Comment 객체
     * @param pageId : 댓글이 달린 Page 의 pageId
     * @param memberId : 댓글을 작성한 Member 의 id
     */
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


    public List<Comment> findByPageId(Long pageId) {
        Page page = pageService.findOne(pageId);
        return commentRepository.findByPageId(page);
    }

    /**
     * Page 에 속한 Comment 찾고, 클라이언트에 보내기 위해 필요 정보들 wrap 해서 리턴함
     */
    public Map<String, List<?>> getWrappedComment(Long pageId) {

        Map<String, List<?>> ret = new HashMap<>();

        List<Comment> comments = findByPageId(pageId);

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
}
