package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import web.instaweb.repository.PageRepository;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class PageService {

    private final PageRepository pageRepository;



}
