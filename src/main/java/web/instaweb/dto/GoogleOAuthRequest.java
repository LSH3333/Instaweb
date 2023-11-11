package web.instaweb.dto;

import lombok.Builder;
import lombok.Data;

/**
 * access_token 받기위해 구글에 보낼 request Dto
 */
@Data
@Builder
public class GoogleOAuthRequest {
    private String redirectUri;
    private String clientId;
    private String clientSecret;
    private String code; // Authorization code
    private String responseType;
    private String scope;
    private String accessType;
    private String grantType;
    private String state;
    private String includeGrantedScopes;
    private String loginHint;
    private String prompt;


}
