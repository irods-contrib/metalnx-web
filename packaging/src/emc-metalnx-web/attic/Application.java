/**
 * 
 */
package org.irods.jargon.metalnx.web.boot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;

/**
 * @author Mike Conway - NIEHS
 *
 */
@SpringBootApplication
@Configuration
@ImportResource({ "classpath:applicationContext.xml" })
public class Application extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		// Customize the application or call application.sources(...) to add sources
		// Since our example is itself a @Configuration class (via
		// @SpringBootApplication)
		// we actually don't need to override this method.
		return application;
	}

}
