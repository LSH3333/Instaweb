package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import web.instaweb.domain.Image;

import javax.persistence.EntityManager;

@Repository
@RequiredArgsConstructor
public class ImageRepository {

    private final EntityManager em;

    public void save(Image image) {
        em.persist(image);
    }

    public Image findOne(Long id) {
        return em.find(Image.class, id);
    }


}
