package servlets;

import com.google.gson.Gson;
import dto.FlowDefinitionDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.ServletUtils;
import utils.SessionUtils;

import java.io.IOException;

@WebServlet("/get-definition")
public class GetFlowDefinitionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String query = request.getParameter("flowName");
        if(query == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Invalid query parameter");
        }
        else {
            EngineApi engine = (Manager) getServletContext().getAttribute("FlowManager");
            synchronized (this) {
                FlowDefinitionDTO res = engine.getFlowDefinition(query);
                response.setStatus(HttpServletResponse.SC_OK);
                Gson gson = new Gson();
                response.getWriter().println(gson.toJson(res));
            }
        }

    }
}
