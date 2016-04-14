package com.emc.metalnx.interceptors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.emc.metalnx.modelattribute.enums.URLMap;

/**
 * This class overrides the post handle method of HandlerInterceptorAdapter in order to add
 * an URLMap attribute to all models. This attribute contains all urls necessary for the website.
 * @author guerra
 *
 */

public class URLHandlerInterceptor extends HandlerInterceptorAdapter {
	
	/**
	 * Add an URLMap attribute to all models
	 */
	@Override
    public void postHandle(final HttpServletRequest request,
            final HttpServletResponse response, final Object handler,
            final ModelAndView modelAndView) throws Exception {

        if (modelAndView != null) {
            URLMap urlMap = new URLMap();
			modelAndView.getModelMap().addAttribute("urlMap", urlMap);
        }
    }

}
