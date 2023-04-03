package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Page;
import web.instaweb.repository.ImageRepository;
import web.instaweb.repository.PageRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;

    @Transactional(readOnly = false)
    public void savePage(Page page) {
        pageRepository.save(page);
    }

    // 변경 감지
    @Transactional
    public void updatePage(Long id, String title, String content, LocalDateTime createTime) {
        // 여기서 findPage 는 db에서 가져왔으므로 영속 상태
        Page findPage = pageRepository.findOne(id);
        findPage.changeAll(id, title, content, createTime);
        // @Transactional 에 의해 commit 됨 -> flush (변경 감지)
    }

    // 제거
    // todo : 페이지 제거할때 연관관계인 이미지도 제거 하도록 해야함
    @Transactional
    public void deletePage(Long id) {
        pageRepository.deletePage(id);
    }

    public Page findOne(Long pageId) {
        return pageRepository.findOne(pageId);
    }

    public List<Page> findAll() {
        return pageRepository.findAll();
    }


}
