package web.instaweb.domain;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table
@Getter
public class Image {

    @Id @GeneratedValue
    @Column(name = "image_id")
    private Long id;

    // Image 기준 Page 와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "page_id")
    private Page page;

    @Lob
    private byte[] image;

    protected Image() {}

    public void setImage(byte[] image) {
        this.image = image;
    }

    public Image(MultipartFile file) throws IOException {
        saveImage(file);
    }

    public void setPage(Page page) {
        this.page = page;
    }

    /**
     * MultiPartFile 로 받은 이미지 파일들 byte 로 변환 후 저장
     */
    private void saveImage(MultipartFile file) throws IOException {
        byte[] byteObject = new byte[file.getBytes().length];

        int i = 0;

        for (byte b : file.getBytes()){
            byteObject[i++] = b;
        }

        this.image = byteObject;
    }

}
