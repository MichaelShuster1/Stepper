package servlets;

import com.google.gson.Gson;
import dto.FreeInputExecutionDTO;
import dto.ResultDTO;
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


@WebServlet("/input-parameters")
public class InputParametersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String inputName =request.getParameter("inputName");
            if(inputName==null){
                ServletUtils.returnBadRequest(response);
            }
            else {
               // synchronized (this){ //maybe synchronized is not necessary here (need to verify that later)
                    EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                    User user = ServletUtils.getUserManager(getServletContext()).getUser(usernameFromSession);
                    try {
                        FreeInputExecutionDTO freeInputExecutionDTO = engine.getInputData(user, inputName);
                        response.getWriter().println(Constants.GSON_INSTANCE.toJson(freeInputExecutionDTO));
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    catch (Exception e) {
                        ServletUtils.returnBadRequest(response);
                    }
              //  }
            }
        }
    }
}
