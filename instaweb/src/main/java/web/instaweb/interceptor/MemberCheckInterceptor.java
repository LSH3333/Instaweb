package web.instaweb.interceptor;

import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import web.instaweb.SessionConst;
import web.instaweb.domain.Member;
import web.instaweb.domain.Page;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Objects;

public class MemberCheckInterceptor implements HandlerInterceptor {

    // 현재 로그인된 맴버가 Page 의 주인 아닐때
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        if (handler instanceof HandlerMethod) {
//            HandlerMethod handlerMethod = (HandlerMethod) handler;
//            Method method = handlerMethod.getMethod(); // request 처리할 controller method
//
//            // Get the controller method's parameters
//            Parameter[] parameters = method.getParameters();
//            for (Parameter parameter : parameters) {
//                // Check if the parameter has the @ModelAttribute annotation
//                if (parameter.getAnnotation(ModelAttribute.class) != null) {
//                    // Get the page object from the model
//                    Page page = (Page) request.getAttribute("page");
//
//                    // Get the member from the page object
//                    Member member = page.getMember();
//
//                    // Check if the member is the same as the logged-in member
//                    HttpSession session = request.getSession();
//                    Member loggedInMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
//                    if (!member.equals(loggedInMember)) {
//                        // Redirect the user to an error page or login page
//                        response.sendRedirect("/error");
//                        return false;
//                    }
//                }
//            }
//        }
//
//        return true; // request 계속 처리
//    }

//    @Override
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//        if (modelAndView != null) {
//            String requestURI = request.getRequestURI();
//            HttpSession session = request.getSession();
//            // 현재 세션에서 로그인 한 member
//            Member loggedInMember = (Member) session.getAttribute(SessionConst.LOGIN_MEMBER);
//            // page 의 주인인 member
//            Member member = (Member) modelAndView.getModelMap().get("member");
//
//            System.out.println("MemberCheckInterceptor.postHandle()");
//            System.out.println("loggedInMember.getId() = " + loggedInMember.getId());
//            System.out.println("member = " + member);
//            System.out.println("member.getId() = " + member.getId());
//
//            if (!Objects.equals(loggedInMember.getId(), member.getId())) {
//                response.sendRedirect("/login?redirectURL=" + requestURI);
//            }
//
//        }
//    }


}
