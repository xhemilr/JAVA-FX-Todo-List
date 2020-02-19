package sample.connection;

import java.io.*;
import java.util.Properties;

public class PropertiesCache {
    private static final PropertiesCache INSTANCE = new PropertiesCache();
    private final Properties configProp = new Properties();

    private PropertiesCache(){
        try {
            InputStream in = new FileInputStream("database.properties");
            configProp.load(in);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public static PropertiesCache getInstance(){
        return INSTANCE;
    }

    public void writeProperty(){
        try {
            OutputStream out = new FileOutputStream("database.properties");
            configProp.store(out, null);
        }catch (IOException e){
            System.out.println(e.getMessage());
        }
    }

    public String getProperty(String key){
        return configProp.getProperty(key);
    }

    public void setProperty(String key, String value){
        configProp.setProperty(key, value);
    }
}
