package utils;


import dto.ResultDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;


public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";

	private static final Object userManagerLock = new Object();

	public static UserManager getUserManager(ServletContext servletContext) {

		synchronized (userManagerLock) {
			if (servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(USER_MANAGER_ATTRIBUTE_NAME, new UserManager());
			}
		}
		return (UserManager) servletContext.getAttribute(USER_MANAGER_ATTRIBUTE_NAME);
	}

	public static Boolean checkAuthorization(String usernameFromSession, HttpServletResponse response) {
		if(usernameFromSession == null) {
			try {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType(Constants.JSON_FORMAT);
				ResultDTO resultDTO=new ResultDTO(Constants.UNAUTHORIZED_ACCESS);
				response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
			}
			catch (Exception e) {
				System.out.println("Something went wrong...");
			}
			return false;
		}
		return true;
	}

}
