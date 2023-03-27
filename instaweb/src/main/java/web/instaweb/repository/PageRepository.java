package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import web.instaweb.domain.Page;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PageRepository {

    private final EntityManager em;

    public void save(Page page) {
        em.persist(page);
    }

    public Page findOne(Long id) {
        return em.find(Page.class, id);
    }

    public List<Page> findAll() {
        return em.createQuery("select p from Page p", Page.class).getResultList();
    }
}
