package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Page;
import web.instaweb.repository.CommentRepository;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;

    public void save(Comment comment, Page page) {
        commentRepository.save(comment);
        comment.setPage(page);
    }
}
