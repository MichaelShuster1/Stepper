package servlets;

import com.google.gson.Gson;
import dto.AvailableFlowDTO;
import dto.ResultDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.User;
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
                User user=userManager.getUser(usernameFromSession);
                //engine.updateUserFlows(userManager.getUser(usernameFromSession));
                List<AvailableFlowDTO> flows = engine.getAvailableFlows(user);
                if (flows != null) {
                    response.getWriter().println(gson.toJson(flows));
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType(Constants.JSON_FORMAT);
                    ResultDTO resultDTO=new ResultDTO("There are no available flows");
                    response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                }
            }
        }

    }

}
