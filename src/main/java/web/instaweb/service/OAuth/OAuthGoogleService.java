package web.instaweb.service.OAuth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import web.instaweb.dto.OAuth.GoogleLoginResponse;
import web.instaweb.dto.OAuth.GoogleUserInfoDto;
import web.instaweb.dto.OAuth.OAuthLoginResponse;
import web.instaweb.dto.OAuth.OAuthUserInfoDto;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;


@Service
@Transactional(readOnly = false)
public class OAuthGoogleService extends OAuthLoginService {

    // 노출되면 안되는 secret key 들은 환경변수로 등록해서 사용
    // 리소서 서버에 로그인 요청 url, 응답으로 auth_code 받음
    @Value("${GOOGLE_LOGIN_URL}")
    private String loginUrl;
    // 리소스 서버에 access_token 요청 url
    @Value("${GOOGLE_AUTH_URL}")
    private String authUrl;
    @Value("${GOOGLE_CLIENT_ID}")
    private String clientId;
    @Value("${GOOGLE_REDIRECT_URL}")
    private String redirectUrl;
    @Value("${GOOGLE_SECRET_CLIENT}")
    private String clientSecret;

    @Autowired
    public OAuthGoogleService(MemberService memberService, LoginService loginService) {
        super(memberService, loginService);
    }

    @Override
    public String getType() {
        return "google";
    }

    @Override
    public String getReqUrl() {
        return loginUrl + "/o/oauth2/v2/auth" +
                "?client_id=" + clientId +
                "&redirect_uri=" + redirectUrl +
                "&response_type=code" +
                "&scope=email%20profile%20openid" +
                "&access_type=offline";
    }

    @Override
    public OAuthLoginResponse requestAccessTokenToResourceServer(String authCode) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grantType", "authorization_code");
        formData.add("clientId", clientId);
        formData.add("redirectUri", redirectUrl);
        formData.add("code", authCode);
        formData.add("clientSecret", clientSecret);


        // WebClient 에 googleOAuthRequest 담아서 요청 보내고 응답 받는다
        WebClient webClient = WebClient.builder()
                .baseUrl(authUrl) // api 요청 base path
                .build();

        ResponseEntity<GoogleLoginResponse> apiResponse = webClient.post().uri(uriBuilder -> uriBuilder.path("/token").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .header("from", "client")
                .retrieve()
                .toEntity(GoogleLoginResponse.class) // GoogleLoginResponse 로 받는다
                .block();

        return apiResponse.getBody();
    }

    @Override
    public OAuthUserInfoDto getUserInfoFromResourceServer(String token) {
        // 받은 토큰을 리소스 서버에 보내 유저정보를 얻고
        // 허가된 토큰의 유저정보를 결과로 받는다.

        // 리소스 서버에 access_token 요청
        WebClient webClient = WebClient.builder()
                .baseUrl(authUrl)
                .defaultHeader("from", "client")
                .build();

        ResponseEntity<GoogleUserInfoDto> googleUserInfoDtoResponseEntity = webClient.get().uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("access_token",token).build())
                .retrieve()
                .toEntity(GoogleUserInfoDto.class)
                .block();

        return googleUserInfoDtoResponseEntity.getBody();
    }


    @Override
    public String getLoginId(OAuthUserInfoDto oAuthUserInfoDto) {
        GoogleUserInfoDto googleUserInfoDto = (GoogleUserInfoDto) oAuthUserInfoDto;
        return googleUserInfoDto.getEmail();
    }

    @Override
    public String getUserName(OAuthUserInfoDto oAuthUserInfoDto) {
        GoogleUserInfoDto googleUserInfoDto = (GoogleUserInfoDto) oAuthUserInfoDto;
        return googleUserInfoDto.getName();
    }


}
