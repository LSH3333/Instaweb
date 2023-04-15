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

    @Transactional
    public void saveImage(Image image) {
        imageRepository.save(image);
    }

    public Image findOne(Long id) {
        return imageRepository.findOne(id);
    }

    public List<Image> findAll() {
        return imageRepository.findAll();
    }

    @Transactional
    public void deleteImage(Long id) {
        imageRepository.deleteImage(id);
    }

    // 변경 감지
    @Transactional
    public void updateImage(Long id, String base64String) {
        // 여기서 image 는 db에서 가져왔으므로 영속 상태
        Image image = imageRepository.findOne(id);
        byte[] decoded = Base64.getDecoder().decode(base64String);
        image.changeImage(decoded);
        // @Transactional 에 의해 commit 됨 -> flush (변경 감지)
    }
}
