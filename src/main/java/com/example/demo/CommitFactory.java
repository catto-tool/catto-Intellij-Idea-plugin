package com.example.demo;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

import com.intellij.openapi.projectRoots.Sdk;


import static com.ibm.icu.impl.ClassLoaderUtil.getClassLoader;

public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        final CheckinHandler checkinHandler = new CheckinHandler() {
            @Override
            public ReturnResult beforeCheckin() {
                final RunManager runManager = RunManager.getInstance(panel.getProject());


                List<RunConfiguration> configs = runManager.getAllConfigurationsList();
                int valore = JOptionPane.showOptionDialog(null, "Choose one configuration", "Select a configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, configs.toArray(), configs.get(0));
                if (valore != JOptionPane.CLOSED_OPTION) {


                    Executor executorToUse = DefaultRunExecutor.getRunExecutorInstance();

                    try {
                        ExecutionEnvironmentBuilder.create(panel.getProject(), executorToUse, configs.get(valore)).build();
                        Module @NotNull [] modules = ModuleManager.getInstance(panel.getProject()).getModules();
                        List<String> paths = new ArrayList<>();

                        final List<String> libraryNames = new ArrayList<String>();
                        for (Module m : modules) {
                            paths.add(Objects.requireNonNull(Objects.requireNonNull(CompilerModuleExtension.getInstance(m)).getCompilerOutputUrl()).replace("file://", ""));
                            paths.add(Objects.requireNonNull(Objects.requireNonNull(CompilerModuleExtension.getInstance(m)).getCompilerOutputUrlForTests()).replace("file://", ""));
                            ;


                            ModuleRootManager.getInstance(m).orderEntries().forEachLibrary(library -> {
                                for (VirtualFile vf : library.getFiles(OrderRootType.SOURCES)) {
                                    libraryNames.add(vf.getPath().replace("!/", ""));
                                }
                                for (VirtualFile vf : library.getFiles(OrderRootType.CLASSES)) {
                                    libraryNames.add(vf.getPath().replace("!/", ""));
                                }

                                return true;
                            });
                            Messages.showInfoMessage(StringUtil.join(libraryNames, "\n"), "Libraries in Module");


                        }
                        ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
                        String jdkPath = "";

                        Sdk[] sq = jdkTable.getAllJdks();

                        for (Sdk k : sq) {
                            if (k.getSdkType().equals(jdkTable.getDefaultSdkType())) {
                                jdkPath = k.getHomePath();
                                Messages.showInfoMessage(jdkPath, "JDK PATH");

                            }

                        }
                        jdkPath = "/Library/Java/JavaVirtualMachines/jdk1.8.0_202.jdk/Contents/Home/jre/lib";
                        assert jdkPath != null;

                        List<File> file = (List<File>) FileUtils.listFiles(new File(jdkPath), TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
                        for (File f : file) {
                            libraryNames.addAll(listf(f.getAbsolutePath()));
                        }

                        //libraryNames.add("/Library/Java/JavaVirtualMachines/liberica-1.8.0_332/jre/lib/jce.jar");
                        //libraryNames.add("/Library/Java/JavaVirtualMachines/liberica-1.8.0_332/jre/lib/rt.jar");



                        /*
                        Project p = new PreviousProject(libraryNames.toArray(new String[0]), paths.toArray(new String[0]));
                        Project p1 = new NewProject(libraryNames.toArray(new String[0]), Paths.get(Objects.requireNonNull(panel.getProject().getBasePath()), "pippo" ).toString());
                        FromTheBottom rta = new FromTheBottom(p,p1);
                        Set<Test> selectedTest = rta.selectTest();;
                        for (Test t : selectedTest ){
                            Runner.run(t, libraryNames.toArray(new String[0]), paths);
                        }


*/
                        Path tmp = Paths.get(Objects.requireNonNull(panel.getProject().getBasePath()), ".whatTests", "jarTmp");
                        extractContentFromJar(Objects.requireNonNull(getClass().getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", ""), tmp.toString());


                        ProcessBuilder pb = new ProcessBuilder("java", "-jar", Paths.get(Objects.requireNonNull(panel.getProject().getBasePath()), ".whatTests", "jarTmp", "whatTests.jar").toString(), panel.getProject().getBasePath());
                        Process p = pb.start();
                        p.waitFor();

                        BufferedReader reader =
                                new BufferedReader(new InputStreamReader(p.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                            builder.append(System.getProperty("line.separator"));
                        }
                        String result = builder.toString();
                        System.out.println(result);

                        reader =
                                new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        builder = new StringBuilder();
                        line = null;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                            builder.append(System.getProperty("line.separator"));
                        }
                        result = builder.toString();
                        System.out.println(result);


                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }


                }

                valore = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit confirmation", JOptionPane.YES_NO_OPTION);
                if (valore == 0)
                    return super.beforeCheckin();

                return ReturnResult.CANCEL;
            }
        };
        return checkinHandler;
    }

    public List<String> listf(String directoryName) {
        File directory = new File(directoryName);
        List<String> files = new ArrayList<>();

        // Get all files from a directory.
        if (directory.isFile()) {
            if (directory.getName().endsWith(".jar"))
                files.add(directory.getAbsolutePath());
        } else {
            File[] fList = directory.listFiles();
            if (fList != null)
                for (File file : fList) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".jar"))
                            files.add(file.getAbsolutePath());
                    } else if (file.isDirectory()) {
                        files.addAll(listf(file.getAbsolutePath()));
                    }
                }

        }
        return files;
    }


    public void extractContentFromJar(String uri, String dest) throws Exception {
        URL location = null;
        try {
            location = new URL(uri);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        assert location != null;
        String jarPath = location.getPath().replace("file:", "").replace("!", "");

        try {
            JarInputStream jar = new JarInputStream(new FileInputStream(jarPath));
            JarEntry jarEntry = null;

            while ((jarEntry = jar.getNextJarEntry()) != null) {
                String jarEntryName = jarEntry.getName();
                File entry = null;
                if (jarEntryName.equals("whatTests.jar")) {
                    try {
                        entry = new File(dest, jarEntryName);

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






