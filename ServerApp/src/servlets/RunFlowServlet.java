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
            response.setContentType("text/plain");
            synchronized (this){
                EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                User user = ServletUtils.getUserManager(getServletContext()).getUser(usernameFromSession);
                String flowId=engine.runFlow(user);
                response.getWriter().println(flowId);
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
    }
}
