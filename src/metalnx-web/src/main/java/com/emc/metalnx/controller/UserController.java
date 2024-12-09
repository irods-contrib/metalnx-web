/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.exception.DuplicateDataException;
import org.irods.jargon.core.exception.JargonException;
import org.irods.jargon.core.pub.domain.UserGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.entity.DataGridZone;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.enums.ExceptionEnum;
import com.emc.metalnx.modelattribute.enums.URLMap;
import com.emc.metalnx.modelattribute.user.UserForm;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;
import com.emc.metalnx.services.interfaces.ZoneService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller of all operations applied on users.
 *
 */

@Controller
@SessionAttributes({ "addReadPermissionsOnDirs", "addWritePermissionsOnDirs", "addOwnerOnDirs" })
@RequestMapping("/users")
public class UserController {

	@Autowired
	UserService userService;

	@Autowired
	GroupService groupService;

	@Autowired
	IRODSServices irodsServices;

	@Autowired
	ZoneService zoneService;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	HeaderService headerService;

	URLMap urlMap = new URLMap();
	private List<String> groupsToBeAdded = new ArrayList<String>();

	// Auxiliary structure to manage permissions changes - ADD
	private Map<String, Boolean> addReadPermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> addWritePermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> addOwnerOnDirs = new HashMap<String, Boolean>();

	// Auxiliary structure to manage permissions changes - REMOVE
	private Map<String, Boolean> removeReadPermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> removeWritePermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> removeOwnerOnDirs = new HashMap<String, Boolean>();

	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	/**
	 * It gets all users existing in iRODS and our database and add this list of
	 * users as a parameter to the Model.
	 *
	 * @param model
	 * @return the user-management template
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String listUsers(Model model) {
		logger.info("listUsers()");
		cleanPermissionsSets();
		return "users/user-management";
	}

	/**
	 * List users by page number
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/find/{query}/")
	public String listUsersByPageAndQuery(Model model, @PathVariable String query) {
		List<DataGridUser> users = userService.findByUsername(query);

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usernameLogged = (String) auth.getPrincipal();
		model.addAttribute("usernameLogged", usernameLogged);

		model.addAttribute("users", users);
		model.addAttribute("queryString", query);
		return "users/userList :: userList";
	}

	/**
	 * Queries the user DB to match users with the given query string. Returns JSON
	 * body.
	 *
	 * @param model
	 */
	@RequestMapping(value = "/query/{query}/")
	@ResponseBody
	public String findUserByQueryy(Model model, @PathVariable String query) {
		ObjectMapper mapper = new ObjectMapper();
		List<DataGridUser> users = userService.findByUsername(query);

		List<Map<String, Object>> userList = new ArrayList<Map<String, Object>>();

		for (DataGridUser user : users) {
			Map<String, Object> userMap = new LinkedHashMap<String, Object>();
			userMap.put("username", user.getUsername());
			userList.add(userMap);
		}

		Map<String, Object> resultMap = new LinkedHashMap<String, Object>();
		resultMap.put("results", userList);
		String resultJson = "";
		try {
			resultJson = mapper.writeValueAsString(resultMap);
		} catch (JsonProcessingException e) {
			logger.info("Could not parse Object to Json: ", e.getMessage());
		}

		return resultJson;
	}

	/**
	 * List users by page number
	 *
	 * @param model
	 * @return
	 */
	@RequestMapping(value = "/findAll/")
	public String listAllUsers(Model model) {
		logger.info("findAll()");
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		String usernameLogged = (String) auth.getPrincipal();

		List<DataGridUser> users = userService.findAll();
		Collections.sort(users);

		model.addAttribute("usernameLogged", usernameLogged);
		model.addAttribute("users", users);
		return "users/userList :: userList";
	}

	/**
	 * adds a group to the list that is supposed to contain groups of a user
	 *
	 * @param groupId
	 * @param model
	 */
	@RequestMapping(value = "addGroupToSaveList/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addGroupToSaveList(@RequestParam("groupId") String groupId) {
		logger.info("addGroupToSaveList()");

		if (groupId == null || groupId.isEmpty()) {
			throw new IllegalArgumentException("null or empty groupId");
		}

		logger.info("groupId:{}", groupId);

		groupsToBeAdded.add(groupId);
	}

	/**
	 * removes a group from the list that is supposed to contain groups of a user
	 *
	 * @param groupId
	 * @param model
	 */
	@RequestMapping(value = "removeGroupToSaveList/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeGroupToSaveList(@RequestParam("groupId") String groupId) {
		logger.info("removeGroupToSaveList()");

		if (groupId == null || groupId.isEmpty()) {
			throw new IllegalArgumentException("null or empty groupId");
		}

		logger.info("groupId:{}", groupId);

		groupsToBeAdded.remove(groupId);
	}

	/**
	 * Responds the request for the url "add/". It adds a new UserForm to the Model
	 * and sets the form's action to "add".
	 *
	 * @param model
	 * @return the userForm template
	 * @throws DataGridException
	 */
	@RequestMapping(value = "add/", method = RequestMethod.GET)
	public String showAddUserForm(Model model) throws DataGridException {
		String[] groupsList = new String[1];
		groupsToBeAdded = new ArrayList<String>();

		String currentZone = irodsServices.getCurrentUserZone();
		UserGroup publicGroup = groupService.findByGroupnameAndZone("public", currentZone);
		groupsList[0] = String.valueOf(publicGroup.getUserGroupName());

		model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
		model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
		model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);

		model.addAttribute("user", new UserForm());
		model.addAttribute("groups", groupService.findAll());
		model.addAttribute("zones", zoneService.findAll());
		model.addAttribute("userZone", "");
		model.addAttribute("groupList", groupsList);
		model.addAttribute("requestMapping", "/users/add/action/");
		model.addAttribute("userTypes", userService.listUserTypes());
		return "users/userForm";
	}

	/**
	 * Controller method that executes action 'create user'
	 *
	 * @param user
	 * @return the name of the template to render
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "add/action/", method = RequestMethod.POST)
	public String addUser(@ModelAttribute UserForm user, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws DataGridException {

		logger.info("addUser()");
		if (user == null) {
			throw new IllegalArgumentException("null user");
		}

		DataGridUser newUser = new DataGridUser();
		newUser.setZone(user.getZone());
		newUser.setUsername(user.getUsername());
		newUser.setUserType(user.getUserType());
		logger.info("adding user:{}", newUser);

		try {
			logger.info("creating the user...");
			boolean creationSucessful = userService.createUser(newUser, user.getPassword());

			if (!creationSucessful) {
				logger.warn("unable to create the user");
				throw new DataGridException("User creation failed");
			}

			// adding read, write and ownership permissions to a set of collections
			userService.updateReadPermissions(newUser, addReadPermissionsOnDirs, removeReadPermissionsOnDirs);
			userService.updateWritePermissions(newUser, addWritePermissionsOnDirs, removeWritePermissionsOnDirs);
			userService.updateOwnership(newUser, addOwnerOnDirs, removeOwnerOnDirs);

			cleanPermissionsSets();

			DataGridUser userForGroups = userService.findByUsernameAndZone(newUser.getUsername(),
					newUser.getZone());

			userService.modifyUser(userForGroups);
			redirectAttributes.addFlashAttribute("userAddedSuccessfully", user.getUsername());

		} catch (DuplicateDataException e) {
			redirectAttributes.addFlashAttribute("duplicateUser",
					ExceptionEnum.USERS_DATA_DUPLICATE_EXCEPTION.getCode());
			logger.error("Could not create user: ", e);
		} catch (JargonException e) {
			redirectAttributes.addFlashAttribute("error", ExceptionEnum.JARGON_EXCEPTION.getCode());
			logger.error("Could not create user: ", e);
		}

		return "redirect:/users/";
	}

	/**
	 * Controller method that deletes an user
	 *
	 * @param username
	 * @param model
	 * @return the name of the template to render
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "delete/{username}/", method = RequestMethod.GET)
	public String deleteUser(@PathVariable String username, Model model, RedirectAttributes redirectAttributes)
			throws DataGridConnectionRefusedException {

		if (userService.deleteUserByUsername(username)) {
			redirectAttributes.addFlashAttribute("userRemovedSuccessfully", username);
		} else {
			redirectAttributes.addFlashAttribute("userNotRemovedSuccessfully", username);
		}

		return "redirect:/users/";
	}

	/**
	 * Controller that shows the modification of user view.
	 *
	 * @param username
	 * @param zone
	 * @param model
	 * @return the template name to render the modify user form
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "modify/{username}/{zone}/", method = RequestMethod.GET)
	public String showModifyUserForm(@PathVariable String username, @PathVariable String zone, Model model)
			throws DataGridException {

		DataGridUser user = userService.findByUsernameAndZone(username, zone);
		List<UserGroup> groups = groupService.findAll();
		List<DataGridZone> zones = zoneService.findAll();

		if (user != null) {
			UserForm userForm = new UserForm();
			// iRODS data
			userForm.setDataGridId(user.getDataGridId());
			userForm.setUsername(user.getUsername());
			userForm.setZone(user.getZone());
			userForm.setUserType(user.getUserType());


			// Getting the list of groups the user belongs to
			String[] groupList = userService.getGroupIdsForUser(user);
			groupsToBeAdded = new ArrayList<String>(Arrays.asList(groupList));

			model.addAttribute("user", userForm);
			model.addAttribute("groupList", groupList);
			model.addAttribute("requestMapping", "/users/modify/action/");

			model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
			model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
			model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);
		}

		model.addAttribute("groups", groups);
		model.addAttribute("zones", zones);
		model.addAttribute("userZone", zone);
		model.addAttribute("userTypes", userService.listUserTypes());

		return "users/userForm";
	}

	/**
	 * Updates the user on the current DB.
	 *
	 * @param userForm
	 * @return the users views template name
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "modify/action/", method = RequestMethod.POST)
	public String modifyUser(@ModelAttribute UserForm userForm, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws DataGridException {

		logger.info("modifyUser()");

		String[] groupList = groupsToBeAdded.toArray(new String[groupsToBeAdded.size()]);

		logger.info("groupList to be added:{}", groupList);

		List<UserGroup> groups = new ArrayList<UserGroup>();
		if (groupList != null && groupList.length != 0) {
			for (String groupId : groupList) {
				UserGroup userGroup = groupService.findById(groupId);
				if (groupId == null) {
					logger.warn("unable to find group for id:{}", groupId);
				} else {
					groups.add(userGroup);
				}
			}
		}

		logger.info("groupsToBeAdded:{}", groups);

		DataGridUser user = userService.findByUsernameAndZone(userForm.getUsername(),
				userForm.getZone());

		if (user == null) {
			logger.warn("user:{} not found:", userForm.getUsername());
			throw new DataGridException("user is not found");
		}

		if (user != null) {
			user.setZone(userForm.getZone());
			user.setUsername(userForm.getUsername());
			user.setPassword(userForm.getPassword());
			user.setUserType(userForm.getUserType());

			userService.modifyUser(user);
			userService.updateGroupList(user, groups);

			redirectAttributes.addFlashAttribute("userModifiedSuccessfully", userForm.getUsername());

			// Updating permissions on collections
			userService.updateOwnership(user, addOwnerOnDirs, removeOwnerOnDirs);
			userService.updateWritePermissions(user, addWritePermissionsOnDirs, removeWritePermissionsOnDirs);
			userService.updateReadPermissions(user, addReadPermissionsOnDirs, removeReadPermissionsOnDirs);

		}

		cleanPermissionsSets();
		return "redirect:/users/";
	}

	/**
	 * Finds all users existing in a group
	 *
	 * @param groupName
	 * @param zone
	 * @param model
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "getUsersInAGroup/{groupName}/{zone}", method = RequestMethod.GET)
	public String getGroupInfo(@PathVariable String groupName, @PathVariable String zone, Model model)
			throws DataGridException {

		logger.info("getGroupInfo()");

		if (groupName == null || groupName.isEmpty()) {
			logger.info("null or empty groupName");
		}

		if (zone == null || zone.isEmpty()) {
			throw new IllegalArgumentException("null or empty zone");
		}

		logger.info("groupName:{}", groupName);
		logger.info("zone:{}", zone);
		logger.info("model:{}", model);

		UserGroup userGroup = groupService.findByGroupnameAndZone(groupName, zone);

		if (userGroup == null) {
			model.addAttribute("users", new ArrayList<DataGridUser>());
			model.addAttribute("foundUsers", false);
			model.addAttribute("resultSize", 0);
			model.addAttribute("queryString", "");
		}

		else {
			List<DataGridUser> usersListOfAGroup = userService
					.findByDataGridIds(groupService.getMemberList(groupName, zone));

			model.addAttribute("users", usersListOfAGroup);
			model.addAttribute("foundUsers", usersListOfAGroup.size() > 0);
			model.addAttribute("resultSize", usersListOfAGroup.size());
			model.addAttribute("queryString", "findAll");
		}

		logger.info("returned model:{}", model);

		return "users/userListOfAGroup :: userList";
	}

	/**
	 * Finds all users existing in a group in the current zone
	 *
	 * @param groupName
	 * @param model
	 * @return
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "getUsersInAGroup/{groupName}", method = RequestMethod.GET)
	public String getGroupInfoHomeZone(@PathVariable String groupName, Model model) throws DataGridException {

		logger.info("getGroupInfo()");

		if (groupName == null || groupName.isEmpty()) {
			logger.info("null or empty groupName");
		}

		logger.info("groupName:{}", groupName);
		logger.info("model:{}", model);

		UserGroup userGroup = groupService.findByGroupnameAndZone(groupName, irodsServices.getCurrentUserZone());

		if (userGroup == null) {
			model.addAttribute("users", new ArrayList<DataGridUser>());
			model.addAttribute("foundUsers", false);
			model.addAttribute("resultSize", 0);
			model.addAttribute("queryString", "");
		}

		else {
			List<DataGridUser> usersListOfAGroup = userService
					.findByDataGridIds(groupService.getMemberList(groupName, irodsServices.getCurrentUserZone()));

			model.addAttribute("users", usersListOfAGroup);
			model.addAttribute("foundUsers", usersListOfAGroup.size() > 0);
			model.addAttribute("resultSize", usersListOfAGroup.size());
			model.addAttribute("queryString", "findAll");
		}

		logger.info("userGroup:{}", userGroup);

		return "users/userListOfAGroup :: userList";
	}

	/*
	 * *****************************************************************************
	 * *************** ******************************** PERMISSION
	 * ***************************************
	 * *****************************************************************************
	 * ***************
	 */

	@RequestMapping(value = "/addReadPermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addReadPermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive,
			Model model) {
		if (removeReadPermissionsOnDirs.containsKey(path)) {
			removeReadPermissionsOnDirs.remove(path);
			logger.info("read permission removed (add): " + path);
		} else {
			addReadPermissionsOnDirs.put(path, recursive);
			model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
			logger.info("read permission added (add):" + path);
		}
	}

	@RequestMapping(value = "/removeReadPermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeReadPermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addReadPermissionsOnDirs.containsKey(path)) {
			addReadPermissionsOnDirs.remove(path);
			logger.info("read permission removed (remove): " + path);
		} else {
			removeReadPermissionsOnDirs.put(path, recursive);
			logger.info("read permission added (remove): " + path);
		}
	}

	@RequestMapping(value = "/addWritePermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addWritePermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive,
			Model model) {
		if (removeWritePermissionsOnDirs.containsKey(path)) {
			removeWritePermissionsOnDirs.remove(path);
			logger.info("Write permission removed (add): " + path);
		} else {
			addWritePermissionsOnDirs.put(path, recursive);
			model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
			logger.info("Write permission added (add): " + path);
		}
	}

	@RequestMapping(value = "/removeWritePermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeWritePermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addWritePermissionsOnDirs.containsKey(path)) {
			addWritePermissionsOnDirs.remove(path);
			logger.info("Write permission removed (remove): " + path);
		} else {
			removeWritePermissionsOnDirs.put(path, recursive);
			logger.info("Write permission added (remove): " + path);
		}
	}

	@RequestMapping(value = "/addOwner/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addOwner(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive, Model model) {
		if (removeOwnerOnDirs.containsKey(path)) {
			removeOwnerOnDirs.remove(path);
			logger.info("Owner permission removed from add list: " + path);
		} else {
			addOwnerOnDirs.put(path, recursive);
			model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);
			logger.info("Owner permission added to add list: " + path);
		}
	}

	@RequestMapping(value = "/removeOwner/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeOwner(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addOwnerOnDirs.containsKey(path)) {
			addOwnerOnDirs.remove(path);
			logger.info("Owner permission removed from removal list: " + path);
		} else {
			removeOwnerOnDirs.put(path, recursive);
			logger.info("Owner permission added to removal list: " + path);
		}
	}

	@RequestMapping(value = "/usersToCSVFile/")
	public void usersToCSVFile(HttpServletResponse response) {
		String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		String filename = String.format("users_%s_%s.csv", loggedUser, date);

		// Setting CSV Mime type
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment;filename=" + filename);

		List<DataGridUser> users = userService.findAll();
		List<String> rows = new ArrayList<String>();
		rows.add(
				"Username;Zone;UserType\n");

		for (DataGridUser user : users) {
			rows.add(user.getUsername() + ";");
			rows.add(user.getZone() + ";");
			rows.add(user.getUserType() + ";");
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
		} catch (IOException e) {
			logger.error("Could not generate CSV file for users", e);
		}
	}

	private void cleanPermissionsSets() {
		addReadPermissionsOnDirs.clear();
		addWritePermissionsOnDirs.clear();
		addOwnerOnDirs.clear();
		removeReadPermissionsOnDirs.clear();
		removeWritePermissionsOnDirs.clear();
		removeOwnerOnDirs.clear();
	}

	/*
	 * *****************************************************************************
	 * *************** ******************************** VALIDATION
	 * ***************************************
	 * *****************************************************************************
	 * ***************
	 */

	/**
	 * Validates a username in iRODS
	 *
	 * @param username
	 * @return true, if the username can be used. False, otherwise.
	 */
	@ResponseBody
	@RequestMapping(value = "isValidUsername/{username}/", method = RequestMethod.GET)
	public String isValidUsername(@PathVariable String username) {

		if (username.compareTo("") != 0) {
			// if no users are found with this username, it means this username can be used
			List<DataGridUser> dataGridUsers = userService.findByUsername(username);
			return dataGridUsers.isEmpty() ? "true" : "false";
		}

		return "false";
	}
}
