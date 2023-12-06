package web.instaweb.service.OAuth;

import lombok.RequiredArgsConstructor;
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
import web.instaweb.domain.Member;
import web.instaweb.dto.OAuth.*;
import web.instaweb.service.LoginService;
import web.instaweb.service.MemberService;

import javax.servlet.http.HttpServletRequest;

@Service
@Transactional(readOnly = false)
public class OAuthKakaoService extends OAuthLoginService {

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
    @Value("${KAKAO_USERINFO_URL}")
    private String kakaoUserInfoUrl;

    @Autowired
    public OAuthKakaoService(MemberService memberService, LoginService loginService) {
        super(memberService, loginService);
    }


    @Override
    public String getType() {
        return "kakao";
    }

    @Override
    public String getReqUrl() {
        return kakaoLoginUrl +
                "?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUrl +
                "&response_type=code" +
                "&scope=profile_nickname";
    }

    @Override
    public OAuthLoginResponse requestAccessTokenToResourceServer(String authCode) {
        /////////
        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoAuthUrl) // api 요청 base path
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "authorization_code");
        formData.add("client_id", kakaoClientId);
        formData.add("redirect_uri", kakaoRedirectUrl);
        formData.add("code", authCode);

        // kakao 는 google 과 다르게 form 형식으로만 받음
        ResponseEntity<KakaoLoginResponse> apiResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/token").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toEntity(KakaoLoginResponse.class)
                .block();

        return apiResponse.getBody();
    }

    @Override
    public OAuthUserInfoDto getUserInfoFromResourceServer(String token) {
        // 받은 토큰을 리소스 서버에 보내 유저정보를 얻고
        // 허가된 토큰의 유저정보를 결과로 받는다.

        // 리소스 서버에 access_token 요청
        WebClient webClient = WebClient.builder()
                .baseUrl(kakaoUserInfoUrl)
                .build();

        ResponseEntity<KakaoUserInfoDto> kakaoUserInfoDto = webClient
                .post()
                .uri(uriBuilder -> uriBuilder.path("/v2/user/me").build())
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .retrieve()
                .toEntity(KakaoUserInfoDto.class)
                .block();

        return kakaoUserInfoDto.getBody();
    }


    @Override
    public String getLoginId(OAuthUserInfoDto oAuthUserInfoDto) {
        KakaoUserInfoDto kakaoUserInfoDto = (KakaoUserInfoDto) oAuthUserInfoDto;
        return kakaoUserInfoDto.getId().toString();
    }

    @Override
    public String getUserName(OAuthUserInfoDto oAuthUserInfoDto) {
        KakaoUserInfoDto kakaoUserInfoDto = (KakaoUserInfoDto) oAuthUserInfoDto;
        return kakaoUserInfoDto.getProperties().get("nickname").toString();
    }
}
