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

    public void delete(Long id) {
        Comment comment = em.find(Comment.class, id);
        em.remove(comment);
    }

    public Comment findOne(Long id) {
        return em.createQuery(
                        "SELECT c FROM Comment c WHERE c.id = :commentId", Comment.class)
                .setParameter("commentId", id)
                .getSingleResult();
    }

    public List<Comment> findAll() {
        return em.createQuery("select c from Comment c", Comment.class).getResultList();
    }

    /**
     * Page 에 속한 모든 Comment 리턴
     */
    public List<Comment> findByPageId(Page page) {
        return page.getComments();
    }

}
