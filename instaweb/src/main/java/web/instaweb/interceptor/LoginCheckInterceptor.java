package web.instaweb.interceptor;


import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import web.instaweb.SessionConst;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class LoginCheckInterceptor implements HandlerInterceptor {

    /**
     * preHandle : 컨트롤러 호출 전 호출됨
     *
     * request 에 session 존재하지 않으면 로그인 안된 상태 -> request 가 온 uri 이용해서 해당 뷰로 리다이렉트함
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();
        HttpSession session = request.getSession();

        // 로그인 안된 경우
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false; // request 종료
        }

        return true; // request 계속 처리
    }


}
