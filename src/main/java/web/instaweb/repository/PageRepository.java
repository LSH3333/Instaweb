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

    /**
     *
     * @param beginIdx : 가져올 시작 인덱스
     * @param count : 최대 몇개 가져 올지
     * @param searchQuery
     * @return : page.title, page.content 중 searchQuery 스트링을 포함하는 page 들 리턴
     */
    public PagesAndEndIdxDto findSearchQuery(int beginIdx, int count, String searchQuery) {

        List<Page> resultList = em.createQuery("SELECT p FROM Page p WHERE LOWER(p.title) LIKE LOWER(:searchQuery) OR LOWER(p.content) LIKE LOWER(:searchQuery) ORDER BY p.createdTime DESC", Page.class)
                .setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%")
                .setFirstResult(beginIdx)
                .setMaxResults(count)
                .getResultList();


        int endIdx = beginIdx + resultList.size();

        return new PagesAndEndIdxDto(endIdx, resultList);
    }

    public PagesAndEndIdxDto findMineSearchQuery(int beginIdx, int count, String searchQuery, Long memberId) {

//        List<Page> resultList = em.createQuery("SELECT p FROM Page p WHERE LOWER(p.title) LIKE LOWER(:searchQuery) OR LOWER(p.content) LIKE LOWER(:searchQuery) ORDER BY p.createdTime DESC", Page.class)
//                .setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%")
//                .setFirstResult(beginIdx)
//                .setMaxResults(count)
//                .getResultList();

        List<Page> resultList = em.createQuery("SELECT p FROM Page p WHERE (LOWER(p.title) LIKE LOWER(:searchQuery) OR LOWER(p.content) LIKE LOWER(:searchQuery)) AND p.member.id = :memberId ORDER BY p.createdTime DESC", Page.class)
                .setParameter("searchQuery", "%" + searchQuery.toLowerCase() + "%")
                .setParameter("memberId", memberId)
                .setFirstResult(beginIdx)
                .setMaxResults(count)
                .getResultList();



        int endIdx = beginIdx + resultList.size();

        return new PagesAndEndIdxDto(endIdx, resultList);
    }
}
