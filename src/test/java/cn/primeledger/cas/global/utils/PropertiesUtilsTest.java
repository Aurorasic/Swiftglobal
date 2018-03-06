package cn.primeledger.cas.global.utils;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * @author Su Jiulong
 * @date 2018/3/2
 */
public class PropertiesUtilsTest {
    String filePath = "src/main/resources/application.properties";

    @Test
    public void loadProps() throws Exception {
        System.out.println("data.root.path: " + PropertiesUtils.loadProps(filePath, "data.root.path"));
    }

    @Test
    public void getallProps() throws Exception {
        ArrayList<String> arrayList = PropertiesUtils.loadAllProps(filePath);
        for (String s : arrayList) {
            String value = PropertiesUtils.loadProps(filePath, s);
            System.out.println(s + " = " + value);
        }
    }

    @Test
    public void updatePropertiesFile() {
        PropertiesUtils.updateProperty(filePath,"onePiece","LuFei");
        System.out.println(PropertiesUtils.loadProps(filePath, "onePiece"));
    }

}