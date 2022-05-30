package fi.tampere.whatTests.plugin.util;

import com.intellij.openapi.project.Project;
import com.intellij.task.ProjectTask;
import com.intellij.task.ProjectTaskManager;
import fi.tampere.whatTests.ConfigWrapper;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import fi.tampere.whatTests.plugin.config.PluginConfiguration;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class Util {
    private static final String WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH = ".whatTests" + File.separator + "jarTmp";
    public static final String WHATESTS_JAR_FILE_RELATIVE_PATH = Util.WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH + File.separator + "whatTests.jar";
    private static final String WHATESTS_JAR_FILE_ORIGINAL_PATH = Objects.requireNonNull(Util.class.getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", "");
    private static String  WHATESTS_JAR_TMP_FOLDER_ABSOLUTE_PATH;




    public static void initialize(Project project){
        WHATESTS_JAR_TMP_FOLDER_ABSOLUTE_PATH = project.getBasePath() + File.separator + WHATESTS_JAR_TMP_FOLDER_RELATIVE_PATH;
        try {
            extractContentFromJar();
            copyClassesInTmp(project);
            PluginConfigWrapper pluginConfigWrapper = new PluginConfigWrapper(project.getBasePath());
            pluginConfigWrapper.getConfig().setInitialized(true);
            pluginConfigWrapper.updateConfig();
        } catch (Exception e) {
            throw new RuntimeException(e);
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
                        File jarTmp = new File(WHATESTS_JAR_TMP_FOLDER_ABSOLUTE_PATH);
                        if(!jarTmp.exists() && !jarTmp.mkdirs()){
                            throw new Exception("Error on create temp file");
                        }
                        entry = new File(WHATESTS_JAR_TMP_FOLDER_ABSOLUTE_PATH, jarEntryName);
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
            } catch (IOException e) {

            }
        }
    }






}
