package web.instaweb.dto;

import lombok.Getter;
import lombok.Setter;
import web.instaweb.domain.Page;

import java.util.List;

@Getter @Setter
public class PagesAndEndIdxDto {
    private int endIdx;
    private List<Page> retPages;

    public PagesAndEndIdxDto(int i, List<Page> retPages) {
        this.endIdx = i;
        this.retPages = retPages;
    }
}
