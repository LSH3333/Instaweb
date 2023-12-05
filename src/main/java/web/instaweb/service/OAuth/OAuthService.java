package web.instaweb.service.OAuth;

import web.instaweb.domain.Member;
import web.instaweb.dto.OAuth.OAuthLoginResponse;
import web.instaweb.dto.OAuth.OAuthUserInfoDto;

import javax.servlet.http.HttpServletRequest;

public interface OAuthService {
    String getType();
    String getReqUrl();
    OAuthLoginResponse requestAccessTokenToResourceServer(String authCode);
    OAuthUserInfoDto getUserInfoFromResourceServer(String token);
    Member login(OAuthUserInfoDto oAuthUserInfoDto, HttpServletRequest request);
}
