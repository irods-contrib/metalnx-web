package com.emc.metalnx.services.interfaces;

import javax.servlet.http.HttpServletResponse;

public interface PreviewService {
	
	public byte[] filePreview(String path, HttpServletResponse response);
}
