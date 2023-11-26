package web.instaweb.service;

import lombok.RequiredArgsConstructor;
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
import web.instaweb.dto.KakaoUserInfoDto;
import web.instaweb.dto.OAuthServerResponse;

import javax.servlet.http.HttpServletRequest;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
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
    @Value("${KAKAO_USERINFO_URL}")
    private String kakaoUserInfoUrl;

    private final MemberService memberService;
    private final LoginService loginService;

    /**
     * client_id : 클라이언트를 Resource Server 에 등록했을때 발급받은 값
     * redirect_uri : 사전에 등록한 OAuth 로그인 성공 후 리다이렉트 될 경로
     * response_type=code : 로그인 성공시 Resource Server 에 code(access_code) 값을 리턴할것을 요구
     * scope : 권한 요청 범위
     * @return : Resource Server 에 요청 보낼 url
     */
    public String getReqUrl() {
        return kakaoLoginUrl +
                "?client_id=" + kakaoClientId +
                "&redirect_uri=" + kakaoRedirectUrl +
                "&response_type=code" +
                "&scope=profile_nickname";
    }

    /**
     * autoCode Resource Server 에 보내서 access_token 얻는다
     * @param authCode : Resource Server 에 보낼 authorization code
     * @return : Resource Server 에게 받은 token 포함된 Response 객체
     */
    public OAuthServerResponse requestAccessTokenToResourceServer(String authCode) {
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
        ResponseEntity<OAuthServerResponse> apiResponse = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/token").build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .toEntity(OAuthServerResponse.class)
                .block();

        return apiResponse.getBody();
    }


    /**
     * kakao 는 json 으로 반환됨
     * access_token 을 Resource Server 에 보내 유저 정보 응답 받는다
     * @param token : Resource Server 에게 발급받은 access_token
     * @return : Resource Server 에게 응답 받은 유저 정보 포함된 Dto
     */
    public KakaoUserInfoDto getUserInfoFromKakao(String token) {
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


    /**
     *
     * @param kakaoUserInfoDto : Resource Server 에게서 받은 유저 정보 포함하는 로그인 처리 필요한 유저 정보 Dto
     * @param request : HttpServletRequest
     * @return : 로그인 성공했다면 로그인된 Member 객체, 실패했다면 null
     */
    public Member loginKakao(KakaoUserInfoDto kakaoUserInfoDto, HttpServletRequest request) {
        // 존재하지 않는 맴버 -> 회원가입 진행 -> 로그인 진행
        if (!memberService.checkLoginIdDuplication(kakaoUserInfoDto.getId().toString())) {
            // 회원가입 진행
            Member registeredMember = memberService.registerNewMember(kakaoUserInfoDto.getId().toString(), kakaoUserInfoDto.getId().toString(), kakaoUserInfoDto.getProperties().get("nickname").toString());
            // 로그인 진행 성공
            if (loginService.loginOAuth(registeredMember)) {
                loginService.regSession(request, registeredMember);
                return registeredMember;
            }
        }
        // 존재하는 맴버 -> 로그인 진행
        else {
            Member member = memberService.getMemberWithLoginId(kakaoUserInfoDto.getId().toString());
            // 로그인 진행 성공
            if (loginService.loginOAuth(member)) {
                loginService.regSession(request, member);
                return member;
            }
        }
        return null;
    }
}
