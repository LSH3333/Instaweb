package web.instaweb.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import web.instaweb.SessionConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MemberCheckInterceptor implements HandlerInterceptor {

    // 현재 로그인된 맴버가 Page 의 주인 아닐때
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        HttpSession session = request.getSession();



        return true; // request 계속 처리
    }
}
