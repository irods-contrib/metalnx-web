/*
 * Copyright (c) 2015-2017, Dell EMC
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.emc.metalnx.services.interfaces;

import org.irods.jargon.core.exception.JargonException;
import org.springframework.web.multipart.MultipartFile;

import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.core.domain.exceptions.DataGridException;

public interface RuleDeploymentService {

	/**
	 * Deploys a rule into the grid
	 * 
	 * @param file
	 *            rule file to deploy
	 * @throws JargonException
	 */
	void deployRule(MultipartFile file) throws DataGridException, JargonException;

	/**
	 * Finds the rule cache directory in the grid
	 * 
	 * @return String representing the rule cache directory in the grid
	 */
	String getRuleCachePath();

	/**
	 * Creates the rule cache directory in the grid (/<zone>/.rulecache)
	 * 
	 * @throws DataGridException
	 *             if the creation of the cache directory fails
	 */
	void createRuleCache() throws DataGridException;

	/**
	 * Checks whether or not the rule cache directory already exists in the grid.
	 * 
	 * @return True, if /<zone>/.rulecache path exists. False, otherwise.
	 * @throws JargonException
	 * @throws DataGridConnectionRefusedException
	 */
	boolean ruleCacheExists() throws DataGridConnectionRefusedException, JargonException;
}
