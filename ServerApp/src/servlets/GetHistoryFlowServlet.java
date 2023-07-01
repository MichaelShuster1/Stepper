package servlets;

import dto.FlowExecutionDTO;
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

@WebServlet("/get-history")
public class GetHistoryFlowServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String usernameFromSession = SessionUtils.getUsername(request);
        if(ServletUtils.checkAuthorization(usernameFromSession, response)) {
            response.setContentType(Constants.JSON_FORMAT);
            String flowId = request.getParameter("flowId");
            if(flowId==null){
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO(Constants.INVALID_PARAMETER);
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            else{
                synchronized (this) {
                    flowId=flowId.trim();
                    EngineApi engine = (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
                    FlowExecutionDTO flowExecutionDTO = engine.getHistoryDataOfFlow(flowId);
                    response.getWriter().println(Constants.GSON_INSTANCE.toJson(flowExecutionDTO));
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        }
    }
}
