package com.emc.metalnx.services.interfaces;

import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface PreviewService {
	
	public ResponseEntity<byte[]> filePreview(String path, HttpServletResponse response);
	
	public HttpHeaders getHeader();
}
