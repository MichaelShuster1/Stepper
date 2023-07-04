package servlets;


import dto.ResultDTO;
import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import users.UserManager;
import utils.Constants;
import utils.ServletUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;


@WebServlet("/upload-file")
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 1024 * 1024 * 5, maxRequestSize = 1024 * 1024 * 5 * 5)
public class FileUploadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        Collection<Part> parts = request.getParts();
        EngineApi engine= (Manager) getServletContext().getAttribute(Constants.FLOW_MANAGER);
        UserManager userManager = ServletUtils.getUserManager(getServletContext());

        for (Part part : parts) {
            try {
                engine.loadXmlFile(part.getInputStream(), userManager.getUsersMap());
                response.setStatus(HttpServletResponse.SC_OK);
            } catch (Exception e) {
                response.setContentType(Constants.JSON_FORMAT);
                ResultDTO resultDTO=new ResultDTO(e.getMessage());
                response.getWriter().print(Constants.GSON_INSTANCE.toJson(resultDTO));
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        }

        List<String> flowsNames=engine.getFlowsNames();

        for(String flowName:flowsNames)
            System.out.println(flowName);

    }
}
