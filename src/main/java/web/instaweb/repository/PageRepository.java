package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Repository;
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
     * findAll, 모든 member 의 모든 page 대상 검색
     * 최대 10개의 쿼리 조건에 만족하는 페이지 리턴 , 클라이언트에 endIdx 를 넘겨서 다음 스크롤시 endIdx 인덱스 부터 시작함
     * @param beginIdx : 가져올 시작 인덱스
     * @param searchQuery
     * @return : page.title, page.content 중 searchQuery 스트링을 포함하는 page 들 리턴
     */
    public PagesAndEndIdxDto findSearchQuery(int beginIdx, int count, String searchQuery) {
        List<Page> resultList = new ArrayList<>();

        List<Page> allPage = em.createQuery("SELECT p FROM Page p ORDER BY p.createdTime DESC", Page.class)
                .getResultList();


        int endIdx = beginIdx;

        for(int i = beginIdx; i < allPage.size(); i++) {
            Page page = allPage.get(i);
            if (page.getTitle() != null && checkContentWithSearchQuery(page.getTitle(), page.getContent(), searchQuery)) {
                resultList.add(page);
            }
            if(resultList.size() > 10) break; // 결과 10개 넘으면 그만 담음
            endIdx++;
        }

        return new PagesAndEndIdxDto(endIdx+1, resultList);
    }

    /**
     * findMine, 로그인한 Member 의 Page 대상 검색
     */
    public PagesAndEndIdxDto findMineSearchQuery(int beginIdx, int count, String searchQuery, Long memberId) {
        List<Page> resultList = new ArrayList<>();

        List<Page> allPage = em.createQuery("SELECT p FROM Page p WHERE p.member.id = :memberId ORDER BY p.createdTime DESC", Page.class)
                .setParameter("memberId", memberId)
                .getResultList();


        int endIdx = beginIdx;

        for(int i = beginIdx; i < allPage.size(); i++) {
            Page page = allPage.get(i);
            if (page.getTitle() != null && checkContentWithSearchQuery(page.getTitle(), page.getContent(), searchQuery)) {
                resultList.add(page);
            }
            if(resultList.size() > 10) break; // 결과 10개 넘으면 그만 담음
            endIdx++;
        }

        //


        return new PagesAndEndIdxDto(endIdx+1, resultList);
    }


    /**
     * page 의 content 의 textContent (element 제외) 에 searchQuery 포함되는지 확인한다
     */
    private boolean checkContentWithSearchQuery( String title,String content, String searchQuery) {
        searchQuery = searchQuery.toLowerCase();
        // Parse the HTML string
        Document doc = Jsoup.parse(content);
        Elements elements = doc.select("div");

        // title
        if(title.contains(searchQuery)) {
            return true;
        }

        // Iterate over the selected elements
        // content element check
        for (Element element : elements) {
            String text = element.text();
            if (text.toLowerCase().contains(searchQuery)) {
                return true;
            }
        }
        return false;
    }
}
