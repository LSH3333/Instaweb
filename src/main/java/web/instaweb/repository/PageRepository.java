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

    /**
     * pageId 와 일치하는 Page 객체 1개 리턴하는데, Page 에 속한 Image 들도 모두 fetch 후 리턴한다
     * @param id : pageId
     * @return : pageId 와 일치하는 Page 객체 1개
     */
    public Page findOne(Long id) {
        // (Page) 와 (Page 에 속한 Image 컬렉션)의 LEFT JOIN 수행 후,
        // Page.id=pageId 인 Page 들만 리턴
        // Page 가져올때는 Image 도 필요하기 때문에 FETCH (EAGER)
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

        List<Page> resultList = em.createQuery("SELECT p FROM Page p ORDER BY p.createdTime DESC", Page.class)
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
     * 모든 member 의 모든 page 대상 검색
     * 최대 count 개의 쿼리 조건에 만족하는 페이지 리턴 , 클라이언트에 endIdx 를 넘겨서 다음 스크롤시 endIdx 인덱스 부터 시작함
     * @param beginIdx : 가져올 시작 인덱스
     * @param count : 최대 count 개의 결과 리턴
     * @param searchQuery : 사용자가 검색한 문자열
     * @return : page.title, page.content 중 searchQuery 스트링을 포함하는 page 들 리턴
     */
    public PagesAndEndIdxDto findSearchQuery(int beginIdx, int count, String searchQuery) {
        List<Page> resultList = new ArrayList<>();
        // 존재하는 모든 Page 디비에서 가져온다, createdTime 기준 내림차순으로
        List<Page> allPage = em.createQuery("SELECT p FROM Page p ORDER BY p.createdTime DESC", Page.class)
                .getResultList();

        // 디비에서 조회결과의 갯수에 따라 endIdx 가 달라진다.
        // endIdx 는 클라이언트로 전달되고 이후 또 다시 검색할때 beginIdx 로서 전달된다
        int endIdx = beginIdx;

        for(int i = beginIdx; i < allPage.size(); i++) {
            Page page = allPage.get(i);
            // Page.title, Page.content 에 검색쿼리와 일치하는 문자열 있는지 확인한다
            if (page.getTitle() != null && checkContentWithSearchQuery(page.getTitle(), page.getContent(), searchQuery)) {
                resultList.add(page);
            }
            if(resultList.size() > count) break; // 결과 count개 넘으면 그만 담음
            endIdx++;
        }

        return new PagesAndEndIdxDto(endIdx+1, resultList);
    }

    /**
     * findAll() 과 로직은 동일하지만, 로그인한 Member 의 Page 대상 검색
     */
    public PagesAndEndIdxDto findMineSearchQuery(int beginIdx, int count, String searchQuery, Long memberId) {
        List<Page> resultList = new ArrayList<>();
        // 현재 로그인해있는 Member 가 작성한 Page 들을 모두 가져온다, createdTime 기준 내림차순으로
        List<Page> allPage = em.createQuery("SELECT p FROM Page p WHERE p.member.id = :memberId ORDER BY p.createdTime DESC", Page.class)
                .setParameter("memberId", memberId)
                .getResultList();


        int endIdx = beginIdx;

        for(int i = beginIdx; i < allPage.size(); i++) {
            Page page = allPage.get(i);
            if (page.getTitle() != null && checkContentWithSearchQuery(page.getTitle(), page.getContent(), searchQuery)) {
                resultList.add(page);
            }
            if(resultList.size() > count) break; // 결과 10개 넘으면 그만 담음
            endIdx++;
        }

        return new PagesAndEndIdxDto(endIdx+1, resultList);
    }


    /**
     * page 의 content 의 textContent (element 제외) 에 searchQuery 포함되는지 확인한다
     */
    private boolean checkContentWithSearchQuery(String title, String content, String searchQuery) {
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
