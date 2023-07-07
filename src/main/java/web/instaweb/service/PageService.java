package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;
import web.instaweb.dto.PagesAndEndIdxDto;
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


    public PagesAndEndIdxDto findRange(int beginIdx, int cnt) {
        return pageRepository.findRange(beginIdx, cnt);
    }

    public PagesAndEndIdxDto findSearchQuery(int beginIdx, int count, String searchQuery) {
        return pageRepository.findSearchQuery(beginIdx, count, searchQuery);
    }

    public PagesAndEndIdxDto findMineSearchQuery(int beginIdx, int count, String searchQuery, Long memberId) {
        return pageRepository.findMineSearchQuery(beginIdx, count, searchQuery, memberId);
    }

    /**
     * 이 member 가 작성한 page 들 중 beginIdx 부터 cnt 개 찾는다
     * @param beginIdx
     * @param cnt
     * @return : {다음에 찾기 시작할 idx, 찾은 pages 들}
     */
    public PagesAndEndIdxDto getCntPagesFromIdxInMemberPage(int beginIdx, int cnt, Long memberId) {
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

    /**
     * 글 작성을 위해 새로운 Page 를 만들때, Member 와 연관관계를 맺은후 Page 를 리턴한다
     */
    public Page createPageForMember(Member member) {
        Page page = new Page(LocalDateTime.now());
        page.setMember(member);
        member.addPage(page);
        pageRepository.save(page);
        // member 가 작성중 상태인 page id 기억해놓음
        memberService.setMemberWritingPageId(member.getId(), page.getId());
        return page;
    }

}
