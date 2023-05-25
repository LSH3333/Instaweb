package web.instaweb.domain;

import lombok.Getter;
import org.apache.tomcat.util.codec.binary.Base64;
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

    // Page.images 리스트에 저장되는 순서
    private String UUID;

    public void setImgUUID(String UUID) {
        this.UUID = UUID;
    }

    public Image() {}

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

    // byte 배열을 base64 string 형으로 변환해 리턴
    // 이 형식으로 뷰에서 display 가능
    public String generateBase64Image() {
        return Base64.encodeBase64String(this.getImage());
    }

    public void changeImage(byte[] image) {
        this.image = image;
    }

    public void changeImgIdx(String UUID) {
        this.UUID = UUID;
    }
}
