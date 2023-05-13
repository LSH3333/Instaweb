package web.instaweb.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class MemberCheckInterceptor implements HandlerInterceptor {


    /**
     * 로그인 상태라면 loginMemberId attribute 추가 (side-tab 에서 사용)
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HttpSession session = request.getSession();
        // 로그인 상태라면 모델에 loginMemberId 추가
        if(session != null && session.getAttribute(SessionConst.LOGIN_MEMBER) != null) {
            Member loginMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
            request.setAttribute("loginMemberId", loginMember.getId());
        }
    }
}
