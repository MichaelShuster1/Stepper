package utils;


import chat.ChatManager;
import dto.ResultDTO;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import okhttp3.Response;
import users.UserManager;

import java.io.IOException;

import static utils.Constants.INT_PARAMETER_ERROR;


public class ServletUtils {

	private static final String USER_MANAGER_ATTRIBUTE_NAME = "userManager";

	private static final String CHAT_MANAGER_ATTRIBUTE_NAME = "chatManager";

	private static final Object userManagerLock = new Object();
	private static final Object chatManagerLock = new Object();

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

	public static ChatManager getChatManager(ServletContext servletContext) {
		synchronized (chatManagerLock) {
			if (servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME) == null) {
				servletContext.setAttribute(CHAT_MANAGER_ATTRIBUTE_NAME, new ChatManager());
			}
		}
		return (ChatManager) servletContext.getAttribute(CHAT_MANAGER_ATTRIBUTE_NAME);
	}

	public static int getIntParameter(HttpServletRequest request, String name) {
		String value = request.getParameter(name);
		if (value != null) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException numberFormatException) {
			}
		}
		return INT_PARAMETER_ERROR;
	}

	public static void returnBadRequest(HttpServletResponse response) throws IOException {
		response.setContentType(Constants.JSON_FORMAT);
		ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
		response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
		response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	}


}
