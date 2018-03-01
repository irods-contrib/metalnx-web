package com.emc.metalnx.services.interfaces;

import javax.servlet.http.HttpServletResponse;

public interface PreviewService {

	public boolean filePreview(String path, HttpServletResponse response);
	public boolean getPermission(String path);
}
