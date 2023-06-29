package servlets;

import com.google.gson.Gson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.UserManager;
import utils.ServletUtils;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@WebServlet("/get-users")
public class GetUsersListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType("application/json");

        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        Set<String> users= userManager.getUsers();

        if(users.size()!=0)
        {
            response.getWriter().println(gson.toJson(users));
            response.setStatus(HttpServletResponse.SC_OK);
        }

    }
}
