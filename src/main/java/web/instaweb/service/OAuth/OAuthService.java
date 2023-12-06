package web.instaweb.service.OAuth;

import web.instaweb.domain.Member;
import web.instaweb.dto.OAuth.OAuthLoginResponse;
import web.instaweb.dto.OAuth.OAuthUserInfoDto;

import javax.servlet.http.HttpServletRequest;

// OAuthService interface <- OAuthLoginService abstract class <- Google or Kakao OAuthService impl
public interface OAuthService {
    // google, kakao 등 type
    String getType();
    String getReqUrl();
    OAuthLoginResponse requestAccessTokenToResourceServer(String authCode);
    OAuthUserInfoDto getUserInfoFromResourceServer(String token);
    // OAuthLoginService abstract class 에서 정의
    Member login(OAuthUserInfoDto oAuthUserInfoDto, HttpServletRequest request);
    String getLoginId(OAuthUserInfoDto oAuthUserInfoDto);
    String getUserName(OAuthUserInfoDto oAuthUserInfoDto);
}
