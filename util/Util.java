package fi.tampere.whatTests.plugin.util;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.task.ProjectTaskManager;
import fi.tampere.whatTests.ConfigWrapper;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import fi.tampere.whatTests.plugin.config.PluginConfiguration;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Util {
    private static final String WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH = ".whatTests" + File.separator + "jarTmp";
    private static final String WHATESTS_JAR_FILE_RELATIVE_PATH = Util.WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH + File.separator + "whatTests.jar";
    private static final String WHATESTS_JAR_FILE_ORIGINAL_PATH = Objects.requireNonNull(Util.class.getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", "");

    private static final String CONFIG_RELATIVE_PATH = ".whatTests" + File.separator + "pluginConfiguration.yaml";


    private static final String RELATIVE_PATH_CONFIG_FOLDER =".whatTests";

    private static String ABSOLUTE_PATH_CONFIG_FOLDER;


    public static String PROJECT_PATH;
    public static String CONFIG_FILE_PATH;
    public static String  WHATESTS_JAR_TMP_FOLDER_PATH;
    public static String WHATESTS_JAR_PATH;



    public static void initialize(Project project){
        WHATESTS_JAR_TMP_FOLDER_PATH = project.getBasePath() + File.separator + WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH;
        WHATESTS_JAR_PATH = project.getBasePath() + File.separator + WHATESTS_JAR_FILE_RELATIVE_PATH;
        CONFIG_FILE_PATH = project.getBasePath() + File.separator + CONFIG_RELATIVE_PATH;
        PROJECT_PATH = project.getBasePath();
        ABSOLUTE_PATH_CONFIG_FOLDER = project.getBasePath() + File.separator + RELATIVE_PATH_CONFIG_FOLDER;
        try {

            NotificationGroupManager.getInstance()
                        .getNotificationGroup("whatTests.plugin.notification")
                        .createNotification("", NotificationType.INFORMATION)
                        .setTitle("whatTests: Initializing")
                        .notify(project);

            extractContentFromJar();
            createConfigurationFile();
            copyClassesInTmp(project);
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            pluginConfiguration.setInitialized(true);
            pluginConfiguration.setEnabled(true);
            pluginConfiguration.setBasePath(Util.PROJECT_PATH);
            PluginConfigWrapper.updateConfig(pluginConfiguration);
            NotificationGroupManager.getInstance()
                    .getNotificationGroup("whatTests.plugin.notification")
                    .createNotification("WhatTests has bee initialized", NotificationType.INFORMATION)
                    .setTitle("WhatTests: initialized successfully")
                    .notify(project);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private static void createConfigurationFile() {
        try {

            File f = new File(CONFIG_FILE_PATH);

            if (!f.exists()) {
                File folder = new File(ABSOLUTE_PATH_CONFIG_FOLDER);
                folder.mkdirs();
            //    f.createNewFile();
                PluginConfiguration pluginConfiguration = new PluginConfiguration(true, false, Util.PROJECT_PATH);
                PluginConfigWrapper.updateConfig(pluginConfiguration);


            }
        }catch (IOException e) {

        }
    }




    private static String extractContentFromJar() throws Exception {
        URL location = null;
        try {
            location = new URL(WHATESTS_JAR_FILE_ORIGINAL_PATH);
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
                        File jarTmp = new File(WHATESTS_JAR_TMP_FOLDER_PATH);
                        if(!jarTmp.exists() && !jarTmp.mkdirs()){
                            throw new Exception("Error on create temp file");
                        }
                        entry = new File(WHATESTS_JAR_TMP_FOLDER_PATH, jarEntryName);
                        if(entry.exists()){
                            return "";
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
        return resultPath;
    }

    public static void copyClassesInTmp(Project project){
        //create the object to read the options of whatTests (jar)
        ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
        //read path of the tmp folder
        String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
        //read the path of the output folder
        List<String> classPath = configWrapper.getCONFIG().getOutputPath();
        //copy all files in the output path in the tmp folder
        for (String cp : classPath) {
            try {
                File src = new File(cp);
                File dest = new File(Paths.get(project.getBasePath(), tempFolder).toString());
                if(!src.exists()){
                    ProjectTaskManager.getInstance(project).buildAllModules().onSuccess(result -> copyClassesInTmp(project));
                }else {
                    FileUtils.copyDirectory(src, dest);
                }
            } catch (IOException ignored) {

            }
        }
    }






}
