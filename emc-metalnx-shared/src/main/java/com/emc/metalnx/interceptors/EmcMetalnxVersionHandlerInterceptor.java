package com.emc.metalnx.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.emc.metalnx.utils.EmcMetalnxVersion;

public class EmcMetalnxVersionHandlerInterceptor extends HandlerInterceptorAdapter {

    @Override
    public void postHandle(final HttpServletRequest request, final HttpServletResponse response, final Object handler,
            final ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            EmcMetalnxVersion emcmetalnxVersion = new EmcMetalnxVersion();
            modelAndView.getModelMap().addAttribute("emcmetalnxVersion", emcmetalnxVersion);
        }
    }

}
