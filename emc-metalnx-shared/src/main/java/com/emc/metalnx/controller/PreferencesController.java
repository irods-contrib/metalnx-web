package com.emc.metalnx.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.LocaleResolver;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.modelattribute.preferences.UserPreferences;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@RequestMapping(value = "/preferences")
public class PreferencesController {

    @Autowired
    private UserService userService;

    @Autowired
    private LocaleResolver localeResolver;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    // ui mode that will be shown when the rods user switches mode from admin to user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    @Value("${irods.zoneName}")
    private String zoneName;

    @RequestMapping(value = "/")
    public String index(Model model, HttpServletRequest request) {
        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        String locale = loggedUser.getLocale();

        String uiMode = (String) request.getSession().getAttribute("uiMode");

        if (uiMode == null || uiMode.isEmpty()) {
            if (loggedUser.isAdmin()) {
                uiMode = UI_ADMIN_MODE;
            }
            else {
                uiMode = UI_USER_MODE;
            }
        }

        UserPreferences userPreferences = new UserPreferences();
        userPreferences.setLocaleLanguage(locale);
        userPreferences.setForceFileOverwriting(loggedUser.isForceFileOverwriting());

        model.addAttribute("preferences", userPreferences);
        model.addAttribute("uiMode", uiMode);

        return "preferences/index";
    }

    @RequestMapping(value = "/action/")
    public String action(Model model, @ModelAttribute UserPreferences preferences, HttpServletRequest request, HttpServletResponse response)
            throws DataGridConnectionRefusedException {

        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        loggedUser.setLocale(preferences.getLocaleLanguage());
        loggedUser.setForceFileOverwriting(preferences.isForceFileOverwriting());
        userService.modifyUser(loggedUser);

        localeResolver.setLocale(request, response, StringUtils.parseLocaleString(preferences.getLocaleLanguage()));
        return "redirect:/dashboard/";
    }

    @RequestMapping(value = "/chat/")
    public String webSocketTest() {
        return "preferences/chat";
    }
}
