package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import web.instaweb.domain.Member;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleOAuthRequest;
import web.instaweb.dto.GoogleUserInfoDto;
import javax.servlet.http.HttpServletRequest;


@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    // 노출되면 안되는 secret key 들은 환경변수로 등록해서 사용
    // 리소서 서버에 로그인 요청 url, 응답으로 auth_code 받음
    @Value("${GOOGLE_LOGIN_URL}")
    private String googleLoginUrl;
    // 리소스 서버에 access_token 요청 url
    @Value("${GOOGLE_AUTH_URL}")
    private String googleAuthUrl;
    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;
    @Value("${GOOGLE_REDIRECT_URL}")
    private String googleRedirectUrl;
    @Value("${GOOGLE_SECRET_CLIENT}")
    private String googleClientSecret;



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
        return googleLoginUrl + "/o/oauth2/v2/auth" +
                "?client_id=" + googleClientId +
                "&redirect_uri=" + googleRedirectUrl +
                "&response_type=code" +
                "&scope=email%20profile%20openid" +
                "&access_type=offline";
    }


    /**
     * autoCode 구글에 보내서 access_token 얻는다
     * @param authCode : 구글에 보낼 authorization code
     * @return : 구글에게 받은 token 포함된 GoogleLoginResponse
     */
    public GoogleLoginResponse requestAccessTokenToResourceServer(String authCode) {
        // token 받기 위해 구글에 보낼 authorization_code 포함하는 GoogleOAuthRequest 객체 생성
        GoogleOAuthRequest googleOAuthRequest = GoogleOAuthRequest
                .builder()
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .code(authCode)
                .redirectUri(googleRedirectUrl)
                .grantType("authorization_code")
                .build();


        // WebClient 에 googleOAuthRequest 담아서 요청 보내고 응답 받는다
        WebClient webClient = WebClient.builder()
                .baseUrl(googleAuthUrl) // api 요청 base path
                .build();

        ResponseEntity<GoogleLoginResponse> apiResponse = webClient.post().uri(uriBuilder -> uriBuilder.path("/token").build())
                .bodyValue(googleOAuthRequest)
                .header("from", "client")
                .retrieve()
                .toEntity(GoogleLoginResponse.class) // GoogleLoginResponse 로 받는다
                .block();

        return apiResponse.getBody();
    }

    /**
     * access_token 을 구글에 보내 유저 정보 응답 받는다
     * @param googleToken : 구글에게 발급받은 access_token
     * @return : 구글에게 응답 받은 유저 정보 포함된 GoogleUserInfoDto
     */
    public GoogleUserInfoDto getUserInfoFromGoogle(String googleToken) {
        // 받은 토큰을 구글에 보내 유저정보를 얻고
        // 허가된 토큰의 유저정보를 결과로 받는다.

        // 구글에 access_token 요청
        WebClient webClient = WebClient.builder()
                .baseUrl(googleAuthUrl)
                .defaultHeader("from", "client")
                .build();

        ResponseEntity<GoogleUserInfoDto> googleUserInfoDtoResponseEntity = webClient.get().uri(uriBuilder -> uriBuilder.path("/tokeninfo").queryParam("access_token",googleToken).build())
                .retrieve()
                .toEntity(GoogleUserInfoDto.class)
                .block();

        return googleUserInfoDtoResponseEntity.getBody();
    }


    /**
     *
     * @param googleUserInfoDto : 구글에게서 받은 유저 정보 포함하는 로그인 처리 필요한 유저 정보 Dto
     * @param request : HttpServletRequest
     * @return : 로그인 성공했다면 로그인된 Member 객체, 실패했다면 null
     */
    public Member loginGoogle(GoogleUserInfoDto googleUserInfoDto, HttpServletRequest request) {
        // 존재하지 않는 맴버 -> 회원가입 진행 -> 로그인 진행
        if (!memberService.checkLoginIdDuplication(googleUserInfoDto.getEmail())) {
            // 회원가입 진행
            Member registeredMember = memberService.registerNewMember(googleUserInfoDto.getEmail(), googleUserInfoDto.getEmail(), googleUserInfoDto.getName());
            // 로그인 진행 성공
            if (loginService.loginOAuth(registeredMember)) {
                loginService.regSession(request, registeredMember);
                return registeredMember;
            }
        }
        // 존재하는 맴버 -> 로그인 진행
        else {
            Member member = memberService.getMemberWithLoginId(googleUserInfoDto.getEmail());
            // 로그인 진행 성공
            if (loginService.loginOAuth(member)) {
                loginService.regSession(request, member);
                return member;
            }
        }
        return null;
    }



}
