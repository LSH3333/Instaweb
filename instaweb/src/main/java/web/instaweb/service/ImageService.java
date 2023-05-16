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

    public Image findOne(Long id) {
        return imageRepository.findOne(id);
    }

    public List<Image> findAll() {
        return imageRepository.findAll();
    }

    public void deleteImage(Long id) {
        imageRepository.deleteImage(id);
    }

    /**
     * @param id : db 에서 찾을 Image 의 id
     * @param base64String : base64 string 형식의 이미지 파일
     *
     * id 에 해당하는 Image 없으면 return
     *
     */
    public void updateImage(Long id, String base64String, long imgIdx) {
        // 여기서 image 는 db에서 가져왔으므로 영속 상태
        Image image = imageRepository.findOne(id);
        if (image == null) {
            return;
        }
        byte[] decoded = Base64.getDecoder().decode(base64String);
        image.changeImage(decoded);
        image.changeImgIdx(imgIdx);
        // @Transactional 에 의해 commit 됨 -> flush (변경 감지)
    }

    // Page 에 속한 모든 Image 들 삭제
    public void deletePagesAllImages(Long pageId) {
        System.out.println("deletePagesAllImages");
        Page page = pageService.findOne(pageId);
        System.out.println("pageId = " + pageId);
        List<Image> images = page.getImages();
        for (Image image : images) {
            System.out.println("image = " + image.getId());
        }
        images.clear();
    }
}
