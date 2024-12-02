/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.controller;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.irods.jargon.core.pub.UserGroupAO;
import org.irods.jargon.core.pub.domain.UserGroup;
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
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.modelattribute.group.GroupForm;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.GroupService;
import com.emc.metalnx.services.interfaces.HeaderService;
import com.emc.metalnx.services.interfaces.IRODSServices;
import com.emc.metalnx.services.interfaces.UserService;
import com.emc.metalnx.services.interfaces.ZoneService;

@Controller
@SessionAttributes({ "addReadPermissionsOnDirs", "addWritePermissionsOnDirs", "addOwnerOnDirs",
		"addInheritanceOnDirs" })
@RequestMapping(value = "/groups")
public class GroupController {

	@Autowired
	GroupService groupService;

	@Autowired
	UserService userService;

	@Autowired
	CollectionService collectionService;

	@Autowired
	ZoneService zoneService;

	@Autowired
	LoggedUserUtils loggedUserUtils;

	@Autowired
	HeaderService headerService;

	@Autowired
	IRODSServices irodsServices;

	private GroupForm groupForm;
	private UserGroup currentGroup;
	private List<String> usersToBeAdded = new ArrayList<String>();

	// Auxiliary structure to manage permissions changes - ADD
	private Map<String, Boolean> addReadPermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> addWritePermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> addOwnerOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> addInheritanceOnDirs = new HashMap<String, Boolean>();

	// Auxiliary structure to manage permissions changes - REMOVE
	private Map<String, Boolean> removeReadPermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> removeWritePermissionsOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> removeOwnerOnDirs = new HashMap<String, Boolean>();
	private Map<String, Boolean> removeInheritanceOnDirs = new HashMap<String, Boolean>();

	public final static Logger logger = LoggerFactory.getLogger(GroupController.class);

	/**
	 * It gets all groups existing in iRODS and our database and add this list of
	 * users as a parameter to the Model.
	 *
	 * @param model
	 * @return the user-management template
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String listGroups(Model model) throws DataGridException {

		List<UserGroup> groups = groupService.findAll();
		model.addAttribute("groups", groups);
		cleanModificationSets();
		return "groups/groupManagement";
	}

	/**
	 * Retrieves groups matching 'query' which is the group name search prefix and
	 * return JSON format
	 *
	 * @param model
	 * @return {@link String}
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/query/{query}/")
	@ResponseBody
	public String listGroupsByQueryAsString(Model model, @PathVariable String query) throws DataGridException {
		List<UserGroup> groups = groupService.findByGroupname(query);
		StringBuilder groupsResults = new StringBuilder();
		groupsResults.append("[");

		for (int i = 0; i < groups.size(); i++) {
			groupsResults.append(String.format("\"%s\"", groups.get(i).getUserGroupName()));
			if (i != groups.size() - 1) {
				groupsResults.append(", ");
			}
		}

		groupsResults.append("]");

		return groupsResults.toString();
	}

	/**
	 * List users by page number
	 *
	 * @param model
	 * @return
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/find/{query}/")
	public String listGroupsByQuery(Model model, @PathVariable String query) throws DataGridException {
		List<UserGroup> groups = groupService.findByGroupname(query);
		model.addAttribute("groups", groups);
		model.addAttribute("queryString", query);
		return "groups/groupList :: groupList";
	}

	/**
	 * List users by page number
	 *
	 * @param model
	 * @return
	 * @throws DataGridException
	 */
	@RequestMapping(value = "/findAll/")
	public String listAllGroups(Model model) throws DataGridException {
		List<UserGroup> groups = groupService.findAll();
		model.addAttribute("groups", groups);
		return "groups/groupList :: groupList";
	}

	/**
	 * Responds the request for the url "add/". It adds a new GroupForm to the Model
	 * and sets the form's action to "add".
	 *
	 * @param model
	 * @return the userForm template
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "add/", method = RequestMethod.GET)
	public String showAddGroupForm(Model model) throws DataGridConnectionRefusedException {
		groupForm = new GroupForm();
		model.addAttribute("groupForm", groupForm);
		model.addAttribute("requestMapping", "/groups/add/action/");

		// gets all users from iRODS to be attached to a group
		List<DataGridUser> users = userService.findAll();
		String[] membersList = new String[0];
		usersToBeAdded = new ArrayList<String>(Arrays.asList(membersList));

		model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
		model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
		model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);
		model.addAttribute("addInheritanceOnDirs", addInheritanceOnDirs);

		model.addAttribute("users", users);
		model.addAttribute("membersList", membersList);
		model.addAttribute("resultSize", users.size());
		model.addAttribute("foundUsers", users.size() > 0);
		model.addAttribute("zones", zoneService.findAll());
		model.addAttribute("groupZone", "");

		return "groups/groupForm";
	}

	/**
	 * Controller method that executes action 'create group'
	 *
	 * @param user
	 * @return the name of the template to render
	 * @throws DataGridException
	 */
	@RequestMapping(value = "add/action/", method = RequestMethod.POST)
	public String addGroup(@ModelAttribute GroupForm groupForm, HttpServletRequest httpServletRequest,
			RedirectAttributes redirectAttributes) throws DataGridException {

		logger.info("addGroup()");
		if (groupForm == null) {
			throw new IllegalArgumentException("null groupForm");
		}

		logger.info("groupForm:{}", groupForm);

		UserGroup newGroup = new UserGroup();
		newGroup.setUserGroupName(groupForm.getGroupname());
		newGroup.setZone(irodsServices.getCurrentUserZone());

		// Get the list of users to be attached to the group
		String[] idsList = usersToBeAdded.toArray(new String[usersToBeAdded.size()]);

		List<DataGridUser> usersToBeAttached = new ArrayList<DataGridUser>();
		if (idsList != null && idsList.length != 0) {
			usersToBeAttached = userService.findByDataGridIds(idsList);
		}

		if (groupService.createGroup(newGroup, usersToBeAttached) == false) {
			return "redirect:/groups/add/";
		}

		// Updating permissions on collections
		// boolean recursive = false;
		groupService.updateReadPermissions(newGroup, addReadPermissionsOnDirs, removeReadPermissionsOnDirs);
		groupService.updateWritePermissions(newGroup, addWritePermissionsOnDirs, removeWritePermissionsOnDirs);
		groupService.updateOwnership(newGroup, addOwnerOnDirs, removeOwnerOnDirs);

		redirectAttributes.addFlashAttribute("groupAddedSuccessfully", groupForm.getGroupname());
		collectionService.updateInheritanceOptions(addInheritanceOnDirs, removeInheritanceOnDirs, ""); // FIXME: what is
																										// add'l info?

		cleanModificationSets();

		currentGroup = null;
		return "redirect:/groups/";
	}

	/**
	 * Controller method that deletes a group
	 *
	 * @param groupName
	 * @param model
	 * @return the name of the template to render
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "delete/{groupName}/", method = RequestMethod.GET)
	public String deleteGroup(@PathVariable String groupName, Model model, RedirectAttributes redirectAttributes)
			throws DataGridConnectionRefusedException {

		logger.info("deleteGroup()");

		if (groupName == null || groupName.isEmpty()) {
			throw new IllegalArgumentException("null or empty groupName");
		}

		if (model == null) {
			throw new IllegalArgumentException("null model");
		}

		logger.info("groupName:{}", groupName);
		logger.info("model:{}", model);

		UserGroup userGroup = UserGroupAO.splitGroupIntoNameAndZone(groupName);

		try {
			groupService.deleteGroup(userGroup);
			redirectAttributes.addFlashAttribute("groupRemovedSuccessfully", groupName);
		} catch (DataGridException e) {
			logger.info("unable to delete group:{}", userGroup, e);
			redirectAttributes.addFlashAttribute("groupNotRemovedSuccessfully", groupName);
		}

		return "redirect:/groups/";
	}

	/**
	 * adds a user to the list that is supposed to contain users in a group
	 *
	 * @param userId
	 */
	@RequestMapping(value = "addUserToSaveList/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addUserToSaveList(@RequestParam("userId") String userId) {
		usersToBeAdded.add(userId);
	}

	/**
	 * removes a user from the list that is supposed to contain users in a group
	 *
	 * @param userId
	 */
	@RequestMapping(value = "removeUserToSaveList/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeUserToSaveList(@RequestParam("userId") String userId) {
		usersToBeAdded.remove(userId);
	}

	/**
	 * Controller that shows the modification of group view.
	 *
	 * @param groupname
	 * @param model
	 * @return
	 * @throws DataGridException
	 */
	@RequestMapping(value = "modify/{groupname}", method = RequestMethod.GET)
	public String showModifyGroupForm(@PathVariable String groupname, Model model) throws DataGridException {

		List<DataGridUser> users = userService.findAll();
		currentGroup = groupService.findByGroupnameAndZone(groupname, irodsServices.getCurrentUserZone());

		String[] membersList;
		if (currentGroup != null) {
			membersList = groupService.getMemberList(currentGroup.getUserGroupName(), currentGroup.getZone());
		} else {
			membersList = new String[0];
		}
		usersToBeAdded = new ArrayList<String>(Arrays.asList(membersList));

		GroupForm groupForm = new GroupForm();
		if (currentGroup != null) {
			groupForm.setGroupname(groupname);
		}

		model.addAttribute("groupForm", groupForm);
		model.addAttribute("requestMapping", "/groups/modify/action/");

		model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
		model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
		model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);
		model.addAttribute("addInheritanceOnDirs", addInheritanceOnDirs);

		model.addAttribute("users", users);
		model.addAttribute("membersList", membersList);
		model.addAttribute("resultSize", users.size());
		model.addAttribute("foundUsers", users.size() > 0);
		model.addAttribute("zones", zoneService.findAll());

		return "groups/groupForm";
	}

	/**
	 * Updates the group on the current DB.
	 *
	 * @param groupForm
	 * @return the groups views template name
	 * @throws DataGridConnectionRefusedException
	 */
	@RequestMapping(value = "modify/action/", method = RequestMethod.POST)
	public String modifyGroup(@ModelAttribute GroupForm groupForm, HttpServletRequest request,
			RedirectAttributes redirectAttributes) throws DataGridException {

		logger.info("modifyGroup()");

		if (groupForm == null) {
			throw new IllegalArgumentException("null groupForm");
		}

		logger.info("groupForm{}", groupForm);

		String[] userList = usersToBeAdded.toArray(new String[usersToBeAdded.size()]);
		List<DataGridUser> users = new ArrayList<DataGridUser>();
		if (userList != null && userList.length != 0) {
			users = userService.findByDataGridIds(userList);
		}

		logger.info("modifying users:{}", users);

		if (currentGroup != null) {
			groupService.updateMemberList(currentGroup, users);
		}

		// String redirectUrl = "redirect:/groups/modify/" +
		// currentGroup.getNameWithZone() + "/";

		// Updating permissions on collections
		groupService.updateReadPermissions(currentGroup, addReadPermissionsOnDirs, removeReadPermissionsOnDirs);
		groupService.updateWritePermissions(currentGroup, addWritePermissionsOnDirs, removeWritePermissionsOnDirs);
		groupService.updateOwnership(currentGroup, addOwnerOnDirs, removeOwnerOnDirs);
		collectionService.updateInheritanceOptions(addInheritanceOnDirs, removeInheritanceOnDirs, ""); // FIXME:
																										// add'l //
																										// info?
		cleanModificationSets();

		currentGroup = null;
		return "redirect:/groups/";
	}

	@RequestMapping(value = "/groupsToCSVFile/")
	public void groupsToCSVFile(HttpServletResponse response) throws DataGridException {
		String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

		String filename = String.format("groups_%s_%s.csv", loggedUser, date);

		// Setting CSV Mime type
		response.setContentType("text/csv");
		response.setHeader("Content-disposition", "attachment;filename=" + filename);

		List<UserGroup> groups = groupService.findAll();
		List<String> rows = new ArrayList<String>();
		rows.add("Group name;Zone\n");

		for (UserGroup group : groups) {
			rows.add(group.getUserGroupName() + ";"); // FIXME: add'l info?
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
			logger.error("Could not generate CSV file for groups", e);
		}
	}

	@RequestMapping(value = "/addReadPermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addReadPermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive,
			Model model) {
		if (removeReadPermissionsOnDirs.containsKey(path)) {
			removeReadPermissionsOnDirs.remove(path);
		} else {
			addReadPermissionsOnDirs.put(path, recursive);
			model.addAttribute("addReadPermissionsOnDirs", addReadPermissionsOnDirs);
		}
	}

	@RequestMapping(value = "/removeReadPermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeReadPermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addReadPermissionsOnDirs.containsKey(path)) {
			addReadPermissionsOnDirs.remove(path);
		} else {
			removeReadPermissionsOnDirs.put(path, recursive);
		}
	}

	@RequestMapping(value = "/addWritePermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addWritePermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive,
			Model model) {
		if (removeWritePermissionsOnDirs.containsKey(path)) {
			removeWritePermissionsOnDirs.remove(path);
		} else {
			addWritePermissionsOnDirs.put(path, recursive);
			model.addAttribute("addWritePermissionsOnDirs", addWritePermissionsOnDirs);
		}
	}

	@RequestMapping(value = "/removeWritePermission/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeWritePermission(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addWritePermissionsOnDirs.containsKey(path)) {
			addWritePermissionsOnDirs.remove(path);
		} else {
			removeWritePermissionsOnDirs.put(path, recursive);
		}
	}

	@RequestMapping(value = "/addOwner/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addOwner(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive, Model model) {
		if (removeOwnerOnDirs.containsKey(path)) {
			removeOwnerOnDirs.remove(path);
		} else {
			addOwnerOnDirs.put(path, recursive);
			model.addAttribute("addOwnerOnDirs", addOwnerOnDirs);
		}
	}

	@RequestMapping(value = "/removeOwner/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeOwner(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addOwnerOnDirs.containsKey(path)) {
			addOwnerOnDirs.remove(path);
		} else {
			removeOwnerOnDirs.put(path, recursive);
		}
	}

	@RequestMapping(value = "/addInheritance/")
	@ResponseStatus(value = HttpStatus.OK)
	public void addInheritance(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive,
			Model model) {
		if (removeInheritanceOnDirs.containsKey(path)) {
			removeInheritanceOnDirs.remove(path);
		} else {
			addInheritanceOnDirs.put(path, recursive);
			model.addAttribute("addInheritanceOnDirs", addInheritanceOnDirs);
		}
	}

	@RequestMapping(value = "/removeInheritance/")
	@ResponseStatus(value = HttpStatus.OK)
	public void removeInheritance(@RequestParam("path") String path, @RequestParam("recursive") Boolean recursive) {
		if (addInheritanceOnDirs.containsKey(path)) {
			addInheritanceOnDirs.remove(path);
		} else {
			removeInheritanceOnDirs.put(path, recursive);
		}
	}

	private void cleanModificationSets() {
		addReadPermissionsOnDirs.clear();
		addWritePermissionsOnDirs.clear();
		addOwnerOnDirs.clear();
		addInheritanceOnDirs.clear();
		removeReadPermissionsOnDirs.clear();
		removeWritePermissionsOnDirs.clear();
		removeOwnerOnDirs.clear();
		removeInheritanceOnDirs.clear();
	}

	/**
	 * Validates a groupname in iRODS
	 *
	 * @param model
	 * @param groupname
	 * @return true, if the groupname can be used. False, otherwise.
	 * @throws DataGridException
	 */
	@ResponseBody
	@RequestMapping(value = "isValidGroupname/{groupname}/", method = RequestMethod.GET)
	public String isValidGroupname(@PathVariable String groupname) throws DataGridException {

		if (groupname.compareTo("") != 0) {
			// if no users are found with this groupname, it means this groupname can be
			// used
			List<UserGroup> dataGridGroups = groupService.findByGroupname(groupname);
			List<DataGridUser> dataGridUsers = userService.findByUsername(groupname);
			return dataGridGroups.isEmpty() && dataGridUsers.isEmpty() ? "true" : "false";
		}

		return "false";
	}

}
