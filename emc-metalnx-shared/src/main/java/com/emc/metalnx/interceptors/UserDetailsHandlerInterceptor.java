package com.emc.metalnx.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.emc.metalnx.services.auth.UserTokenDetails;

public class UserDetailsHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler, final ModelAndView modelAndView)
            throws Exception {

        if (modelAndView != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // add user details only if the user is logged
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                UserTokenDetails userTokenDetails = (UserTokenDetails) authentication.getDetails();
                modelAndView.getModelMap().addAttribute("userDetails", userTokenDetails.getUser());
            }
        }
    }

}
