package servlets;

import com.google.gson.JsonArray;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;

import java.io.IOException;

@WebServlet("/get-updates")
public class GetUpdatesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Constants.JSON_FORMAT);
        EngineApi engine =(Manager)getServletContext().getAttribute(Constants.FLOW_MANAGER);
        JsonArray jsonArray=new JsonArray();
    }
}
