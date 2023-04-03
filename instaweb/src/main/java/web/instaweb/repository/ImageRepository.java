package web.instaweb.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import web.instaweb.domain.Image;
import web.instaweb.domain.Page;

import javax.persistence.EntityManager;
import java.util.List;

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

    public List<Image> findAll() {
        return em.createQuery("select i from Image i", Image.class).getResultList();
    }

    public void deleteImage(Long id) {
        Image image = em.find(Image.class, id);
        if(image != null) em.remove(image);
    }
}
