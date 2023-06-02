package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;
import web.instaweb.repository.ImageRepository;
import web.instaweb.repository.PageRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;
    private final MemberService memberService;

    public void savePage(Page page) {
        pageRepository.save(page);
    }

    // 변경 감지
    public Page updatePage(Long id, String title, String content, LocalDateTime createTime, boolean writingDone) {
        // 여기서 findPage 는 db에서 가져왔으므로 영속 상태
        Page findPage = pageRepository.findOne(id);
        findPage.changeAll(id, title, content, createTime, writingDone);
        // @Transactional 에 의해 commit 됨 -> flush (변경 감지)
        return findPage;
    }

    // 제거
    public void deletePage(Long id) {
        pageRepository.deletePage(id);
    }

    public Page findOne(Long pageId) {
        return pageRepository.findOne(pageId);
    }

    public List<Page> findAll() {
        return pageRepository.findAll();
    }

    public PagesAndEndIdxDto findRange(int beginIdx, int cnt) {
        return pageRepository.findRange(beginIdx, cnt);
    }

    /**
     * 이 member 가 작성한 page 들 중 beginIdx 부터 cnt 개 찾는다
     * @param beginIdx
     * @param cnt
     * @return : {다음에 찾기 시작할 idx, 찾은 pages 들}
     */
    public PagesAndEndIdxDto getCntPagesFromIdx(int beginIdx, int cnt, Long memberId) {
        Member member = memberService.findOne(memberId);
        List<Page> pages = member.getPages();
        List<Page> retPages = new ArrayList<>();
        int i;
        for(i = beginIdx; (i < beginIdx+cnt) && (i < pages.size()) ; i++) {
            if(pages.get(i).getWritingDone()) {
                retPages.add(pages.get(i));
            }
        }
        return new PagesAndEndIdxDto(i, retPages);
    }


}
