package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import dto.FreeInputExecutionDTO;
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
import java.lang.reflect.Type;
import java.util.List;

@WebServlet("/roles")
public class RolesServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

            response.setContentType(Constants.JSON_FORMAT);
            String jsonRoles = request.getParameter("roles");
            String username = request.getParameter("userName");
            if(jsonRoles == null || username == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
            else {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                UserManager userManager = ServletUtils.getUserManager(getServletContext());

            }
    }
}
