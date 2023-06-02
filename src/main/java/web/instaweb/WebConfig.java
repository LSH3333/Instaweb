package web.instaweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import web.instaweb.interceptor.LoginCheckInterceptor;
import web.instaweb.interceptor.MemberCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 스프링 인터셉터 추가
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 경로 닫아 놓고, 특정 경로만 열어 놓음

        // Login 여부 확인 인터셉터
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                // 로그인 되어있어야 접근 가능한 경로
                .addPathPatterns("/**")
                // 로그인 안되어있어도 접근 가능 경로
                .excludePathPatterns("/", "/members/register", "/login", "/logout", "/css/**", "/*.ico", "/error",
                        "/*/pages", // pageList 볼 수 있음,
                        "/*/pages/*", // 글 볼 수 있음
                        "/pages/ajaxReq", "/view/ajaxReq", // ajax 경로도 제외해줘야함
                        "/allPages", "/allPages/ajaxReq");

        // 로그인 상태라면 postHandle 에서 loginMemberId attribute 에 저장
        registry.addInterceptor(new MemberCheckInterceptor())
                .order(2)
                .addPathPatterns("/**");


    }

}
