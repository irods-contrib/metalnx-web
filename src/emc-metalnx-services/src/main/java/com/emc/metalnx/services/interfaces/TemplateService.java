/* Copyright (c) 2018, University of North Carolina at Chapel Hill */
/* Copyright (c) 2015-2017, Dell EMC */

package com.emc.metalnx.services.interfaces;

import java.io.InputStream;
import java.util.List;

import org.irods.jargon.core.exception.DataNotFoundException;

import com.emc.metalnx.core.domain.entity.DataGridTemplate;
import com.emc.metalnx.core.domain.entity.DataGridTemplateField;
import com.emc.metalnx.core.domain.exceptions.DataGridException;
import com.emc.metalnx.services.irods.MetadataTemplateException;

public interface TemplateService {

	/**
	 * Modifies an existing template
	 *
	 * @param template template object to persist in the database
	 * @return True, if the template was successfully modified. False, otherwise.
	 */
	public boolean modifyTemplate(DataGridTemplate template);

	/**
	 * Finds a template by id
	 *
	 * @param id id of the template to be found
	 * @return DataGridTemplate object if found. Null, otherwise.
	 */
	public DataGridTemplate findById(long id);

	/**
	 * Finds a template by a given name
	 *
	 * @param templateName name of the template to be found
	 * @return DataGridTemplate object if found. Null, otherwise.
	 */
	public DataGridTemplate findByName(String templateName);

	/**
	 * Creates a template into the database
	 *
	 * @param dataGridTemplate template to be saved into the database
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
	 * @param id id of the template to be removed
	 * @return True, if the template was successfully removed. False, otherwise.
	 */
	public boolean deleteTemplate(long id);

	/**
	 * Find templates by a query string
	 *
	 * @param queryString string containing the search term to match template names
	 * @return list of templates
	 */
	public List<DataGridTemplate> findByQueryString(String queryString);

	/**
	 * Lists all fields of a template by its name
	 *
	 * @param template name of the template
	 * @return List of template fields, if any.
	 */
	public List<DataGridTemplateField> listTemplateFields(String template);

	/**
	 * Lists all fields of a template by its id
	 *
	 * @param id name of the template
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
	 * Returns the number of all templates existing in the database.
	 *
	 * @return number of templates
	 */
	int countAll();

	/**
	 * Upload a JSON metadata template into the Metalnx environment. Warning: this
	 * functionality is transitional as the metadata template working group works to
	 * define a final templating spec
	 * 
	 * @param inStream {@link InputStream} that contains a JSON template document
	 * @param owner    {@code String} with the owner of the template
	 * @param prefix   {@code String} with template name prefix
	 * @param suffix   {@code String} with the template name suffix
	 * @return {@code boolean} indicating success
	 * @throws MetadataTemplateException {@link MetadataTemplateException}
	 * @throws DataGridException         {@link DataGridException}
	 */
	boolean importMetadataTemplate(InputStream inStream, String owner, String prefix, String suffix)
			throws MetadataTemplateException, DataGridException;

	/**
	 * Given an id of a metadata template in the irods-ext data store, retrieve the
	 * JSON string that is the serialized format of the template
	 * 
	 * @param id {@code long} with a valid id of a metadata template
	 * @return {@code String} with the serialized JSON string representing the
	 *         template
	 * @throws DataNotFoundException     {@link DataNotFoundException}
	 * @throws MetadataTemplateException {@link MetadataTemplateException}
	 * @throws DataGridException         {@link DataGridException}
	 */
	String exportMetadataTemplateAsJsonString(long id)
			throws DataNotFoundException, MetadataTemplateException, DataGridException;

}
