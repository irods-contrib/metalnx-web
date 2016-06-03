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

import java.util.List;

import org.irods.jargon.core.query.SpecificQueryResultSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.emc.metalnx.core.domain.entity.DataGridSpecificQuery;
import com.emc.metalnx.core.domain.exceptions.DataGridConnectionRefusedException;
import com.emc.metalnx.modelattribute.enums.URLMap;
import com.emc.metalnx.services.interfaces.SpecificQueryService;

@Controller
@RequestMapping(value = "/specificqueries")
public class SpecificQueryController {

	@Autowired
	private SpecificQueryService specificQueryService;
	
	@Value("${irods.zoneName}")
	private String irodsZoneName;
	
	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String index(Model model) {
		return "specificqueries/index";
	}
	
	@RequestMapping(value = "/findAll/", method = RequestMethod.GET)
	public String findAll(Model model) throws DataGridConnectionRefusedException {
		List<DataGridSpecificQuery> specificQueries = specificQueryService.findAll();
		model.addAttribute("specificQueries", specificQueries);
		model.addAttribute("resultSize", specificQueries.size());
		model.addAttribute("foundSpecificQueries", specificQueries.size() > 0);
		return "specificqueries/list :: specificQueriesList";
	}
	
	@RequestMapping(value = "/find/{query}/", method = RequestMethod.GET)
	public String find(Model model, @PathVariable String query) 
		throws DataGridConnectionRefusedException {
		
		List<DataGridSpecificQuery> specificQueries = specificQueryService.findByAliasLike("%" + query + "%");
		model.addAttribute("specificQueries", specificQueries);
		model.addAttribute("foundSpecificQueries", specificQueries.size() > 0);
		model.addAttribute("resultSize", specificQueries.size());
		model.addAttribute("queryString", query);
		
		return "specificqueries/list :: specificQueriesList";
	}
	
	@RequestMapping(value = "/add/", method = RequestMethod.GET)
	public String showAddForm(Model model) {
		model.addAttribute("specificQueryForm", new DataGridSpecificQuery());
		model.addAttribute("requestMapping", URLMap.URL_ADD_SPECIFIC_QUERY);
		return "specificqueries/form";
	}
	
	@RequestMapping(value = "/add/action/", method = RequestMethod.POST)
	public String addSpecificQuery(Model model, @ModelAttribute DataGridSpecificQuery specificQuery) 
		throws DataGridConnectionRefusedException {
		
		specificQueryService.createSpecificQuery(specificQuery);
		
		return "specificqueries/index";
	}
	
	@RequestMapping(value = "/modify/{specAlias}/", method = RequestMethod.GET)
	public String showModifyForm(Model model, @PathVariable String specAlias) 
		throws DataGridConnectionRefusedException {
		
		DataGridSpecificQuery specQuery = specificQueryService.findByAlias(specAlias);
		model.addAttribute("specificQueryForm", specQuery);
		model.addAttribute("requestMapping", URLMap.URL_MODIFY_SPECIFIC_QUERY);
		
		return "specificqueries/form";
	}
	
	@RequestMapping(value = "/modify/action/", method = RequestMethod.POST)
	public String modifySpecificQuery(Model model, 
		@ModelAttribute DataGridSpecificQuery specificQuery) 
		throws DataGridConnectionRefusedException {
		
		specificQueryService.updateSpecificQuery(specificQuery);
		
		return "specificqueries/index";
	}
	
	@RequestMapping(value = "/remove/{specAlias}/", method = RequestMethod.GET)
	public String removeSpecificQuery(Model model, @PathVariable String specAlias) 
		throws DataGridConnectionRefusedException {
		
		DataGridSpecificQuery specQuery = specificQueryService.findByAlias(specAlias);
		specificQueryService.removeSpecificQueryByAlias(specQuery);
		
		return "specificqueries/index";
	}
	
	@RequestMapping(value="/execute/{specAlias}/", method = RequestMethod.GET)
	public String executeQuery(@PathVariable String specAlias, Model model) 
		throws DataGridConnectionRefusedException {
		
		DataGridSpecificQuery specQuery = specificQueryService.findByAlias(specAlias);
		SpecificQueryResultSet results = specificQueryService.executeSpecificQuery(specQuery, irodsZoneName);
		
		int totalRecords = results.getTotalRecords();
		
		model.addAttribute("totalRecords", totalRecords);
		model.addAttribute("results", results);
		return "specificqueries/results";
	}
	
}
