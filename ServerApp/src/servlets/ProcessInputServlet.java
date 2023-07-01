package servlets;

import dto.ResultDTO;
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

@WebServlet("/process-input")
public class ProcessInputServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String inputName=request.getParameter("inputName");
            String data=request.getParameter("data");

            if(inputName==null||data==null){
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else {
                synchronized (this) { //maybe synchronized is not necessary here (need to verify that later)
                    EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                    User user = ServletUtils.getUserManager(getServletContext()).getUser(usernameFromSession);
                    ResultDTO resultDTO = engine.processInput(user, inputName, data);
                    response.getWriter().println(Constants.GSON_INSTANCE.toJson(resultDTO));
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        }
    }
}
