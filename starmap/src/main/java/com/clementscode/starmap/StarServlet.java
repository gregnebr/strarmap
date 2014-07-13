/**
 * 
 */
package com.clementscode.starmap;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Brian ... Greg
 * 
 */
public class StarServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -559556171407691234L;

	@Override
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html");
		response.setStatus(HttpServletResponse.SC_OK); // ie 404 for not found
		PrintWriter writer = response.getWriter();
		try {
			writer.println("<h1>Hello Servlet</h1>");
			writer.println("session=" + request.getSession(true).getId());
		} finally {
			writer.flush();
			writer.close();
		}
	}

}
