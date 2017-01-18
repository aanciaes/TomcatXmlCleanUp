/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaResources;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 *
 * @author miguel
 */
public class ReadConfig {
    
    private Properties prop;
    
    public ReadConfig () {
        try{
            prop = new Properties();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("/resources/config/config.properties");
            
            if(inputStream != null){
                prop.load(inputStream);
            }else{
                System.err.println("Err input Stream == null");
            }          
        }catch (IOException e ) {
            System.out.println("I/O Exception");
        }
    }
    
    public Set<Map.Entry<Object, Object>> getValues () {
        return prop.entrySet();
    }
}
