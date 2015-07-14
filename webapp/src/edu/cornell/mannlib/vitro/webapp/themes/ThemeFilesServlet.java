/* $This file is distributed under the terms of the license in /doc/license.txt$ */

package edu.cornell.mannlib.vitro.webapp.themes;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.cornell.mannlib.vitro.webapp.application.ApplicationUtils;

/**
 * Respond to a request for /vivo/themes/wilma/this.that by serving the file at
 * [Vitro_Home]/themes/wilma/this.that, if it exists.
 * 
 * Serves all requests of the form /themes/*
 */
public class ThemeFilesServlet extends HttpServlet {
	private static final Log log = LogFactory.getLog(ThemeFilesServlet.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String relativePath = figureRelativePath(req);
		Path absolutePath = figureAbsolutePath(relativePath);

		if (isValidPath(absolutePath)) {
			serveFile(resp, absolutePath);
		} else {
			serve404(resp);
		}
	}

	/**
	 * If the request is for /vivo/themes/wilma/css/screen.css, the relative
	 * path is themes/wilma/css/screen.css. Most of this is just guarding
	 * against edge-cases.
	 */
	private String figureRelativePath(HttpServletRequest req) {
		String servletPath = req.getServletPath();
		if (servletPath.charAt(0) == '/') {
			servletPath = servletPath.substring(1);
		}

		String pathInfo = req.getPathInfo();
		if (pathInfo == null) {
			pathInfo = "";
		}

		String relativePath = servletPath + pathInfo;

		if (log.isDebugEnabled()) {
			log.debug("Request for " + req.getRequestURI() + " translated to "
					+ relativePath);
		}

		return relativePath;
	}

	/**
	 * The relative path is relative to the Vitro home directory.
	 */
	private Path figureAbsolutePath(String relativePath) {
		Path absolute = ApplicationUtils.instance().getHomeDirectory()
				.getPath().resolve(relativePath);

		if (log.isDebugEnabled()) {
			log.debug("Relative path '" + relativePath
					+ "' translated to absolute path '" + absolute + "'");
		}

		return absolute;
	}

	/**
	 * Is there a file there, and can we read it? (If we can't read it, warn the
	 * user about the permissions problem.)
	 */
	private boolean isValidPath(Path path) {
		if (Files.isRegularFile(path)) {
			if (Files.isReadable(path)) {
				return true;
			} else {
				log.warn("No permission to read '" + path + "'");
			}
		}
		return false;
	}

	/**
	 * Serve the file contents. Don't worry about the MIME type: the
	 * CharsetEncodingFilter will take care of it.
	 */
	private void serveFile(HttpServletResponse resp, Path absolutePath)
			throws IOException {
		InputStream input = new BufferedInputStream(
				Files.newInputStream(absolutePath));
		IOUtils.copy(input, resp.getOutputStream());
	}

	/**
	 * No such file, or can't read it.
	 */
	private void serve404(HttpServletResponse resp) {
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
	}

}
