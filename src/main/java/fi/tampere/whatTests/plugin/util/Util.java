package fi.tampere.whatTests.plugin.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Util {



    public static String extractContentFromJar(String uri, String dest) throws Exception {
        URL location = null;
        try {
            location = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert location != null;
        String jarPath = location.getPath().replace("file:", "").replace("!", "");
        String resultPath = "";

        try {
            JarInputStream jar = new JarInputStream(new FileInputStream(jarPath));
            JarEntry jarEntry = null;

            while ((jarEntry = jar.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                File entry = null;
                if (jarEntryName.equals("whatTests.jar") || jarEntryName.equals("pluginConfiguration.yaml")) {
                    try {
                        entry = new File(dest, jarEntryName);
                        resultPath = entry.getPath();

                        if (entry.createNewFile()) {

                            FileOutputStream out = new FileOutputStream(entry);
                            byte[] buffer = new byte[1024];
                            int readCount = 0;

                            while ((readCount = jar.read(buffer)) >= 0) {
                                out.write(buffer, 0, readCount);
                            }

                            jar.closeEntry();
                            out.flush();
                            out.close();
                        }
                    } catch (Exception e) {
                        throw new Exception("Error on create temp file");
                    }
                }
            }
            jar.close();
        } catch (Exception e) {
            throw new Exception("Error on create temp file");
        }
        return resultPath;
    }






}
