package servlets;

import com.google.gson.JsonArray;
import dto.FlowExecutionDTO;
import dto.ResultDTO;
import dto.StatisticsDTO;
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
import java.util.List;

@WebServlet("/get-history")
public class getUserHistoryServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            EngineApi engine =(Manager)getServletContext().getAttribute(Constants.FLOW_MANAGER);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            String rawVersion = request.getParameter("historyVersion");

            if(rawVersion==null){
                ServletUtils.returnBadRequest(response);
            }
            else {
                int historyVersion = -1;
                try {
                    historyVersion = Integer.parseInt(rawVersion);
                    synchronized (this) {
                        User user = userManager.getUser(usernameFromSession);
                        List<FlowExecutionDTO> flowHistoryList;
                        if(user.isManager())
                            flowHistoryList = engine.getFlowsHistoryDelta(historyVersion);
                        else
                            flowHistoryList = engine.getFlowsHistoryDeltaFromUser(historyVersion, user);

                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().print(Constants.GSON_INSTANCE.toJson(flowHistoryList));
                    }
                } catch (Exception e) {
                    ServletUtils.returnBadRequest(response);
                }
            }
        }
    }
}
