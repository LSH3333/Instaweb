package web.instaweb.singletonBean;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Component
public class NoImgProvider {
    private byte[] noImgFile;

    public NoImgProvider() throws IOException {
        loadNoImgFile();
    }

    private void loadNoImgFile() throws IOException {
        Resource resource = new ClassPathResource("static/no-img.png");
        try (InputStream inputStream = resource.getInputStream()) {
            noImgFile = inputStream.readAllBytes();
        }
    }

    public byte[] getNoImgFile() {
        return noImgFile;
    }
}
