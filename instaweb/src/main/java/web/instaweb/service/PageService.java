package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.domain.Page;
import web.instaweb.repository.PageRepository;

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

    public Page findOne(Long pageId) {
        return pageRepository.findOne(pageId);
    }

    public List<Page> findAll() {
        return pageRepository.findAll();
    }


}
