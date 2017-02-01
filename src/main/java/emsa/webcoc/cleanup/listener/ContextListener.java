/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emsa.webcoc.cleanup.listener;

import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * Web application lifecycle listener.
 *
 * @author aanciaes
 */
@WebListener
public class ContextListener implements ServletContextListener {

    private final Logger logger = LogManager.getLogger(ContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();

        ReadConfig readConf = new ReadConfig();

        Set<Map.Entry<Object, Object>> set = readConf.getValues();
        for (Map.Entry entry : set) {
            context.setAttribute((String) entry.getKey(), entry.getValue());
        }
        
        logger.info("Context Initialized");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("Context Destroyed");
        LogManager.shutdown();
    }
}
