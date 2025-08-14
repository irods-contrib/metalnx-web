 /* Copyright (c) 2018, University of North Carolina at Chapel Hill */
 /* Copyright (c) 2015-2017, Dell EMC */
 

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
	 * @param startItemNumber
	 *            the startItemNumber to set
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
	 * @param endItemNumber
	 *            the endItemNumber to set
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
	 * @param totalNumberOfItems
	 *            the totalNumberOfItems to set
	 */
	public void setTotalNumberOfItems(int totalNumberOfItems) {
		this.totalNumberOfItems = totalNumberOfItems;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataGridPageContext [startItemNumber=").append(startItemNumber).append(", endItemNumber=")
				.append(endItemNumber).append(", totalNumberOfItems=").append(totalNumberOfItems).append("]");
		return builder.toString();
	}

}
