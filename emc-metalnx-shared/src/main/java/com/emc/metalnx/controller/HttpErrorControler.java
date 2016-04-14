package com.emc.metalnx.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@RequestMapping(value = "/httpError")
public class HttpErrorControler {

    private static final Logger logger = LoggerFactory.getLogger(HttpErrorControler.class);

    /**
     * Responds the error 404
     * 
     * @param model
     * @return the error 404 custom page
     */
    @RequestMapping(value = "/404")
    public String show404CustomizedPage(Model model) {
        logger.error("404 - Page not found");

        return "errors/404";
    }

    /**
     * Responds the error 403
     * 
     * @param model
     * @return the error 403 custom page
     */
    @RequestMapping(value = "/403")
    public String show403CustomizedPage(Model model) {
        logger.error("403 - Access denied");

        return "errors/403";
    }

    /**
     * Responds the error 500
     * 
     * @param model
     * @return the error 500 custom page
     */
    @RequestMapping(value = "/500")
    public String show500CustomizedPage(Model model) {
        logger.error("500 - Internal Server Error");

        return "errors/500";
    }

    /**
     * Responds the server not respoding
     * 
     * @param model
     * @return the server not responding error custom page
     */
    @RequestMapping(value = "/serverNotResponding")
    public String showServerNotRespondingCustomizedPage(Model model) {
        logger.error("503 - Connect Exception (iCAT Server not responding)");

        return "errors/serverNotResponding";
    }
}
