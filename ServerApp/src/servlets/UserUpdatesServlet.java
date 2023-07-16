package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dto.AvailableFlowDTO;
import dto.ResultDTO;
import dto.UserInfoDTO;
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

@WebServlet("/user-updates")
public class UserUpdatesServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession,response)) {
            Gson gson = Constants.GSON_INSTANCE;
            response.setContentType(Constants.JSON_FORMAT);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());

            //synchronized (this) {
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                User user=userManager.getUser(usernameFromSession);
                JsonArray jsonArray=new JsonArray();
                List<AvailableFlowDTO> flows = engine.getAvailableFlows(user);
                int currentVersion = engine.getHistoryVersion();
                UserInfoDTO userInfoDTO=user.getUserInformation(currentVersion);
                jsonArray.add(gson.toJson(flows));
                jsonArray.add(gson.toJson(userInfoDTO));
                response.getWriter().print(jsonArray.toString());
                response.setStatus(HttpServletResponse.SC_OK);
           // }
        }

    }

}
