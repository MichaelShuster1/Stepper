package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.AvailableFlowDTO;
import dto.FreeInputExecutionDTO;
import dto.ResultDTO;
import dto.RoleInfoDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
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
import java.util.Set;

@WebServlet("/role")
public class RolesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Constants.JSON_FORMAT);
        String roleName =request.getParameter("roleName");
        Gson gson=Constants.GSON_INSTANCE;
        if(roleName==null){
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
            response.getWriter().print(gson.toJson(resultDTO));
        }
        else {
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            RoleInfoDTO roleInfo = engine.getRoleInfo(roleName);
            response.getWriter().print(gson.toJson(roleInfo));
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }

    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType(Constants.JSON_FORMAT);
        String jsonRole = request.getParameter("newRole");
        if(jsonRole == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
            response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
        }
        else {
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);

            try {
                RoleInfoDTO roleInfoDTO = Constants.GSON_INSTANCE.fromJson(jsonRole, RoleInfoDTO.class);
                boolean success;
                synchronized (this) {
                    success = engine.addRole(roleInfoDTO);
                }
                if(success)
                    response.setStatus(HttpServletResponse.SC_OK);
                else
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
            }
            catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO("Server failed to add the role");
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType(Constants.JSON_FORMAT);
        String jsonFlows = request.getParameter("flows");
        String roleName = request.getParameter("roleName");
        if(jsonFlows == null || roleName == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
            response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
        }
        else {
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            Type setType = new TypeToken<Set<String>>() {}.getType();
            try {
                Set<String> flowNames = Constants.GSON_INSTANCE.fromJson(jsonFlows, setType);
                synchronized (this) {
                    engine.updateRole(roleName, flowNames, userManager.getUsersMap());
                }
                response.setStatus(HttpServletResponse.SC_OK);
            }
            catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO("Server failed to update user roles");
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
        }
    }
}
