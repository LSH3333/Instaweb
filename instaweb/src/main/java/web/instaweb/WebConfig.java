package web.instaweb;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import web.instaweb.interceptor.LoginCheckInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 스프링 인터셉터 추가
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 경로 열어놓고, 특정 경로만 닫음
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(1)
                .addPathPatterns("/**")
                .excludePathPatterns("/", "/members/register", "/login", "/logout", "/css/**", "/*.ico", "/error");
    }

}
