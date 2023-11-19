package web.instaweb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class KakaoLoginResponse {
    // 반환된 토큰 유형
    private String token_type;
    // 어플리케이션이 Google Api 요청을 승인하기 위해 보내는 토큰
    private String access_token;
    // 사용자의 정보: name, email 등 담겨있음.
    // 리소스 오너에게 보내는 용도가 아닌 클라이언트에서 유저 식별 용도로 사용
    private String id_token;
    // access_token 의 수명
    private Integer expires_in;
    // 새 access_token 을 얻는데 사용할 수 있는 토큰
    private String refresh_token;
    private Integer refresh_token_expires_in;

    private String scope;


}
