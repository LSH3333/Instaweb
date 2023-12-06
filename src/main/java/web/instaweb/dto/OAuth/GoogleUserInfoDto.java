package web.instaweb.dto.OAuth;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GoogleUserInfoDto implements OAuthUserInfoDto {
    private String email;
    private Boolean email_verified;
    private String name;
    private String given_name;
    private String picture;
    private String locale;
}
