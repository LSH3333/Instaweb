package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import web.instaweb.domain.Member;
import web.instaweb.dto.GoogleUserInfoDto;
import web.instaweb.service.OAuthService;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;

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
        // 구글 로그인 창을 띄우고, 로그인 후 login/googleOAuth 로 리다이렉트
        return "redirect:/" + reqUrl;
    }


    /**
     * 유저 구글 로그인 이후 리다이렉트 되는 경로
     * OAuth 의 모든 과정 진행
     * 1. authCode 포함 정보를 구글에 보내 access_token, id_token 요청한다
     * 2. 발급 받은 token 으로 id_token 기반 유저 정보 요청한다
     * 3. 받은 유저 정보를 기반으로 회원가입,로그인 처리한다
     *
     * @param authCode : access_token,id_token 을 얻기 위한 authorization code
     * @return : 구글 로그인 후 navigate 될 경로
     */
    @GetMapping("/login/googleOAuth")
    public String googleOAuth(@RequestParam(value = "code") String authCode, HttpServletRequest request) {
        // authCode 를 구글에 보내 유저 정보 반환 받는다
        GoogleUserInfoDto googleUserInfoDto = oAuthService.getGoogleUserInfoDto(authCode);
        // 유저 정보를 기반으로 로그인 시도한다
        oAuthService.loginGoogle(googleUserInfoDto, request);
        return "home";
    }


}
