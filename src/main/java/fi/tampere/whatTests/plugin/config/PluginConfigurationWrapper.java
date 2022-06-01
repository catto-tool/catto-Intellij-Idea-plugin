package fi.tampere.whatTests.plugin.config;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.intellij.openapi.project.Project;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class PluginConfigurationWrapper {
    private final String TMP_JAR_FOLDER_RELATIVE_PATH = ".whatTests" + File.separator + "jarTmp";
    private final String JAR_FILE_RELATIVE_PATH = TMP_JAR_FOLDER_RELATIVE_PATH + File.separator + "whatTests.jar";
    private final String JAR_FILE_ORIGINAL_PATH = Objects.requireNonNull(PluginConfigurationWrapper.class.getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", "");
    private final String CONFIGURATION_FILE_RELATIVE_PATH = ".whatTests" + File.separator + "pluginConfiguration.yaml";
    private final String CONFIGURATION_FILE_ABSOLUTE_PATH;
    private final String CONFIGURATION_FOLDER_RELATIVE_PATH =".whatTests";
    private final String CONFIGURATION_FOLDER_ABSOLUTE_PATH;
    private final String TMP_JAR_FOLDER_ABSOLUTE_PATH;
    private final String JAR_FILE_ABSOLUTE_PATH;
    private final String PROJECT_PATH;

    public PluginConfigurationWrapper(Project project){
        this.TMP_JAR_FOLDER_ABSOLUTE_PATH = project.getBasePath() + File.separator + TMP_JAR_FOLDER_RELATIVE_PATH;
        this.CONFIGURATION_FILE_ABSOLUTE_PATH = project.getBasePath() + File.separator + CONFIGURATION_FILE_RELATIVE_PATH;
        this.JAR_FILE_ABSOLUTE_PATH = project.getBasePath() + File.separator + JAR_FILE_RELATIVE_PATH;
        this.CONFIGURATION_FOLDER_ABSOLUTE_PATH = project.getBasePath() + File.separator + CONFIGURATION_FOLDER_RELATIVE_PATH;
        this.PROJECT_PATH = project.getBasePath();
        this.initOrLoadConfig();

    }

    public void setEnabled(boolean enabled) throws IOException {
        PluginConfiguration pluginConfiguration = this.getConfig();
        pluginConfiguration.setEnabled(enabled);
        this.updateConfig(pluginConfiguration);
    }

    public PluginConfiguration getConfig() {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        if (!new File(CONFIGURATION_FILE_ABSOLUTE_PATH).exists()){
            return null;
        }
        try {
           return om.readValue(new File(CONFIGURATION_FILE_ABSOLUTE_PATH), PluginConfiguration.class);

        } catch (Exception e) {
            throw new RuntimeException();
        }

    }


    private void updateConfig(PluginConfiguration pluginConfiguration) throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(new File(CONFIGURATION_FILE_ABSOLUTE_PATH), pluginConfiguration);
    }


    private PluginConfiguration initOrLoadConfig(){
        if (this.getConfig() == null){
            this.initConfig();
        }
        return this.getConfig();
    }



    private PluginConfiguration initConfig() {
        PluginConfiguration pluginConfiguration = new PluginConfiguration(true, false, PROJECT_PATH, JAR_FILE_ABSOLUTE_PATH);
        //this.copyClassesInTmp(project);
        try {
            this.extractContentFromJar();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        pluginConfiguration.setInitialized(true);
        try {
            this.updateConfig(pluginConfiguration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return pluginConfiguration;
    }




    private void extractContentFromJar() throws Exception {
        URL location = null;
        try {
            location = new URL(JAR_FILE_ORIGINAL_PATH);
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
                if (jarEntryName.equals("whatTests.jar")) {
                    try {
                        File jarTmp = new File(TMP_JAR_FOLDER_ABSOLUTE_PATH);
                        if(!jarTmp.exists() && !jarTmp.mkdirs()){
                            throw new Exception("Error on create temp file");
                        }
                        entry = new File(TMP_JAR_FOLDER_ABSOLUTE_PATH, jarEntryName);
                        if(entry.exists()){
                            return;
                        }
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
    }

}

