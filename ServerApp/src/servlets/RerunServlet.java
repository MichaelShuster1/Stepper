package servlets;

import com.google.gson.reflect.TypeToken;
import dto.FreeInputExecutionDTO;
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
import java.lang.reflect.Type;
import java.util.List;

@WebServlet("/rerun")
public class RerunServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String jsonInputs = request.getParameter("freeInputs");
            String flowName = request.getParameter(Constants.FLOW_NAME);
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            if (jsonInputs == null || flowName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
            else {
                try {
                    Type listType = new TypeToken<List<FreeInputExecutionDTO>>() {
                    }.getType();
                    List<FreeInputExecutionDTO> inputs = Constants.GSON_INSTANCE.fromJson(jsonInputs, listType);
                    synchronized (this) {
                        engine.reUseInputsData(userManager.getUser(usernameFromSession), inputs, flowName);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                }
                catch (Exception e) {
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    response.setContentType(Constants.JSON_FORMAT);
                    ResultDTO resultDTO=new ResultDTO("Server failed to get free inputs");
                    response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                }
            }
        }
    }
}
