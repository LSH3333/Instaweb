package web.instaweb.factory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import web.instaweb.service.OAuth.OAuthService;

import java.util.List;

@Component
public class OAuthServiceFactory {

    private final List<OAuthService> oAuthServices;

    // 스프링이 OAuthService 타입의 빈 자동 주입
    @Autowired
    public OAuthServiceFactory(List<OAuthService> oAuthServices) {
        this.oAuthServices = oAuthServices;
    }

    /**
     * serviceType 에 따라 맞는 OAuthService 구현체 찾아서 리턴함
     * @param serviceType : "kakao", "google" 등
     * @return : OAuthService 구현체
     */
    public OAuthService getOAuthService(String serviceType) {
        for (OAuthService oAuthService : oAuthServices) {
            if (oAuthService.getType().equals(serviceType)) {
                return oAuthService;
            }
        }
        return null;
    }
}
