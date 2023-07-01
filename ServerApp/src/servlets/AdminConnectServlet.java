package servlets;

import dto.ResultDTO;
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
            if (getServletContext().getAttribute(Constants.ADMIN_CONNECTED) == null) {
                getServletContext().setAttribute(Constants.ADMIN_CONNECTED, true);
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                boolean adminConnected = (Boolean) getServletContext().getAttribute(Constants.ADMIN_CONNECTED);
                if (adminConnected) {
                    response.setContentType(Constants.JSON_FORMAT);
                    ResultDTO resultDTO=new ResultDTO("Admin already connected to the server!");
                    response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
                else {
                    getServletContext().setAttribute(Constants.ADMIN_CONNECTED, true);
                    response.setStatus(HttpServletResponse.SC_OK);
                }
            }
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        getServletContext().setAttribute(Constants.ADMIN_CONNECTED,false);
    }
}
