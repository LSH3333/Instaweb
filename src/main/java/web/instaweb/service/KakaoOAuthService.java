package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleOAuthRequest;
import web.instaweb.dto.KakaoLoginResponse;
import web.instaweb.dto.KakaoOAuthRequest;

@Service
@RequiredArgsConstructor
public class KakaoOAuthService {
    // 노출되면 안되는 secret key 들은 환경변수로 등록해서 사용
    @Value("${KAKAO_AUTH_URL}")
    private String kakaoAuthUrl;
    @Value("${KAKAO_LOGIN_URL}")
    private String kakaoLoginUrl;
    @Value("${KAKAO_CLIENT_ID}")
    private String kakaoClientId;
    @Value("${KAKAO_REDIRECT_URL}")
    private String kakaoRedirectUrl;
    @Value("${KAKAO_SECRET_CLIENT}")
    private String kakaoClientSecret;

    private final MemberService memberService;
    private final LoginService loginService;

    /**
     * client_id : 클라이언트를 구글에 등록했을때 발급받은 값
     * redirect_uri : 사전에 등록한 구글 로그인 성공 후 리다이렉트 될 경로
     * response_type=code : 로그인 성공시 구글에서 code(access_code) 값을 리턴할것을 요구
     * scope : 권한 요청 범위
     * access_type=offline : refresh token 발급 (사용자가 오프라인 상태일때도 클라이언트가 리소스 서버에게 권한을 요청할수 있게 됨)
     * @return : 구글에게 요청 보낼 url
     */
    public String getReqUrl() {
        return kakaoLoginUrl +
                "?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUrl +
                "&response_type=code" +
                "&scope=profile_nickname";

    }

    /**
     * autoCode 구글에 보내서 access_token 얻는다
     * @param authCode : 구글에 보낼 authorization code
     * @return : 구글에게 받은 token 포함된 GoogleLoginResponse
     */
    public KakaoLoginResponse requestAccessTokenToResourceServer(String authCode) {
        // token 받기 위해 구글에 보낼 authorization_code 포함하는 GoogleOAuthRequest 객체 생성
        KakaoOAuthRequest kakaoOAuthRequest = KakaoOAuthRequest
                .builder()
                .grant_type("authorization_code")
                .client_id(kakaoClientId)
                .redirect_uri(kakaoRedirectUrl)
                .code(authCode)
                .content_type("application/x-www-form-urlencoded")
//                .client_secret(kakaoClientSecret)
                .build();

        // .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE + ";charset=UTF-8")

        // WebClient 에 googleOAuthRequest 담아서 요청 보내고 응답 받는다
        WebClient webClient = WebClient.builder()
//                .defaultHeader("Content-type", "application/x-www-form-urlencoded")
//                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED)
                .baseUrl(kakaoAuthUrl) // api 요청 base path
                .build();


        ResponseEntity<KakaoLoginResponse> apiResponse = webClient.post().uri(uriBuilder -> uriBuilder.path("/token").build())
                .header("key","value").contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(kakaoOAuthRequest)
                .retrieve()
                .toEntity(KakaoLoginResponse.class)
                .block();

        System.out.println("getBody");
        System.out.println(apiResponse.getBody());

        return apiResponse.getBody();
    }

}
