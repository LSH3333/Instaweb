package web.instaweb.domain;

import lombok.Data;

/**
 * 업로드한 파일 정보
 */

@Data
public class UploadFile {

    // 사용자가 업로드한 파일의 이름
    private String uploadFileName;
    // 서버에 저장되는 파일의 이름
    private String storeFileName;
    // 파일 데이터
    private String fileData;

    public UploadFile(String uploadFileName, String storeFileName) {
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
    }

}
