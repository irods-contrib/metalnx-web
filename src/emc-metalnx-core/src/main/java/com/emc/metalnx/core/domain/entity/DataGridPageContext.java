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
package com.emc.metalnx.core.domain.entity;

public class DataGridPageContext {

	private int startItemNumber;
	private int endItemNumber;
	private int totalNumberOfItems;
	
	/**
	 * @return the startItemNumber
	 */
	public int getStartItemNumber() {
		return startItemNumber;
	}
	/**
	 * @param startItemNumber the startItemNumber to set
	 */
	public void setStartItemNumber(int startItemNumber) {
		this.startItemNumber = startItemNumber;
	}
	/**
	 * @return the endItemNumber
	 */
	public int getEndItemNumber() {
		return endItemNumber;
	}
	/**
	 * @param endItemNumber the endItemNumber to set
	 */
	public void setEndItemNumber(int endItemNumber) {
		this.endItemNumber = endItemNumber;
	}
	/**
	 * @return the totalNumberOfItems
	 */
	public int getTotalNumberOfItems() {
		return totalNumberOfItems;
	}
	/**
	 * @param totalNumberOfItems the totalNumberOfItems to set
	 */
	public void setTotalNumberOfItems(int totalNumberOfItems) {
		this.totalNumberOfItems = totalNumberOfItems;
	}
	
}
