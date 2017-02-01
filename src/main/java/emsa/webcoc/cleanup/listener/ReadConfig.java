/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package emsa.webcoc.cleanup.listener;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author aanciaes
 */
public class ReadConfig {
    
    private final Logger logger = LogManager.getLogger(ReadConfig.class);
    
    private Properties prop;
    
    public ReadConfig () {
        
        logger.info("ReadConfig Initialized");
        
        InputStream inputStream = null;
         
        try{
            prop = new Properties();
            inputStream = getClass().getClassLoader().getResourceAsStream("config.properties");
            
            if(inputStream != null){
                logger.info("Reding properties");
                prop.load(inputStream);
                inputStream.close();
            }else{
                logger.fatal("Can't open config.properties file");
            }          
        }catch (IOException e ) {
            logger.fatal("I/O Exception");
        }
    }
    
    public Set<Map.Entry<Object, Object>> getValues () {
        return prop.entrySet();
    }
}
