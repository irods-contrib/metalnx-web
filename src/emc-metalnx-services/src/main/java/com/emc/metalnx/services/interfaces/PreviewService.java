package com.emc.metalnx.services.interfaces;

import javax.servlet.http.HttpServletResponse;

public interface PreviewService {

	public boolean filePreview(String path, String mimeType, HttpServletResponse response);
	public boolean getPermission(String path);
	public String getTemplate(String mimeType);
}
