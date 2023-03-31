package web.instaweb.file;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import web.instaweb.domain.UploadFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class FileStore {

    // application 파일의 file.dir 가져옴
    @Value("${file.dir}")
    private String fileDir;

    /**
     *  절대경로 full path 리턴함
     */
    public String getFullPath(String fileName) {
        return fileDir + fileName;
    }

    /**
     * 전달 받은 MultipartFile 들을 서버에 저장하고 UploadFile 형식으로 만들어 List 에 담아 리턴함
     */
    public List<UploadFile> storeFiles(List<MultipartFile> multipartFiles) throws IOException {
        List<UploadFile> storeFileResult = new ArrayList<>();

        for (MultipartFile multipartFile : multipartFiles) {
            if (!multipartFile.isEmpty()) {
                storeFileResult.add(storeFile(multipartFile));
            }
        }

        return storeFileResult;
    }

    /**
     * 전달 받은 MultipartFile 을 서버에 저장하고 UploadFile 형식으로 만들어 리턴함
     */
    public UploadFile storeFile(MultipartFile multipartFile) throws IOException {
        if (multipartFile.isEmpty()) {
            return null;
        }

        // 사용자가 업로드한 파일 명
        String originalFilename = multipartFile.getOriginalFilename();

        // UUID.확장자 형식으로 서버에 저장할 파일명 만듦
        String storeFileName = createStoreFileName(originalFilename);


        multipartFile.transferTo(new File(getFullPath(storeFileName)));

        return new UploadFile(originalFilename, storeFileName);
    }

    /**
     * 랜덤 UUID.확장자 형식으로된 StoreFileName 리턴함
     */
    private String createStoreFileName(String originalFilename) {
        String uuid = UUID.randomUUID().toString();
        String ext = extractExt(originalFilename);
        return uuid + "." + ext;
    }

    /**
     * originalFilename 의 확장자 리턴함
     */
    private String extractExt(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }
}
