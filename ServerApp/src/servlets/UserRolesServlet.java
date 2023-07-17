package servlets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dto.ResultDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.User;
import utils.Constants;
import utils.ServletUtils;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Set;

@WebServlet("/user-roles")
public class UserRolesServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson=Constants.GSON_INSTANCE;
        response.setContentType(Constants.JSON_FORMAT);
        String jsonRoles = request.getParameter("roles");
        String username = request.getParameter("userName");
        if(jsonRoles == null || username == null) {
            ServletUtils.returnBadRequest(response);
        }
        else {
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            Type setType = new TypeToken<Set<String>>() {}.getType();
            try {
                Set<String> roleNames = gson.fromJson(jsonRoles, setType);
                Object userRolesLock = getServletContext().getAttribute(Constants.ROLE_UPDATE_LOCK);
                synchronized (userRolesLock) {
                    User user = ServletUtils.getUserManager(getServletContext()).getUser(username);
                    if(user!=null) {
                        engine.updateUserRoles(user, roleNames);
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.setContentType(Constants.JSON_FORMAT);
                        ResultDTO resultDTO = new ResultDTO("the user: "+ username+" dont exist in the system");
                        response.getWriter().print(gson.toJson(resultDTO));
                    }
                }

            }
            catch (Exception e) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO = new ResultDTO("Server failed to update user roles");
                response.getWriter().print(gson.toJson(resultDTO));
            }
        }
    }
}
