package com.emc.metalnx.core.domain.entity;

public class HeaderObject {

	private String name;
	private String icon;
	private String hint;
	
	
	public HeaderObject(String name, String icon, String hint) {
		super();
		this.name = name;
		this.icon = icon;
		this.hint = hint;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getIcon() {
		return icon;
	}
	public void setIcon(String icon) {
		this.icon = icon;
	}
	public String getHint() {
		return hint;
	}
	public void setHint(String hint) {
		this.hint = hint;
	}
	
	
}
