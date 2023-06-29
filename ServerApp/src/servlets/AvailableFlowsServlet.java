package servlets;

import com.google.gson.Gson;
import dto.AvailableFlowDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/get-flows")
public class AvailableFlowsServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");

        EngineApi engine= (Manager) getServletContext().getAttribute("FlowManager");
        List<AvailableFlowDTO> flows = engine.getAvailableFlows();
        if(flows != null) {
            response.getWriter().println(gson.toJson(flows));
            response.setStatus(HttpServletResponse.SC_OK);
        }
        else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

}
