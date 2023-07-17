package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import web.instaweb.domain.Comment;
import web.instaweb.domain.Page;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentRepository {

    private final EntityManager em;

    public void save(Comment comment) {
        em.persist(comment);
    }

    public List<Comment> findAll() {
        return em.createQuery("select c from Comment c", Comment.class).getResultList();
    }


}
