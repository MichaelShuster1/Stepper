package contextlistener;

import enginemanager.EngineApi;
import enginemanager.Manager;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import utils.Constants;

import javax.crypto.Cipher;

@WebListener
public class AppServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        servletContext.setAttribute(Constants.FLOW_MANAGER,new Manager());
        servletContext.setAttribute(Constants.FLOWS_LOCK,new Object());
        servletContext.setAttribute(Constants.USERS_LOCK,new Object());
        servletContext.setAttribute(Constants.ROLE_UPDATE_LOCK,new Object());
        servletContext.setAttribute(Constants.USER_ROLES_LOCK,new Object());
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        EngineApi engine= (Manager)servletContext.getAttribute(Constants.FLOW_MANAGER);
        engine.endProcess();
    }
}
