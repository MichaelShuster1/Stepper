package servlets;


import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

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
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();

        Collection<Part> parts = request.getParts();

        StringBuilder fileContent = new StringBuilder();

        if(getServletContext().getAttribute("FlowManager")==null)
            getServletContext().setAttribute("FlowManager",new Manager());

        EngineApi engine= (Manager) getServletContext().getAttribute("FlowManager");

        for (Part part : parts) {
            try {
                engine.loadXmlFile(part.getInputStream());
            } catch (JAXBException e) {
                throw new RuntimeException(e);
            }
        }

        List<String> flowsNames=engine.getFlowsNames();

        for(String flowName:flowsNames)
            System.out.println(flowName);






    }
}
