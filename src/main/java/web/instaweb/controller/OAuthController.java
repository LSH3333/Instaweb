package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleUserInfoDto;
import web.instaweb.dto.KakaoLoginResponse;
import web.instaweb.dto.KakaoUserInfoDto;
import web.instaweb.service.GoogleOAuthService;
import web.instaweb.service.KakaoOAuthService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OAuthController {
    private final GoogleOAuthService googleOAuthService;
    private final KakaoOAuthService kakaoOAuthService;


    ///////////////// GOOGLE

    /**
     * OAuth 로그인 버튼 누를시 요청 경로
     * @return : 리소스 서버 에게 로그인 페이지 요청할 경로로 리다이렉트
     */
    @GetMapping("/login/getAuthUrl")
    public String getGoogleAuthUrl() {
        // 리소스 서버에게 요청 보낼 경로
        String reqUrl = googleOAuthService.getReqUrl();
        // reqUrl 로 요청하면 OAuth 로그인 창으로 이동함
        // 사용자가 로그인 성공하면 지정된 redirect_uri 로 리다이렉트됨
        return "redirect:" + reqUrl;
    }

    /**
     * 유저 OAuth 로그인 이후 리다이렉트 되는 경로
     * 리소스 서버에서 쿼리 파라미터에 authorization_code 포함한 정보 포함해서 내가 등록한 redirect_uri 로 리다이렉트 시킨다
     *
     * 1. authorization_code 포함 정보를 구글에 보내 access_token 요청한다
     * 2. access_token 발급받은 이후 부터 구글에 회원정보등 요청할수 있다
     * 3. 유저 정보 요청하고, 받은 유저 정보를 기반으로 회원가입,로그인 처리한다
     *
     * @param authCode : 리소스 서버에서 쿼리 파라미터로 보내온 authorization_code, 이걸 다시 리소스 서버로 보내서 access_token 요청한다
     * @return : 구글 로그인 후 navigate 될 경로
     */
    @GetMapping("/login/googleOAuth")
    public String googleOAuth(@RequestParam(value = "code") String authCode, HttpServletRequest request) {
        // authorization_code 포함 정보들 구글에 보내고, access_token 담긴 response 얻는다
        GoogleLoginResponse googleLoginResponse = googleOAuthService.requestAccessTokenToResourceServer(authCode);
        // 받은 access_token 구글에 보내 유저정보를 얻는다
        String googleToken = googleLoginResponse.getAccess_token();
        GoogleUserInfoDto googleUserInfoDto = googleOAuthService.getUserInfoFromGoogle(googleToken);

        // 유저 정보를 기반으로 로그인 시도한다
        googleOAuthService.loginGoogle(googleUserInfoDto, request);
        return "home";
    }

    ///////////////// KAKAO

    @GetMapping("/login/kakaoOAuthLogin")
    public String loginKakaoOAuth() {
        String reqUrl = kakaoOAuthService.getReqUrl();
        return "redirect:" + reqUrl;
    }

    @GetMapping("/login/kakaoOAuth")
    public String kakaoOAuth(@RequestParam(value = "code") String authCode, HttpServletRequest request) {
        KakaoLoginResponse kakaoLoginResponse = kakaoOAuthService.requestAccessTokenToResourceServer(authCode);
        String access_token = kakaoLoginResponse.getAccess_token();
        KakaoUserInfoDto userInfoFromKakao = kakaoOAuthService.getUserInfoFromKakao(access_token);

        kakaoOAuthService.loginKakao(userInfoFromKakao, request);
        return "home";
    }
}
