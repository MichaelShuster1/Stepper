package servlets;

import com.google.gson.Gson;
import dto.InputsDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/input-options")
public class InputsOptionsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String buttonId = request.getParameter("Id");
            if (buttonId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid query parameter");
            } else {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                UserManager userManager = ServletUtils.getUserManager(getServletContext());
                synchronized (this) {
                    String data =engine.getInputData(userManager.getUser(usernameFromSession), buttonId).getData();
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(Constants.GSON_INSTANCE.toJson(data));
                }
            }
        }
    }

    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String buttonId = request.getParameter("Id");
            if (buttonId == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid query parameter");
            } else {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                UserManager userManager = ServletUtils.getUserManager(getServletContext());
                synchronized (this) {
                    Boolean necessity =engine.clearInputData(userManager.getUser(usernameFromSession) , buttonId).getNecessity();
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.getWriter().println(Constants.GSON_INSTANCE.toJson(necessity));
                }
            }
        }
    }

}
