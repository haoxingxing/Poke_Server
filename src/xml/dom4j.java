package xml;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import process.aes;
import process.md5;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

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

    public static String xmltostring(Document doc, OutputFormat outputFormat) {
        try {
            StringWriter s = new StringWriter();
            XMLWriter xmlWriter = new XMLWriter(s, outputFormat);
            xmlWriter.write(doc);
            return s.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public static Document retxmlmodel(String status, String message, String clasp, String func, Element parameter) {
        Document doc = DocumentHelper.createDocument();
        Element root = doc.addElement("reply");
        root.addElement("status").setText(status);
        root.addElement("class").setText(clasp);
        root.addElement("func").setText(func);
        root.addElement("message").setText(message);
        root.add(parameter);
        return doc;
    }

    public static OutputFormat makeOF() {
        OutputFormat outputFormat = OutputFormat.createPrettyPrint();
        outputFormat.setEncoding("UTF-8");
        outputFormat.setIndent(false); //设置是否缩进
        outputFormat.setNewlines(true); //设置是否换行
        return outputFormat;
    }

    public static Document retaesprocess(Document doc, String token) {
        String encrypet = aes.encrypt(xmltostring(doc, makeOF()), Objects.requireNonNull(md5.md5_encode(token + "MakeTokenEnc")));
        Document docret = DocumentHelper.createDocument();
        Element root = docret.addElement("reply");
        root.addElement("token").setText(token);
        root.addElement("data").setText(encrypet);
        root.addElement("md5").setText(md5.md5_encode(encrypet + token));
        return docret;
    }
}
