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

@WebServlet("/get-inputs")
public class getFlowInputsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String flowName = request.getParameter(Constants.FLOW_NAME);
            if (flowName == null) {
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO("Invalid query parameter");
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            } else {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                UserManager userManager = ServletUtils.getUserManager(getServletContext());
                synchronized (this) {
                    InputsDTO inputsDTO = engine.getFlowInputs(userManager.getUser(usernameFromSession), flowName);
                    response.setStatus(HttpServletResponse.SC_OK);
                    Gson gson = new Gson();
                    response.getWriter().println(gson.toJson(inputsDTO));
                }
            }
        }

    }
}
