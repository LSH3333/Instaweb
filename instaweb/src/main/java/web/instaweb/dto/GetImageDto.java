package web.instaweb.dto;

import lombok.Data;
import org.apache.tomcat.util.codec.binary.Base64;
import web.instaweb.domain.Image;

@Data
public class GetImageDto {
    private Long id;
    private byte[] image;
    private String base64Image; // view 에서 디스플레이 할때 사용

    public GetImageDto(Image image) {
        this.id = image.getId();
        this.image = image.getImage();
        this.base64Image = generateBase64Image();
    }

    // byte 배열을 base64 string 형으로 변환해 리턴
    // 이 형식으로 뷰에서 display 가능
    public String generateBase64Image() {
        return Base64.encodeBase64String(this.getImage());
    }
}
