package web.instaweb.domain;

import lombok.Getter;

import javax.persistence.*;

@Entity
@Table
@Getter
public class Image {

    @Id @GeneratedValue
    @Column(name = "image_id")
    private Long id;

    @Lob
    private byte[] image;


    public void setImage(byte[] image) {
        this.image = image;
    }
}
