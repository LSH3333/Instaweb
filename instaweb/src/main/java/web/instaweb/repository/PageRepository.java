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

    public void deletePage(Long id) {
        Page page = em.find(Page.class, id);
        em.remove(page);
    }

    /**
     * firstIdx 부터 count 개의 Page 를 가져온다 (생성일 기준 내림차순 정렬)
     */
    public List<Page> findPages(int firstIdx, int count) {
        return em.createQuery("select p from Page p order by p.createdTime desc", Page.class)
                .setFirstResult(firstIdx)
                .setMaxResults(count)
                .getResultList();
    }
}
