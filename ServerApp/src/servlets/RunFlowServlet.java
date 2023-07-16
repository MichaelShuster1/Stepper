package servlets;

import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.User;
import utils.Constants;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/run-flow")
public class RunFlowServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
           // synchronized (this){
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                User user = ServletUtils.getUserManager(getServletContext()).getUser(usernameFromSession);
                try {
                    String flowId = engine.runFlow(user);
                    response.setContentType(Constants.TEXT_FORMAT);
                    response.getWriter().println(flowId);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                catch (Exception e) {
                    ServletUtils.returnBadRequest(response);
                }
           // }
        }
    }
}
