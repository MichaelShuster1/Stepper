package contextlistener;

import enginemanager.Manager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import utils.Constants;

@WebListener
public class AppServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute(Constants.FLOW_MANAGER,new Manager());
    }
}
