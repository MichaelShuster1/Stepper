package servlets;

import com.google.gson.Gson;
import dto.AvailableFlowDTO;
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
import java.util.List;

@WebServlet("/get-flows")
public class AvailableFlowsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession,response)) {
            Gson gson = new Gson();
            response.setContentType(Constants.JSON_FORMAT);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());

            synchronized (this) {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                engine.updateUserFlows(userManager.getUser(usernameFromSession));
                List<AvailableFlowDTO> flows = engine.getAvailableFlows();
                if (flows != null) {
                    response.getWriter().println(gson.toJson(flows));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                }
            }
        }

    }

}
