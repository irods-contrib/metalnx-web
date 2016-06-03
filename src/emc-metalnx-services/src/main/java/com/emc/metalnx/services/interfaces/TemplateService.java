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

package com.emc.metalnx.services.interfaces;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBException;

import com.emc.com.emc.metalnx.core.xml.MlxMetadataTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateAttrException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateUnitException;
import com.emc.metalnx.core.domain.exceptions.DataGridTemplateValueException;
import com.emc.metalnx.core.domain.exceptions.DataGridTooLongTemplateNameException;

public interface TemplateService {

    /**
     * Modifies an existing template
     *
     * @param template
     *            template object to persist in the database
     * @return True, if the template was successfully modified. False, otherwise.
     */
    public boolean modifyTemplate(DataGridTemplate template);

    /**
     * Finds a template by id
     *
     * @param id
     *            id of the template to be found
     * @return DataGridTemplate object if found. Null, otherwise.
     */
    public DataGridTemplate findById(long id);

    /**
     * Finds a template by a given name
     *
     * @param templateName
     *            name of the template to be found
     * @return DataGridTemplate object if found. Null, otherwise.
     */
    public DataGridTemplate findByName(String templateName);

    /**
     * Creates a template into the database
     *
     * @param dataGridTemplate
     *            template to be saved into the database
     * @return The id of the template just created
     */
    public long createTemplate(DataGridTemplate dataGridTemplate);

    /**
     * Lists all templates existing in the database.
     *
     * @return List of templates
     */
    public List<DataGridTemplate> findAll();

    /**
     * Deletes a template from the database based on its id
     *
     * @param id
     *            id of the template to be removed
     * @return True, if the template was successfully removed. False, otherwise.
     */
    public boolean deleteTemplate(long id);

    /**
     * Find templates by a query string
     *
     * @param queryString
     *            string containing the search term to match template names
     * @return list of templates
     */
    public List<DataGridTemplate> findByQueryString(String queryString);

    /**
     * Lists all fields of a template by its name
     *
     * @param template
     *            name of the template
     * @return List of template fields, if any.
     */
    public List<DataGridTemplateField> listTemplateFields(String template);

    /**
     * Lists all fields of a template by its id
     *
     * @param id
     *            name of the template
     * @return List of template fields, if any.
     */
    public List<DataGridTemplateField> listTemplateFields(Long id);

    /**
     * Lists all the system-wide metadata templates
     *
     * @return List of template fields, if any.
     */
    public List<DataGridTemplate> listPublicTemplates();

    /**
     * Returns all the privates templates owned by a given user
     *
     * @param user
     * @return List of template fields, if any.
     */
    public List<DataGridTemplate> listPrivateTemplatesByUser(String user);

    /**
     * Imports a XML file with multiple metadata template definition
     *
     * @param inStream
     * @return
     * @throws JAXBException
     * @throws DataGridTooLongTemplateNameException
     * @throws DataGridTemplateAttrException
     * @throws DataGridTemplateValueException
     * @throws DataGridTemplateUnitException
     */
    public boolean importXmlMetadataTemplate(InputStream inStream, String owner, String prefix, String suffix) throws JAXBException,
            DataGridTooLongTemplateNameException, DataGridTemplateAttrException, DataGridTemplateValueException, DataGridTemplateUnitException;

    /**
     * Maps a DataGridTemplate to a XML format
     *
     * @param template
     * @return
     */
    public MlxMetadataTemplate mapDataGridTemplateToXml(DataGridTemplate template);

    /**
     * Returns the number of all templates existing in the database.
     *
     * @return number of templates
     */
    int countAll();

}
