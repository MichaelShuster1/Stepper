package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import dto.ResultDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;

import java.io.IOException;



@WebServlet("/admin")
public class AdminConnectServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        synchronized (getServletContext()) {

            Boolean adminConnected = (Boolean) getServletContext().getAttribute(Constants.ADMIN_CONNECTED);
            if(adminConnected==null)
                adminConnected=false;

            response.setContentType(Constants.JSON_FORMAT);
            Gson gson = Constants.GSON_INSTANCE;
            if (adminConnected) {
                ResultDTO resultDTO=new ResultDTO("Admin already connected to the server!");
                response.getWriter().print(gson.toJson(resultDTO));
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
            else {
                getServletContext().setAttribute(Constants.ADMIN_CONNECTED, true);
                JsonArray jsonArray=new JsonArray();
                EngineApi engine=(Manager)getServletContext().getAttribute(Constants.FLOW_MANAGER);
                jsonArray.add(gson.toJson(engine.getFlowsNames()));
                jsonArray.add(gson.toJson(engine.getRolesNames()));
                response.getWriter().print(jsonArray.toString());
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().setAttribute(Constants.ADMIN_CONNECTED,false);
    }
}
