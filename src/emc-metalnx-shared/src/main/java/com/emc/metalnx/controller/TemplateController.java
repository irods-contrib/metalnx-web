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
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
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
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.emc.com.emc.metalnx.core.xml.MlxMetadataTemplate;
import com.emc.com.emc.metalnx.core.xml.MlxMetadataTemplates;
import com.emc.metalnx.controller.utils.LoggedUserUtils;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.entity.DataGridUser;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;
import com.emc.metalnx.core.domain.exceptions.DataGridTooLongTemplateNameException;
import com.emc.metalnx.modelattribute.enums.MetadataTemplateAccessType;
import com.emc.metalnx.modelattribute.metadatatemplate.MetadataTemplateForm;
import com.emc.metalnx.modelattribute.template.field.TemplateFieldForm;
import com.emc.metalnx.services.interfaces.CollectionService;
import com.emc.metalnx.services.interfaces.TemplateFieldService;
import com.emc.metalnx.services.interfaces.TemplateService;
import com.emc.metalnx.services.interfaces.UserService;

@Controller
@Scope(WebApplicationContext.SCOPE_SESSION)
@SessionAttributes({ "selectedTemplates" })
@RequestMapping(value = "/templates")
public class TemplateController {

    @Autowired
    TemplateService templateService;

    @Autowired
    TemplateFieldService templateFieldService;

    @Autowired
    CollectionService collectionService;

    @Autowired
    UserService userService;

    @Autowired
    LoggedUserUtils loggedUserUtils;

    @Value("${irods.zoneName}")
    private String zoneName;

    // contains the template fields to be added to a brand new template
    private List<TemplateFieldForm> addTemplateFields;

    // contains the template fields to be removed from a brand new template
    private List<TemplateFieldForm> removeTemplateFields;

    // Auxiliary structure to handle template selection
    private Set<Long> selectedTemplates = new HashSet<Long>();

    // UI mode that will be shown when the rods user switches mode from admin to
    // user and vice-versa
    public static final String UI_USER_MODE = "user";
    public static final String UI_ADMIN_MODE = "admin";

    private static final Logger logger = LoggerFactory.getLogger(TemplateController.class);

    @RequestMapping(value = "/")
    public String index(Model model, HttpServletRequest request) throws DataGridConnectionRefusedException, DataGridException {

        addTemplateFields = new ArrayList<TemplateFieldForm>();
        removeTemplateFields = new ArrayList<TemplateFieldForm>();

        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        String uiMode = (String) request.getSession().getAttribute("uiMode");

        if (uiMode == null || uiMode.isEmpty()) {
            if (loggedUser.isAdmin()) {
                uiMode = UI_ADMIN_MODE;
            }
            else {
                uiMode = UI_USER_MODE;
                model.addAttribute("homePath", collectionService.getHomeDirectyForCurrentUser());
                model.addAttribute("publicPath", collectionService.getHomeDirectyForPublic());
            }
        }
        selectedTemplates.clear();
        model.addAttribute("uiMode", uiMode);
        return "template/templateManagement";
    }

    @RequestMapping(value = "/listTemplateFields", method = RequestMethod.POST)
    public String listTemplateFields(Model model, @RequestParam("template") String template) {
        updateAddTemplateFieldsList();

        List<DataGridTemplateField> dataGridTemplateFields = templateService.listTemplateFields(template);
        List<TemplateFieldForm> templateFields = this.mapDataGridTempToFieldForm(dataGridTemplateFields);
        DataGridTemplate metadataTemplate = templateService.findByName(template);

        if (addTemplateFields != null) {
            templateFields.addAll(addTemplateFields);
        }
        if (removeTemplateFields != null) {
            templateFields.removeAll(removeTemplateFields);
        }

        model.addAttribute("metadataTemplate", metadataTemplate);
        model.addAttribute("templateFields", templateFields);
        model.addAttribute("resultSize", templateFields.size());
        model.addAttribute("foundTemplateFields", templateFields.size() > 0);

        return "template/templateFieldList";
    }

    @RequestMapping(value = "/listTemplateFieldsForCollections", method = RequestMethod.POST)
    public String listTemplateFieldsForCollections(Model model, @RequestParam("templateIDsList") long[] templateIDsList) {
        MetadataTemplateForm templateForm = new MetadataTemplateForm();
        List<TemplateFieldForm> templateFields = new ArrayList<TemplateFieldForm>();
        List<DataGridTemplateField> dataGridTemplateFields = new ArrayList<DataGridTemplateField>();

        if (templateIDsList.length > 0) {
            for (long id : templateIDsList) {
                dataGridTemplateFields.addAll(templateService.listTemplateFields(id));
            }
            templateFields = this.mapDataGridTempToFieldForm(dataGridTemplateFields);
        }

        model.addAttribute("templateForm", templateForm);
        model.addAttribute("requestMapping", "/collections/applyTemplatesToCollections/");
        model.addAttribute("templateFields", templateFields);
        model.addAttribute("resultSize", templateFields.size());
        model.addAttribute("foundTemplateFields", templateFields.size() > 0);

        return "collections/templateFieldListForCollections";
    }

    @RequestMapping(value = "/listTemplatesForCollections", method = RequestMethod.POST)
    public String listTemplatesForCollections(Model model) {
        findAllTemplates(model);
        return "collections/templateListForCollections :: templateList";
    }

    @RequestMapping(value = "/findAll/")
    public String findAll(Model model) {
        findAllTemplates(model);
        return "template/templateList :: templateList";
    }

    @RequestMapping(value = "/find/{templateName}")
    public String findTemplate(Model model, @PathVariable String templateName) {
        List<DataGridTemplate> templates = templateService.findByQueryString(templateName);

        model.addAttribute("templates", templates);
        model.addAttribute("foundTemplates", templates.size() >= 0);
        model.addAttribute("resultSize", templates.size());
        model.addAttribute("queryString", templateName);

        return "template/templateList :: templateList";
    }

    @RequestMapping(value = "add/")
    public String newTemplate(Model model, HttpServletRequest request) {
        MetadataTemplateForm templateForm = new MetadataTemplateForm();
        TemplateFieldForm templateFieldForm = new TemplateFieldForm();

        if (addTemplateFields == null) {
            addTemplateFields = new ArrayList<TemplateFieldForm>();
        }

        templateForm.setOwner(loggedUserUtils.getLoggedDataGridUser().getUsername());

        model.addAttribute("uiMode", request.getSession().getAttribute("uiMode"));
        model.addAttribute("accessTypes", MetadataTemplateAccessType.values());
        model.addAttribute("metadataTemplateForm", templateForm);
        model.addAttribute("templateFieldForm", templateFieldForm);
        model.addAttribute("requestMapping", "/templates/add/action/");
        model.addAttribute("requestMappingForTemplateField", "/templates/addFieldToCurrentTemplate");

        return "template/templateForm";
    }

    @RequestMapping(value = "add/action/")
    public String addNewTemplate(Model model, @ModelAttribute MetadataTemplateForm templateForm, RedirectAttributes redirectAttributes) {
        DataGridTemplate newTemplate = null;

        try {
            newTemplate = new DataGridTemplate();
            newTemplate.setTemplateName(templateForm.getTemplateName());
            newTemplate.setDescription(templateForm.getDescription());
            newTemplate.setUsageInformation(templateForm.getUsageInformation());
            newTemplate.setOwner(loggedUserUtils.getLoggedDataGridUser().getUsername());
            newTemplate.setAccessType(templateForm.getAccessType().toString());

            long templateID = templateService.createTemplate(newTemplate);

            if (templateID > 0) {
                redirectAttributes.addFlashAttribute("templateAddedSuccessfully", newTemplate.getTemplateName());
                newTemplate.setId(templateID);

                // adding all fields to the template
                for (TemplateFieldForm tempFieldForm : addTemplateFields) {
                    DataGridTemplateField dataGridTempField = mapTempFieldFormToDataGridTemp(tempFieldForm);
                    dataGridTempField.setTemplate(newTemplate);
                    templateFieldService.createTemplateField(dataGridTempField);
                }

                // reseting the temporary fields to be added and removed from a
                // template
                addTemplateFields = new ArrayList<TemplateFieldForm>();
                removeTemplateFields = new ArrayList<TemplateFieldForm>();

                return "redirect:/templates/";
            }
        }
        catch (DataGridTooLongTemplateNameException e) {
            redirectAttributes.addFlashAttribute("templateNotAddedSuccessfully", true);
            redirectAttributes.addFlashAttribute("tooLongTemplateName", true);
        }
        catch (Exception e) {
            redirectAttributes.addFlashAttribute("templateNotAddedSuccessfully", true);
        }

        return "redirect:/templates/add/";
    }

    @RequestMapping(value = "modify/")
    public String showModifyTemplate(Model model, HttpServletRequest request) {
        // DataGridTemplate template = templateService.findById(selectedTemplates.iterator().next());
        DataGridTemplate template = templateService.findById(Long.parseLong(request.getParameter("templateId")));

        if (template == null) {
            return "redirect:/templates/";
        }

        MetadataTemplateForm templateForm = new MetadataTemplateForm();
        templateForm.setId(template.getId());
        templateForm.setTemplateName(template.getTemplateName());
        templateForm.setDescription(template.getDescription());
        templateForm.setUsageInformation(template.getUsageInformation());
        templateForm.setOwner(template.getOwner());
        templateForm.setAccessType(template.getAccessType());
        templateForm.setVersion(template.getVersion());

        TemplateFieldForm templateFieldForm = new TemplateFieldForm();

        model.addAttribute("uiMode", request.getSession().getAttribute("uiMode"));
        model.addAttribute("accessTypes", MetadataTemplateAccessType.values());
        model.addAttribute("metadataTemplateForm", templateForm);
        model.addAttribute("templateFieldForm", templateFieldForm);
        model.addAttribute("requestMapping", "/templates/modify/action/");

        return "template/templateForm";
    }

    @RequestMapping(value = "modify/action/")
    public String modifyTemplate(Model model, @ModelAttribute MetadataTemplateForm templateForm, RedirectAttributes redirectAttributes) {

        DataGridTemplate template = null;

        try {
            template = templateService.findById(templateForm.getId());

            if (template == null) {
                throw new Exception("Cannot modify a non-existent template");
            }
            DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
            if (!template.getOwner().equalsIgnoreCase(loggedUser.getUsername())) {
                throw new Exception("Cannot modify a template beloging to another user");
            }

            template.setTemplateName(templateForm.getTemplateName());
            template.setDescription(templateForm.getDescription());
            template.setUsageInformation(templateForm.getUsageInformation());
            template.setAccessType(templateForm.getAccessType().toString());

            List<String> positions = templateForm.getAvuPositions();
            List<String> attributes = templateForm.getAvuAttributes();
            List<String> values = templateForm.getAvuValues();
            List<String> units = templateForm.getAvuUnits();

            if (attributes != null) {
                for (int i = 0; i < attributes.size(); i++) {
                    for (int j = i + 1; j < attributes.size(); j++) {
                        if (attributes.get(i).equals(attributes.get(j)) && !values.get(i).isEmpty() && values.get(i).equals(values.get(j))
                                && units.get(i).equals(units.get(j))) {
                            redirectAttributes.addFlashAttribute("repeatedAVU", true);
                            return "redirect:/templates/modify/";
                        }
                    }
                }
            }

            if (positions != null) {
                for (int i = 0; i < positions.size(); i++) {
                    String position = positions.get(i);
                    if (position.contains("mod_pos_")) {
                        addTemplateFields.get(Integer.parseInt(position.replace("mod_pos_", ""))).setAttribute(attributes.get(i));
                        addTemplateFields.get(Integer.parseInt(position.replace("mod_pos_", ""))).setValue(values.get(i));
                        addTemplateFields.get(Integer.parseInt(position.replace("mod_pos_", ""))).setUnit(units.get(i));
                    }
                    else if (position.contains("mod_id_")) {
                        templateFieldService.modifyTemplateField(Long.parseLong(position.replace("mod_id_", "")), attributes.get(i), values.get(i),
                                units.isEmpty() ? "" : units.get(i));
                        template.setModified(true);
                    }
                }
            }

            // adding all fields to the template
            for (TemplateFieldForm tempFieldForm : addTemplateFields) {
                DataGridTemplateField dataGridTempField = mapTempFieldFormToDataGridTemp(tempFieldForm);
                dataGridTempField.setTemplate(template);
                templateFieldService.createTemplateField(dataGridTempField);
                template.setModified(true);
            }

            Set<DataGridTemplateField> currentTempFields = template.getFields();

            // removing fields from the template
            for (TemplateFieldForm tempFieldForm : removeTemplateFields) {
                DataGridTemplateField dataGridTempField = mapTempFieldFormToDataGridTemp(tempFieldForm);
                dataGridTempField.setTemplate(template);
                template.setModified(true);

                // removing template fields from template
                if (currentTempFields.remove(dataGridTempField)) {
                    templateFieldService.deleteTemplateField(dataGridTempField);
                    logger.debug("Template removed from memory and database");
                }
                // template field wasn't removed
                else {
                    throw new Exception("Could not removed template field from memory.");
                }
            }

            template.setFields(currentTempFields);
            templateService.modifyTemplate(template);

            // reseting the temporary fields to be added and removed from a template
            addTemplateFields = new ArrayList<TemplateFieldForm>();
            removeTemplateFields = new ArrayList<TemplateFieldForm>();

            redirectAttributes.addFlashAttribute("templateModifiedSuccessfully", template.getTemplateName());

            selectedTemplates.clear();
        }
        catch (Exception e) {
            logger.error("Could not modify template {}.", templateForm.getTemplateName());
            redirectAttributes.addFlashAttribute("templateNotModifiedSuccessfully", templateForm.getTemplateName());
        }

        return "redirect:/templates/";

    }

    @RequestMapping(value = "delete/")
    public String deleteTemplate(RedirectAttributes redirectAttributes) {

        boolean deletionSuccessful = true;

        for (Long templateId : selectedTemplates) {
            logger.debug("Deleting template [{}]", templateId);
            DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
            if (templateService.findById(templateId).getOwner().equalsIgnoreCase(loggedUser.getUsername())) {
                deletionSuccessful &= templateService.deleteTemplate(templateId);
            }
        }

        redirectAttributes.addFlashAttribute("templateRemovedSuccessfully", deletionSuccessful);
        selectedTemplates.clear();

        return "redirect:/templates/";
    }

    @RequestMapping(value = "/addFieldToCurrentTemplate")
    public String addFieldToCurrentTemplate(Model model, @ModelAttribute TemplateFieldForm templateFieldForm) {
        updateAddTemplateFieldsList();

        boolean isAddFieldsEmpty = false;

        try {
            List<DataGridTemplateField> existingTemplateFields = templateService.listTemplateFields(templateFieldForm.getTemplateName());

            DataGridTemplateField newDataGridField = mapTempFieldFormToDataGridTemp(templateFieldForm);

            List<TemplateFieldForm> templateFields = new ArrayList<TemplateFieldForm>();
            TemplateFieldForm newField = this.mapDataGridTempToFieldForm(newDataGridField, addTemplateFields.size());
            templateFields.add(newField);

            if (addTemplateFields.isEmpty()) {
                isAddFieldsEmpty = true;
            }

            // prevent an AVU from being added twice to a template
            if (!addTemplateFields.contains(newField) || addTemplateFields.contains(newField) && newField.getValue().equals("")) {
                addTemplateFields.add(newField.getFormListPosition(), newField);
            }

            model.addAttribute("templateFields", templateFields);
            model.addAttribute("resultSize", templateFields.size());
            model.addAttribute("foundTemplateFields", templateFields.size() > 0);

            if (isAddFieldsEmpty && existingTemplateFields.isEmpty()) {
                return "template/templateFieldList";
            }
        }
        catch (DataGridTemplateAttrException e) {
            logger.error(e.getMessage());
        }
        catch (DataGridTemplateValueException e) {
            logger.error(e.getMessage());
        }
        catch (DataGridTemplateUnitException e) {
            logger.error(e.getMessage());
        }

        return "template/templateFieldList :: avuRow";
    }

    @RequestMapping(value = "/removeFieldFromDB")
    @ResponseStatus(value = HttpStatus.OK)
    public void removeFieldFromDB(@RequestParam("templateFieldsIDList") long[] templateFieldsIDList) {
        for (long templateFieldID : templateFieldsIDList) {
            DataGridTemplateField dataGridField = templateFieldService.findById(templateFieldID);
            TemplateFieldForm field = this.mapDataGridTempToFieldForm(dataGridField);

            if (addTemplateFields.contains(field)) {
                addTemplateFields.remove(field);
            }

            // adding the field to the list that is needed to be removed from a template
            if (!removeTemplateFields.contains(field)) {
                removeTemplateFields.add(field);
            }
        }
    }

    @RequestMapping(value = "/removeFieldFromTemplate")
    @ResponseStatus(value = HttpStatus.OK)
    public void removeFieldFromTemplate(@RequestParam("templateFieldsPosList") int[] templateFieldsPosList) {
        int addTemplateFieldsSize = addTemplateFields.size();

        for (int templateFieldPos : templateFieldsPosList) {
            if (templateFieldPos >= 0 && templateFieldPos < addTemplateFieldsSize) {
                addTemplateFields.remove(templateFieldPos);
            }
        }
    }

    @RequestMapping(value = "/exportTemplatesToXMLFile/")
    public void exportTemplateListToXMLFile(HttpServletResponse response) {
        try {
            setReponseHeaderForXmlExport(response);

            // Creating marshaller mechanism
            JAXBContext jaxbContext = JAXBContext.newInstance(MlxMetadataTemplates.class);
            Marshaller m = jaxbContext.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

            // Creating XML data structure for exporting
            MlxMetadataTemplates ts = new MlxMetadataTemplates();

            for (Long templateId : selectedTemplates) {

                // Getting template data
                DataGridTemplate template = templateService.findById(Long.valueOf(templateId));

                // Mapping DB entity to XML entity
                MlxMetadataTemplate t = templateService.mapDataGridTemplateToXml(template);
                ts.getTemplates().add(t);
            }

            // Marshalling and flushing stream
            m.marshal(ts, response.getOutputStream());
            response.getOutputStream().flush();

            selectedTemplates.clear();

        }
        catch (JAXBException | IOException e) {
            logger.error("Could not export templates using metadata", e);
        }
    }

    @RequestMapping(value = "/import/", method = RequestMethod.POST)
    @ResponseStatus(value = HttpStatus.OK)
    @ResponseBody
    public String importXMLFile(Model model, HttpServletRequest request, RedirectAttributes redirect) {

        String responseString = "ok";

        if (request instanceof MultipartHttpServletRequest) {

            MultipartHttpServletRequest multipartRequest = (MultipartHttpServletRequest) request;
            List<MultipartFile> multipartFiles = multipartRequest.getFiles("file");
            String prefix = multipartRequest.getParameter("prefix");
            String suffix = multipartRequest.getParameter("suffix");

            try {
                String username = loggedUserUtils.getLoggedDataGridUser().getUsername();
                boolean result = templateService.importXmlMetadataTemplate(multipartFiles.get(0).getInputStream(), username, prefix, suffix);

                if (!result) {
                    responseString = "partial";
                }

            }
            catch (JAXBException | IOException | DataGridException e) {
                logger.error("Could not import metadata templates", e);
                responseString = "error";
            }

        }
        return responseString;
    }

    /*
     * ************************************************************************
     * ******************** HANDLING SESSION VARIABLES ************************
     * ************************************************************************
     */

    @RequestMapping(value = "/selectTemplate/", method = RequestMethod.POST)
    @ResponseBody
    public boolean selectTemplate(@RequestParam("id") String id) {
        Long idLong = Long.valueOf(id);
        if (!selectedTemplates.contains(idLong)) {
            selectedTemplates.add(idLong);
        }

        return canCurrentUserRemoveSelectedTemplates();
    }

    @RequestMapping(value = "/unselectTemplate/", method = RequestMethod.POST)
    @ResponseBody
    public boolean unselectTemplate(@RequestParam("id") String id) {
        Long idLong = Long.valueOf(id);
        if (selectedTemplates.contains(idLong)) {
            selectedTemplates.remove(idLong);
        }

        return canCurrentUserRemoveSelectedTemplates();
    }

    /*
     * ********************************************************************************************
     * ******************************** VALIDATION ***************************************
     * ********************************************************************************************
     */

    /**
     * Validates a template name
     *
     * @param templateName
     * @return True, if the template name can be used. False, otherwise.
     */
    @ResponseBody
    @RequestMapping(value = "isValidTemplateName/{templateName}/", method = RequestMethod.GET)
    public String isValidUsername(@PathVariable String templateName) {

        if (templateName.compareTo("") != 0) {
            // if no users are found with this username, it means this username can be used
            DataGridTemplate template = templateService.findByName(templateName);
            return template == null ? "true" : "false";
        }

        return "false";
    }

    /*
     * *************************************************************************
     * ***************************** PRIVATE METHODS ***************************
     * *************************************************************************
     */

    private void setReponseHeaderForXmlExport(HttpServletResponse response) {
        String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();
        String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        String filename = String.format("template_%s_%s.xml", loggedUser, date);

        // Setting CSV Mime type
        response.setContentType("text/xml");
        response.setHeader("Content-disposition", "attachment;filename=" + filename);
    }

    private void findAllTemplates(Model model) {

        List<DataGridTemplate> templates = new ArrayList<DataGridTemplate>();
        String loggedUser = loggedUserUtils.getLoggedDataGridUser().getUsername();

        templates = templateService.listPublicTemplates();
        templates.addAll(templateService.listPrivateTemplatesByUser(loggedUser));

        Collections.sort(templates);

        model.addAttribute("templates", templates);
        model.addAttribute("resultSize", templates.size());
        model.addAttribute("foundTemplates", templates.size() >= 0);
    }

    /**
     * Updates the form list position of each element of the addTemplateFields array list.
     */
    private void updateAddTemplateFieldsList() {
        if (addTemplateFields != null) {
            // updating form list position once an element was removed from the
            // array
            int i = 0;
            for (TemplateFieldForm templateFieldForm : addTemplateFields) {
                templateFieldForm.setFormListPosition(i);
                i++;
            }
        }
    }

    /**
     * Maps a single instance of DataGridTemplateField into a TemplateFieldForm object
     *
     * @param dataGridTempField
     *            DataGridTemplateField object
     * @return TemplateFieldForm object
     */
    private TemplateFieldForm mapDataGridTempToFieldForm(DataGridTemplateField dataGridTempField) {
        if (dataGridTempField == null) {
            return null;
        }

        TemplateFieldForm tempField = new TemplateFieldForm();
        tempField.setAttribute(dataGridTempField.getAttribute());
        tempField.setValue(dataGridTempField.getValue());
        tempField.setUnit(dataGridTempField.getUnit());
        tempField.setStartRange(dataGridTempField.getStartRange());
        tempField.setEndRange(dataGridTempField.getEndRange());
        tempField.setOrder(dataGridTempField.getOrder());
        tempField.setId(dataGridTempField.getId());

        if (dataGridTempField.getTemplate() != null) {
            tempField.setTemplateName(dataGridTempField.getTemplate().getTemplateName());
        }

        return tempField;
    }

    /**
     * Maps a single instance of DataGridTemplateField into a TemplateFieldForm object
     *
     * @param dataGridTempField
     *            DataGridTemplateField object
     * @param position
     *            DataGridTemplateField object position into the memory array list of fields to be
     *            added to a template
     * @return TemplateFieldForm object
     */
    private TemplateFieldForm mapDataGridTempToFieldForm(DataGridTemplateField dataGridTempField, int position) {
        if (dataGridTempField == null) {
            return null;
        }

        TemplateFieldForm tempField = new TemplateFieldForm();
        tempField.setAttribute(dataGridTempField.getAttribute());
        tempField.setValue(dataGridTempField.getValue());
        tempField.setUnit(dataGridTempField.getUnit());
        tempField.setStartRange(dataGridTempField.getStartRange());
        tempField.setEndRange(dataGridTempField.getEndRange());
        tempField.setOrder(dataGridTempField.getOrder());
        tempField.setFormListPosition(position);
        tempField.setId(dataGridTempField.getId());

        if (dataGridTempField.getTemplate() != null) {
            tempField.setTemplateName(dataGridTempField.getTemplate().getTemplateName());
        }

        return tempField;
    }

    /**
     * Maps a list of DataGridTemplateField objects into a list of TemplateFieldForm objects
     *
     * @param tempFields
     *            list of data grid template objects
     * @return list of TemplateFieldForm objects
     */
    private List<TemplateFieldForm> mapDataGridTempToFieldForm(List<DataGridTemplateField> tempFields) {
        List<TemplateFieldForm> tempFieldFormList = new ArrayList<TemplateFieldForm>();

        if (tempFields == null || tempFields.isEmpty()) {
            return tempFieldFormList;
        }

        TemplateFieldForm tempField = null;

        int position = 0;
        for (DataGridTemplateField dataGridTemplateField : tempFields) {
            tempField = this.mapDataGridTempToFieldForm(dataGridTemplateField, position);
            tempFieldFormList.add(tempField);
        }

        return tempFieldFormList;
    }

    /**
     * Map a Template Field form object into a DataGridTemplateField object
     *
     * @param tempFieldForm
     *            template field form object to be mapped
     * @return DataGridTemplateField object
     * @throws DataGridTemplateAttrException
     * @throws DataGridTemplateValueException
     * @throws DataGridTemplateUnitException
     */
    private DataGridTemplateField mapTempFieldFormToDataGridTemp(TemplateFieldForm tempFieldForm) throws DataGridTemplateAttrException,
            DataGridTemplateValueException, DataGridTemplateUnitException {

        if (tempFieldForm == null) {
            return null;
        }

        DataGridTemplateField templateField = new DataGridTemplateField();
        templateField.setAttribute(tempFieldForm.getAttribute());
        templateField.setValue(tempFieldForm.getValue());
        templateField.setUnit(tempFieldForm.getUnit());
        templateField.setStartRange(tempFieldForm.getStartRange());
        templateField.setEndRange(tempFieldForm.getEndRange());
        templateField.setOrder(tempFieldForm.getOrder());

        if (tempFieldForm.getId() != null) {
            templateField.setId(tempFieldForm.getId());
        }

        return templateField;
    }

    /**
     * Checks if the currently logged user can delete the list of selected templates
     *
     * @return true it is possible to delete selected templates, false otherwise
     */
    private boolean canCurrentUserRemoveSelectedTemplates() {
        DataGridUser loggedUser = loggedUserUtils.getLoggedDataGridUser();
        for (Long templateId : selectedTemplates) {
            if (!templateService.findById(templateId).getOwner().equalsIgnoreCase(loggedUser.getUsername())) {
                return false;
            }
        }
        return true;
    }
}
