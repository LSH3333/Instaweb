package web.instaweb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GoogleLoginResponse {
    // 어플리케이션이 Google Api 요청을 승인하기 위해 보내는 토큰
    private String access_token;
    // access_token 의 수명
    private String expires_in;
    // 새 access_token 을 얻는데 사용할 수 있는 토큰
    private String refreshToken;
    private String scope;
    // 반환된 토큰 유형
    private String token_type;
    private String id_token;
}
