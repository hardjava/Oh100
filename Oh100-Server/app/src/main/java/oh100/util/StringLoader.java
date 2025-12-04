package oh100.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class StringLoader {
    private Properties properties = new Properties();

    public StringLoader()
    {
        try {
            InputStream input = getClass().getClassLoader().getResourceAsStream("string.properties");

            if (input == null) {
                System.out.println("Cannot find property file.");

                return;
            }

            properties.load(input);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key)
    {
        return properties.getProperty(key);
    }
}
