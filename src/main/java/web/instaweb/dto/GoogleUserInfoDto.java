package web.instaweb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
public class GoogleUserInfoDto implements OAuthUserInfoDto {
    private String oauthType;
    private String email;
    private Boolean email_verified;
    private String name;
    private String given_na,e;
    private String picture;
    private String locale;

    public GoogleUserInfoDto() {
        this.oauthType = "GOOGLE";
    }

    @Override
    public String getOAuthType() {
        return oauthType;
    }
}
