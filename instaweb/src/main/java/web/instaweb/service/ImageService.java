package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.Image;
import web.instaweb.repository.ImageRepository;

import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
@Slf4j
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
}
