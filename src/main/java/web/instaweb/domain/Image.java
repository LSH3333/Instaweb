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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "page_id")
    private Page page;

    @Lob
    private byte[] image;


    /**
     * 이미지의 유니크한 uuid, Image 파일은 최초에 클라이언트 쪽에서 삽입되기 때문에 클라이언트에서 uuid 값 생성하고 서버로 보낸 후에 해당값 저장함.
     * 이 UUID 값은 클라이언트에서 이미지를 랜더링할때도 사용됨
     * <img id="7dcbd0e3-2240-4fc4-b097-8b5bd159cb35">
     * <img> 엘레먼트에는 이처럼 uuid 가 저장되고, 이를 이용해서 맞는 이미지 객체 찾음
     */
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
