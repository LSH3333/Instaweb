package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.Image;
import web.instaweb.domain.Page;
import web.instaweb.repository.ImageRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final PageService pageService;

    public void saveImage(Image image) {
        imageRepository.save(image);
    }


    public List<Image> findAllImageFromPage(Long pageId) {
        Page page = pageService.findOne(pageId);
        return page.getImages();
    }

    /**
     * @param id : db 에서 찾을 Image 의 id
     * @param base64String : base64 string 형식의 이미지 파일
     *
     * id 에 해당하는 Image 없으면 return
     *
     */
    public void updateImage(Long id, String base64String, String UUID) {
        // 여기서 image 는 db에서 가져왔으므로 영속 상태
        Image image = imageRepository.findOne(id);
        if (image == null) {
            return;
        }
        byte[] decoded = Base64.getDecoder().decode(base64String);
        image.changeImage(decoded);
        image.changeImgIdx(UUID);
        // @Transactional 에 의해 commit 됨 -> flush (변경 감지)
    }

    // Page 에 속한 모든 Image 들 삭제
    public void deletePagesAllImages(Long pageId) {
        Page page = pageService.findOne(pageId);
        List<Image> images = page.getImages();
        for (Image image : images) {
            imageRepository.deleteImage(image.getId());
        }
    }
}
