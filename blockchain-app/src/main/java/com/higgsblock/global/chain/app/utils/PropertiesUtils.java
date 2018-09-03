package com.higgsblock.global.chain.app.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;

/**
 * @author Su Jiulong
 * @date 2018-3-2
 */
@Slf4j
public class PropertiesUtils {

    private static Properties properties;

    /**
     * Load application.properties file
     *
     * @param filePath : application.properties file path
     *                 The absolute path of the relative path is ok.
     * @param key      : Get the corresponding configuration through the key.
     * @return Configuration information
     */
    public static String loadProps(String filePath, String key) {
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("filePath is blank,Please check the input");
        }
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("key is blank,Please check the input");
        }
        properties = new Properties();
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            properties.load(inputStream);
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.error("The configuration file not found.", e);
        } catch (IOException e) {
            LOGGER.error("Failed to read the configuration file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close inputStream", e);
                }
            }
        }
        return properties.getProperty(key);
    }

    /**
     * read all properties form the filePath.
     *
     * @param filePath : application.properties file path
     *                 The absolute path of the relative path is ok.
     * @return keysList of all properties's name
     */
    public static ArrayList<String> loadAllProps(String filePath) {
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("filePath is blank,Please check the input");
        }
        properties = new Properties();
        ArrayList<String> keysList = null;
        InputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(filePath));
            //Load the configuration file from the inputStream.
            properties.load(inputStream);
            Enumeration en = properties.propertyNames();
            keysList = new ArrayList<String>();
            while (en.hasMoreElements()) {
                String key = (String) en.nextElement();
                keysList.add(key);
            }
        } catch (FileNotFoundException | NullPointerException e) {
            LOGGER.error("The configuration file not found.", e);
            throw new RuntimeException("The configuration file not found.");
        } catch (IOException e) {
            LOGGER.error("Failed to read all properties from the configuration file", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close inputStream", e);
                }
            }
        }
        return keysList;
    }

    /**
     * Change the profile information.
     *
     * @param filePath : application.properties file path
     *                 The absolute path of the relative path is ok.
     * @param keyName  : Get the corresponding configuration through the key.
     */
    public static void updateProperty(String filePath, String keyName, String keyValue) {
        if (StringUtils.isBlank(filePath)) {
            throw new RuntimeException("filePath is blank,Please check the input");
        }
        if (StringUtils.isBlank(keyName)) {
            throw new RuntimeException("keyName is blank,Please check the input");
        }
        if (StringUtils.isBlank(keyValue)) {
            throw new RuntimeException("keyValue is blank,Please check the input");
        }
        FileOutputStream fios = null;
        try {
            if (!new File(filePath).exists()) {
                throw new FileNotFoundException("the configuration file does not exist");
            }
            properties = new Properties();
            properties.setProperty(keyName, keyValue);
            fios = new FileOutputStream(filePath, true);
            properties.store(fios, "add or update the config");
        } catch (FileNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
        } catch (IOException e) {
            LOGGER.error("Failed to update the configuration file", e);
        } finally {
            if (fios != null) {
                try {
                    fios.close();
                } catch (IOException e) {
                    LOGGER.error("Failed to close putPutStream", e);
                }
            }
        }
    }
}
