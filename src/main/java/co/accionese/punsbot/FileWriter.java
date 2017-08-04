package co.accionese.punsbot;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class FileWriter {

    public boolean writeImage(String token, String filePath) throws MalformedURLException {
        String[] name = filePath.split("/");
        File archive = new File("C:\\Users\\Desktop\\IdeaProjects\\PunsBot\\images\\"+name[1]);
        URL url = new URL("https://api.telegram.org/file/bot"+token+"/"+filePath);
        try {
            FileUtils.copyURLToFile(url, archive);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean writeDocument(String token, String filePath) throws MalformedURLException {
        String[] name = filePath.split("/");
        File archive = new File("C:\\Users\\Desktop\\IdeaProjects\\PunsBot\\documents\\"+name[1]);
        URL url = new URL("https://api.telegram.org/file/bot"+token+"/"+filePath);
        try {
            FileUtils.copyURLToFile(url, archive);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
