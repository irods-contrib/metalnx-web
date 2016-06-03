/*
 *    Copyright (c) 2015-2016, EMC Corporation
 *
 * 	Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.emc.metalnx.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridGroup;
import com.emc.metalnx.core.domain.entity.UserProfile;
import com.emc.metalnx.modelattribute.user.profile.UserProfileForm;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.UserProfileService;

@Controller
@RequestMapping(value = "/users/profile")
public class UserProfileController {

    @Autowired
    private UserProfileService userProfileService;

    @Autowired
    private GroupService groupService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    private List<String> groupIdsList = new ArrayList<String>();
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @RequestMapping(value = "/")
    public String index(Model model) {
        return "userProfile/index";
    }

    @RequestMapping(value = "/find/{query}/")
    public String find(Model model, @PathVariable String query) {
        List<UserProfile> userProfiles = userProfileService.findByQueryString(query);
        model.addAttribute("userProfiles", userProfiles);
        model.addAttribute("foundUserProfiles", userProfiles.size() > 0);
        model.addAttribute("resultSize", userProfiles.size());
        model.addAttribute("queryString", query);
        return "userProfile/userProfileList :: userProfileList";
    }

    @RequestMapping(value = "/findAll/")
    public String findAll(Model model) {
        List<UserProfile> userProfiles = userProfileService.findAll();
        model.addAttribute("userProfiles", userProfiles);
        model.addAttribute("resultSize", userProfiles.size());
        model.addAttribute("foundUserProfiles", userProfiles.size() >= 0);
        return "userProfile/userProfileList :: userProfileList";
    }

    @RequestMapping(value = "/create/")
    public String showCreateForm(Model model) {
        UserProfileForm userProfileForm = new UserProfileForm();
        List<DataGridGroup> groups = groupService.findAll();

        groupIdsList = new ArrayList<String>();
        model.addAttribute("userProfileForm", userProfileForm);
        model.addAttribute("groups", groups);
        model.addAttribute("groupsOnProfileList", new String[0]);
        model.addAttribute("resultSize", groups.size());
        model.addAttribute("foundGroups", groups.size() > 0);
        model.addAttribute("requestMapping", "/users/profile/create/action/");

        return "userProfile/userProfileForm";
    }

    /**
     * adds a group to the list that is supposed to contain groups of a profile
     *
     * @param groupId
     */
    @RequestMapping(value = "addGroupToSaveList/")
    @ResponseStatus(value = HttpStatus.OK)
    public void addGroupToSaveList(@RequestParam("groupId") String groupId) {
        groupIdsList.add(groupId);
    }

    /**
     * removes a group from the list that is supposed to contain groups of a profile
     *
     * @param groupId
     */
    @RequestMapping(value = "removeGroupToSaveList/")
    @ResponseStatus(value = HttpStatus.OK)
    public void removeGroupToSaveList(@RequestParam("groupId") String groupId) {
        groupIdsList.remove(groupId);
    }

    @RequestMapping(value = "/create/action/")
    public String createUserProfile(@ModelAttribute UserProfileForm userProfileForm, Model model, HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        UserProfile newUserProfile = new UserProfile();
        newUserProfile.setProfileName(userProfileForm.getProfileName());
        newUserProfile.setDescription(userProfileForm.getDescription());

        // Getting group list from UI
        // String[] groupList = request.getParameterValues("groupIdsList");
        String[] groupList = groupIdsList.toArray(new String[groupIdsList.size()]);
        List<DataGridGroup> groups = new ArrayList<DataGridGroup>();
        if (groupList != null && groupList.length != 0) {
            groups = groupService.findByIdList(groupList);
        }
        Set<DataGridGroup> groupSet = new HashSet<DataGridGroup>(groups);

        Long userProfileId = userProfileService.createUserProfile(newUserProfile);
        newUserProfile = userProfileService.findById(userProfileId);

        newUserProfile.setGroups(groupSet);
        userProfileService.modifyUserProfile(newUserProfile);

        if (newUserProfile != null) {
            redirectAttributes.addFlashAttribute("userProfileAddedSuccessfully", newUserProfile.getProfileName());
        }

        return "redirect:/users/profile/";
    }

    @RequestMapping(value = "/modify/{profileId}/")
    public String showModifyForm(Model model, @PathVariable String profileId) {
        UserProfile userProfile = userProfileService.findById(Long.valueOf(profileId));
        List<DataGridGroup> groups = groupService.findAll();

        UserProfileForm userProfileForm = new UserProfileForm();

        if (userProfile != null) {
            userProfileForm.setProfileId(String.valueOf(userProfile.getProfileId()));
            userProfileForm.setProfileName(userProfile.getProfileName());
            userProfileForm.setDescription(userProfile.getDescription());

            int groupsQuantity = userProfile.getGroups().size(), i = 0;
            String[] groupsOnProfileList = new String[groupsQuantity];
            for (DataGridGroup group : userProfile.getGroups()) {
                groupsOnProfileList[i++] = String.valueOf(group.getId());
            }

            model.addAttribute("groupsOnProfileList", groupsOnProfileList);
            groupIdsList = new ArrayList<String>(Arrays.asList(groupsOnProfileList));
        }

        model.addAttribute("userProfileForm", userProfileForm);

        model.addAttribute("groups", groups);
        model.addAttribute("resultSize", groups.size());
        model.addAttribute("foundGroups", groups.size() > 0);
        model.addAttribute("requestMapping", "/users/profile/modify/action/");

        return "userProfile/userProfileForm";
    }

    @RequestMapping(value = "/modify/action/")
    public String modifyUserProfile(@ModelAttribute UserProfileForm userProfileForm, HttpServletRequest request, RedirectAttributes redirectAttributes) {

        // Getting group list from UI
        // String[] groupList = request.getParameterValues("groupIdsList");
        String[] groupList = groupIdsList.toArray(new String[groupIdsList.size()]);
        List<DataGridGroup> groups = new ArrayList<DataGridGroup>();
        if (groupList != null && groupList.length != 0) {
            groups = groupService.findByIdList(groupList);
        }
        Set<DataGridGroup> groupSet = new HashSet<DataGridGroup>(groups);

        // Updating the UserProfile entity
        UserProfile userProfile = userProfileService.findById(Long.valueOf(userProfileForm.getProfileId()));
        userProfile.setProfileName(userProfileForm.getProfileName());
        userProfile.setDescription(userProfileForm.getDescription());
        userProfile.setGroups(groupSet);

        userProfileService.modifyUserProfile(userProfile);

        if (userProfile != null) {
            redirectAttributes.addFlashAttribute("userProfileModifiedSuccessfully", userProfile.getProfileName());
        }

        return "redirect:/users/profile/";
    }

    @RequestMapping(value = "/remove/{profileId}/")
    public String remove(@PathVariable String profileId, Model model, RedirectAttributes redirectAttributes) {
        UserProfile profileToRemove = userProfileService.findById(Long.valueOf(profileId));
        String profileName = profileToRemove.getProfileName();
        userProfileService.removeUserProfile(profileToRemove);
        redirectAttributes.addFlashAttribute("profileRemovedSuccessfully", profileName);
        return "redirect:/users/profile/";
    }

    @RequestMapping(value = "/profilesToCSVFile/")
    public void groupsToCSVFile(HttpServletResponse response) {
        String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String filename = String.format("user_profiles_%s_%s.csv", loggedUser, date);

        // Setting CSV Mime type
        response.setContentType("text/csv");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);

        List<UserProfile> profiles = userProfileService.findAll();
        List<String> rows = new ArrayList<String>();
        rows.add("UserProfileName;Description;Groups\n");

        for (UserProfile profile : profiles) {
            rows.add(profile.getProfileName() + ";");
            rows.add(profile.getDescription() + ";");

            String groupNames = StringUtils.join(profile.getGroups().toArray(), " ");
            rows.add(groupNames);

            rows.add("\n");
        }

        try {
            ServletOutputStream outputStream = response.getOutputStream();

            // Writing CSV file
            Iterator<String> fileIterator = rows.iterator();
            while (fileIterator.hasNext()) {
                outputStream.print(fileIterator.next());
            }
            outputStream.flush();
        }
        catch (IOException e) {
            logger.error("Could not generate CSV file for groups", e);
        }
    }

    /*
     * ********************************************************************************************
     * ********************************* VALIDATION *********************************************
     * ********************************************************************************************
     */

    /**
     * Validates a profile name against our database
     *
     * @param profileName
     *            to be validated
     * @return true, if the profile name can be used. False, otherwise.
     */
    @ResponseBody
    @RequestMapping(value = "isValidProfileName/{profileName}/", method = RequestMethod.GET)
    public String isValidUsername(@PathVariable String profileName) {

        if (profileName.compareTo("") != 0) {
            // if no profiles are found with this profile Name, it means this profile Name can be used
            List<UserProfile> userProfiles = userProfileService.findAll();
            for (UserProfile userProfile : userProfiles) {
                if (userProfile.getProfileName().compareTo(profileName) == 0) {
                    return "false";
                }
            }
            return "true";
        }

        return "false";
    }
}
