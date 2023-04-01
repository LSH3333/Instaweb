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
import java.util.List;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    @Transactional
    public void save(MultipartFile file)  {

        try {
//            Image image = imageRepository.findOne(id);
            Image image = new Image();

            byte[] byteObject = new byte[file.getBytes().length];

            int i = 0;

            for (byte b : file.getBytes()){
                byteObject[i++] = b;
            }

            image.setImage(byteObject);

            imageRepository.save(image);
        } catch (IOException e) {
            log.error("Error occurred", e);

            e.printStackTrace();
        }
    }

    public Image findOne(Long id) {
        return imageRepository.findOne(id);
    }

    public List<Image> findAll() {
        return imageRepository.findAll();
    }
}
