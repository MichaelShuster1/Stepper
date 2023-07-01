package servlets;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dto.ResultDTO;
import dto.StatisticsDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import enginemanager.Statistics;
import flow.FlowHistory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import utils.Constants;

import java.io.IOException;
import java.util.List;

@WebServlet("/get-updates")
public class GetUpdatesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson=Constants.GSON_INSTANCE;
        response.setContentType(Constants.JSON_FORMAT);
        EngineApi engine =(Manager)getServletContext().getAttribute(Constants.FLOW_MANAGER);
        String rawVersion = request.getParameter("historyVersion");

        if(rawVersion==null){
            ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
            response.getWriter().print(gson.toJson(resultDTO));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        int historyVersion=-1;
        try{
            historyVersion=Integer.parseInt(rawVersion);
        }
        catch (Exception e)
        {
            ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
            response.getWriter().print(gson.toJson(resultDTO));
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        JsonArray jsonArray=new JsonArray();
        List<FlowHistory> flowHistoryList=engine.getFlowsHistory();
        jsonArray.add(gson.toJson(flowHistoryList));
        StatisticsDTO statisticsDTO =engine.getStatistics();
        jsonArray.add(gson.toJson(flowHistoryList));
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().print(jsonArray.toString());
    }
}
