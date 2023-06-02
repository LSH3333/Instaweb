package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PageRepository {

    private final EntityManager em;

    public void save(Page page) {
        em.persist(page);
    }

    // page 를 가져올때는 images collection 도 모두 가져옴 (fetch eager)
    public Page findOne(Long id) {
//        return em.find(Page.class, id);

        return em.createQuery(
                        "SELECT p FROM Page p LEFT JOIN FETCH p.images WHERE p.id = :pageId",
                        Page.class)
                .setParameter("pageId", id)
                .getSingleResult();
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
    public PagesAndEndIdxDto findRange(int beginIdx, int count) {
        List<Page> ret = new ArrayList<>();

        List<Page> resultList = em.createQuery("select p from Page p order by p.createdTime desc", Page.class)
                .setFirstResult(beginIdx)
                .setMaxResults(count)
                .getResultList();


        for (Page page : resultList) {
            if(page.getWritingDone()) {
                ret.add(page);
            }
        }

        int endIdx = beginIdx + resultList.size();

        return new PagesAndEndIdxDto(endIdx, ret);
    }
}
