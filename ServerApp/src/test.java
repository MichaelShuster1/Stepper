import dto.FlowExecutionDTO;
import dto.ResultDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import utils.Constants;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@WebServlet(name="test",urlPatterns = "/test")
public class test  extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.getWriter().println("test");
        System.out.println("test");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StringBuilder requestBody = new StringBuilder();
        BufferedReader reader = request.getReader();

        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        System.out.println(requestBody);
        EngineApi engine=(Manager)getServletContext().getAttribute(Constants.FLOW_MANAGER);
        List<FlowExecutionDTO> flowExecutionDTOList =engine.getFlowsHistoryDelta(0);
        response.getWriter().print(Constants.GSON_INSTANCE.toJson(flowExecutionDTOList));
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StringBuilder requestBody = new StringBuilder();
        BufferedReader reader = request.getReader();

        String line;
        while ((line = reader.readLine()) != null) {
            requestBody.append(line);
        }
        System.out.println(requestBody);
        response.getWriter().print(requestBody);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }
}
