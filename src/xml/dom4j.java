package xml;

import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class dom4j {
    public static Document load(String filename) {
        Document document = null;
        try {
            SAXReader saxReader = new SAXReader();
            document = saxReader.read(new File(filename));  //读取XML文件,获得document对象
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return document;
    }

    public static void write(Document document, String filename) {
        try {
            XMLWriter writer = new XMLWriter(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));
            writer.write(document);
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
