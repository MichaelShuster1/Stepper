package servlets;

import dto.ContinutionMenuDTO;
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

@WebServlet("/continuation")
public class ContinuationServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String getBy = request.getParameter("getBy");
            String flowName = request.getParameter("flowName");
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            if (getBy == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().println("Invalid query parameter");
            }
            else {
                synchronized (this) {
                    ContinutionMenuDTO continutionMenuDTO;
                    if(getBy.equals("current")) {
                        continutionMenuDTO = engine.getContinutionMenuDTO(userManager.getUser(usernameFromSession));
                        response.setStatus(HttpServletResponse.SC_OK);
                        response.getWriter().println(Constants.GSON_INSTANCE.toJson(continutionMenuDTO));
                    }
                    else if (getBy.equals("name")){
                        if(flowName == null) {
                            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                            response.getWriter().println("Invalid query parameter");
                        }
                        else {
                            continutionMenuDTO = engine.getContinuationMenuDTOByName(userManager.getUser(usernameFromSession), flowName);
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().println(Constants.GSON_INSTANCE.toJson(continutionMenuDTO));
                        }
                    }
                    else {
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        response.getWriter().println("Invalid query parameter");
                    }
                }
            }
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String Id = request.getParameter("Id");
            String targetName = request.getParameter(Constants.FLOW_NAME);
            EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
            UserManager userManager = ServletUtils.getUserManager(getServletContext());
            if (Id == null || targetName == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
            }
            else {
                synchronized (this) {
                    engine.doContinuation(userManager.getUser(usernameFromSession), engine.getFlowExecution(Id),targetName);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        }
    }
}
