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
    @Value("${GOOGLE_AUTH_URL}")
    private String googleAuthUrl;
    @Value("${GOOGLE_LOGIN_URL}")
    private String googleLoginUrl;
    @Value("${GOOGLE_CLIENT_ID}")
    private String googleClientId;
    @Value("${GOOGLE_REDIRECT_URL}")
    private String googleRedirectUrl;
    @Value("${GOOGLE_SECRET_CLIENT}")
    private String googleClientSecret;



    private final MemberService memberService;
    private final LoginService loginService;

    /**
     * @return : 구글에게 요청 보낼 url
     */
    public String getReqUrl() {
        return googleLoginUrl + "/o/oauth2/v2/auth?client_id=" +
                googleClientId + "&redirect_uri=" +
                googleRedirectUrl + "&response_type=code&scope=email%20profile%20openid&access_type=offline";
    }

    /**
     * autoCode 구글에 보내서 access_token 얻고, access_token 구글에 보내서 유저 정보 받는다
     * @param authCode : 구글에 보낼 authorization code
     * @return : 구글에게 리턴 받은 사용자 정보 포함된 GoogleUserInfoDto
     */
    public GoogleUserInfoDto getGoogleUserInfoDto(String authCode) {
        // authorization code 포함 정보들 구글에 보내고, token 담긴 response 얻는다
        GoogleLoginResponse googleLoginResponse = getGoogleLoginResponse(authCode);

        // 받은 access_token 구글에 보내 유저정보를 얻는다
        String googleToken = googleLoginResponse.getAccess_token();
        return getUserInfoFromGoogle(googleToken);
    }

    /**
     * autoCode 구글에 보내서 token 얻는다
     * @param authCode : 구글에 보낼 authorization code
     * @return : 구글에게 받은 token 포함된 GoogleLoginResponse
     */
    private GoogleLoginResponse getGoogleLoginResponse(String authCode) {
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
     * token 을 구글에 보내 유저 정보 응답 받는다
     * @param googleToken : 구글에게 발급받은 id_token
     * @return : 구글에게 응답 받은 유저 정보 포함된 GoogleUserInfoDto
     */
    private GoogleUserInfoDto getUserInfoFromGoogle(String googleToken) {
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
