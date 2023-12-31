package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if (ServletUtils.checkAuthorization(usernameFromSession, response)) {
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            Object userRolesLock = getServletContext().getAttribute(Constants.ROLE_UPDATE_LOCK);
            synchronized (userRolesLock) {
                userManager.removeUser(usernameFromSession);
            }
            SessionUtils.clearSession(request);
        }
    }
}
