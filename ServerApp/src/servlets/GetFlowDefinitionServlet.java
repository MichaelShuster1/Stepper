package servlets;

import com.google.gson.Gson;
import dto.FlowDefinitionDTO;
import dto.InputsDTO;
import dto.ResultDTO;
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

@WebServlet("/get-definition")
public class GetFlowDefinitionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String flowName = request.getParameter(Constants.FLOW_NAME);
            if (flowName == null) {
                ServletUtils.returnBadRequest(response);
            } else {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                synchronized (this) {
                    try {
                        FlowDefinitionDTO res = engine.getFlowDefinition(flowName);
                        response.setStatus(HttpServletResponse.SC_OK);
                        Gson gson = new Gson();
                        response.getWriter().println(gson.toJson(res));
                    }
                    catch (Exception e) {
                        ServletUtils.returnBadRequest(response);
                    }
                }
            }
        }

    }
}
