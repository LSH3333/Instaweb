package web.instaweb.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.KakaoLoginResponse;
import web.instaweb.service.KakaoOAuthService;

import javax.servlet.http.HttpServletRequest;

@Controller
@RequiredArgsConstructor
@Slf4j
public class KakaoOAuthController {

    private final KakaoOAuthService kakaoOAuthService;

    @GetMapping("/login/kakaoOAuthLogin")
    public String loginKakaoOAuth() {
        String reqUrl = kakaoOAuthService.getReqUrl();
        return "redirect:" + reqUrl;
    }

    @GetMapping("/login/kakaoOAuth")
    public String kakaoOAuth(@RequestParam(value = "code") String authCode, HttpServletRequest request) {
        System.out.println("kakaoOAuthh");
        System.out.println("authCode = " + authCode);

        KakaoLoginResponse kakaoLoginResponse = kakaoOAuthService.requestAccessTokenToResourceServer(authCode);
        String access_token = kakaoLoginResponse.getAccess_token();
        System.out.println("access_token = " + access_token);

        return "home";
    }
}
