package servlets;

import dto.ResultDTO;
import dto.RoleInfoDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;
import utils.ServletUtils;

import java.io.IOException;

@WebServlet("/new-role")
public class NewRoleServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {

        response.setContentType(Constants.JSON_FORMAT);
        String jsonRole = request.getParameter("newRole");
        if(jsonRole == null) {
            ServletUtils.returnBadRequest(response);
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
                else {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    response.setContentType(Constants.JSON_FORMAT);
                    ResultDTO resultDTO=new ResultDTO("New role not created, a role with this name already exists.");
                    response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                }
            }
            catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO("Server failed to add the role");
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
        }
    }
}
