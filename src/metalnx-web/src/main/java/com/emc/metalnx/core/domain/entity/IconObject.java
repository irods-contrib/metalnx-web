package com.emc.metalnx.core.domain.entity;

public class IconObject {

	private String type;
	private String mimeIcon;
	
	public IconObject(String type , String mimeIcon) {
		this.type = type;
		this.mimeIcon = mimeIcon;
	}
	
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMimeIcon() {
		return mimeIcon;
	}
	public void setMimeIcon(String mimeIcon) {
		this.mimeIcon = mimeIcon;
	}
	
	
}
