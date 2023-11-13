package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleUserInfoDto;
import web.instaweb.service.OAuthService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class OAuthController {

    private final OAuthService oAuthService;


    /**
     * 구글 로그인 버튼 누를시 요청 경로
     * @return : 구글에게 로그인 페이지 요청할 경로로 리다이렉트
     */
    @GetMapping("/login/getAuthUrl")
    public String getGoogleAuthUrl() {
        // 구글에게 요청 보낼 경로
        String reqUrl = oAuthService.getReqUrl();
        // reqUrl 로 요청하면 구글 로그인 창으로 이동함
        // 사용자가 로그인 성공하면 지정된 redirect_uri 로 리다이렉트됨
        return "redirect:" + reqUrl;
    }


    /**
     * 유저 구글 로그인 이후 리다이렉트 되는 경로
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
        GoogleLoginResponse googleLoginResponse = oAuthService.requestAccessTokenToResourceServer(authCode);
        // 받은 access_token 구글에 보내 유저정보를 얻는다
        String googleToken = googleLoginResponse.getAccess_token();
        GoogleUserInfoDto googleUserInfoDto = oAuthService.getUserInfoFromGoogle(googleToken);

        // 유저 정보를 기반으로 로그인 시도한다
        oAuthService.loginGoogle(googleUserInfoDto, request);
        return "home";
    }


}