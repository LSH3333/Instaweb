package web.instaweb.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class GoogleUserInfoDto {
    private String email;
    private Boolean email_verified;
    private String name;
    private String given_na,e;
    private String picture;
    private String locale;
}
