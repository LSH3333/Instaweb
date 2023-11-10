package web.instaweb.controller;

import com.sun.tools.jconsole.JConsoleContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import web.instaweb.dto.GoogleLoginResponse;
import web.instaweb.dto.GoogleOAuthRequest;
import org.springframework.web.util.UriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URI;

@RestController
@Slf4j
public class OAuthController {
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


//    @GetMapping("/trymapping")
//    public String trymapping() {
//        log.info("trymapping");
//        return "hi";
//    }

    @GetMapping("/login/getGoogleAuthUrl")
    public ResponseEntity<?> getGoogleAuthUrl(HttpServletRequest request) throws Exception {
        log.info("getGoogleAuthUrl");

        String reqUrl = googleLoginUrl + "/o/oauth2/v2/auth?client_id=" +
                googleClientId + "&redirect_uri=" +
                googleRedirectUrl + "&response_type=code&scope=email%20profile%20openid&access_type=offline";

        log.info("myLog-ClientId : {}",googleClientId);
        log.info("myLog-RedirectUrl : {}",googleRedirectUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(reqUrl));

        // 구글 로그인 창을 띄우고, 로그인 후 login/googleOAuth 로 리다이렉트
        return new ResponseEntity<>(headers, HttpStatus.MOVED_PERMANENTLY);
    }

    // 구글에서 리다이렉션
    @GetMapping("/login/googleOAuth")
    public String googleOAuth(HttpServletRequest request, @RequestParam(value = "code") String authCode, HttpServletResponse response) {
        //2.구글에 등록된 레드망고 설정정보를 보내어 약속된 토큰을 받위한 객체 생성
        GoogleOAuthRequest googleOAuthRequest = GoogleOAuthRequest
                .builder()
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .code(authCode)
                .redirectUri(googleRedirectUrl)
                .grantType("authorization_code")
                .build();

        RestTemplate restTemplate = new RestTemplate();

        //3.토큰요청을 한다.
        ResponseEntity<GoogleLoginResponse> apiResponse = restTemplate.postForEntity(googleAuthUrl + "/token", googleOAuthRequest, GoogleLoginResponse.class);
        //4.받은 토큰을 토큰객체에 저장
        GoogleLoginResponse googleLoginResponse = apiResponse.getBody();

        log.info("responseBody {}",googleLoginResponse.toString());


        String googleToken = googleLoginResponse.getId_token();

        //5.받은 토큰을 구글에 보내 유저정보를 얻는다.
        String requestUrl = UriComponentsBuilder.fromHttpUrl(googleAuthUrl + "/tokeninfo").queryParam("id_token",googleToken).toUriString();

        //6.허가된 토큰의 유저정보를 결과로 받는다.
        String resultJson = restTemplate.getForObject(requestUrl, String.class);

        return resultJson;
    }





//    @RequestMapping(value = "/api/v1/oauth2/google", method = RequestMethod.POST)
//    public String loginUrlGoogle() {
//        String reqUrl =
//                "https://accounts.google.com/o/oauth2/v2/auth?" +
//                        "client_id=" + googleClientId +
//                        "&redirect_uri=http://localhost:8080/login/googleOAuth" +
//                        "&response_type=code" +
//                        "&scope=email%20profile%20openid" +
//                        "&access_type=offline";
//        return reqUrl;
//    }
}
