package web.instaweb.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleOAuthRequest;
import web.instaweb.dto.GoogleUserInfoDto;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Service
@Transactional(readOnly = false)
@RequiredArgsConstructor
@Slf4j
public class OAuthService {
    @Value("${google.auth.url}")
    private String googleAuthUrl;
    @Value("${google.login.url}")
    private String googleLoginUrl;
    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.redirect.url}")
    private String googleRedirectUrl;
    @Value("${google.secret}")
    private String googleClientSecret;

    private final MemberService memberService;
    private final LoginService loginService;

    /**
     * @return : 구글에게 요청 보낼 url
     */
    public String getReqUrl() {
        String reqUrl = googleLoginUrl + "/o/oauth2/v2/auth?client_id=" +
                googleClientId + "&redirect_uri=" +
                googleRedirectUrl + "&response_type=code&scope=email%20profile%20openid&access_type=offline";

        log.info("myLog-ClientId : {}",googleClientId);
        log.info("myLog-RedirectUrl : {}",googleRedirectUrl);
        return reqUrl;
    }

    public GoogleUserInfoDto getGoogleUserInfoDto(String authCode) {
        // 구글에 등록된 클라이언트 설정정보를 보내어 약속된 access_token 받기위한 객체 생성
        GoogleOAuthRequest googleOAuthRequest = GoogleOAuthRequest
                .builder()
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .code(authCode)
                .redirectUri(googleRedirectUrl)
                .grantType("authorization_code")
                .build();

        RestTemplate restTemplate = new RestTemplate();

        // RestTemplate 을 통해 구글에 access_token 요청
        ResponseEntity<GoogleLoginResponse> apiResponse = restTemplate.postForEntity(googleAuthUrl + "/token", googleOAuthRequest, GoogleLoginResponse.class);
        // 받은 토큰을 토큰객체에 저장
        GoogleLoginResponse googleLoginResponse = apiResponse.getBody();

        log.info("responseBody {}",googleLoginResponse.toString());

        String googleToken = googleLoginResponse.getId_token();
        // 구글에 보낼 requestUrl 생성 (토큰 포함)
        String requestUrl = UriComponentsBuilder.fromHttpUrl(googleAuthUrl + "/tokeninfo").queryParam("id_token",googleToken).toUriString();
        // 받은 토큰을 구글에 보내 유저정보를 얻고
        // 허가된 토큰의 유저정보를 결과로 받는다.
//        String resultJson = restTemplate.getForObject(requestUrl, String.class);

        ResponseEntity<GoogleUserInfoDto> googleUserInfoDtoResponseEntity = restTemplate.getForEntity(requestUrl, GoogleUserInfoDto.class);
        GoogleUserInfoDto googleUserInfoDto = googleUserInfoDtoResponseEntity.getBody();
        log.info("googleUserInfoDto {}", googleUserInfoDto.toString());

        return googleUserInfoDto;
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
