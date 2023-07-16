package servlets;

import com.google.gson.Gson;
import dto.UserInfoDTO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import users.User;
import users.UserManager;
import utils.Constants;
import utils.ServletUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@WebServlet("/get-users")
public class GetUsersListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Gson gson = new Gson();
        response.setContentType(Constants.JSON_FORMAT);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());
        Map<String,User> users= userManager.getUsersMap();

        Object usersLock=getServletContext().getAttribute(Constants.USERS_LOCK);
        synchronized (usersLock) {
            if (users.size() != 0) {
                List<UserInfoDTO> userInfoDTOList = new ArrayList<>();

                Set<String> usersNames = users.keySet();
                for (String userName : users.keySet())
                    userInfoDTOList.add(users.get(userName).getUserInformation());

                response.getWriter().println(gson.toJson(userInfoDTOList));
                response.setStatus(HttpServletResponse.SC_OK);
            }
        }


    }
}
