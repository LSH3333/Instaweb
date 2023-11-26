package web.instaweb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONObject;

@Data
public class KakaoUserInfoDto implements OAuthUserInfoDto {
    private String oauthType;
    private Long id;
    private JSONObject properties; // nickname
    // https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
    // 사용자에 대한 추가 정보 필요하면 위에서 확인후 추가

    public KakaoUserInfoDto() {
        this.oauthType = "KAKAO";
    }


    @Override
    public String getOAuthType() {
        return oauthType;
    }


}
