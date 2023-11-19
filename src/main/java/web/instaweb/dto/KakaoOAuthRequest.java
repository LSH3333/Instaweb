package web.instaweb.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class KakaoOAuthRequest {
    String grant_type;
    String client_id;
    String redirect_uri;
    String code; // Authorization code
    String client_secret;
    String content_type;
}
