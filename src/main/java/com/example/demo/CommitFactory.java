package com.example.demo;

import com.intellij.execution.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;

import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;

import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import com.intellij.openapi.projectRoots.Sdk;


import static com.ibm.icu.impl.ClassLoaderUtil.getClassLoader;

public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        //TODO: create a task and wait for it to be sure that before the execution of the commit the build has been completed
        RunManager instance = RunManager.getInstance(panel.getProject());
        List<RunnerAndConfigurationSettings> allSettings = instance.getAllSettings();
        RunnerAndConfigurationSettings runnerAndConfigurationSettings = allSettings.get(0);
        ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder
                .createOrNull(DefaultRunExecutor.getRunExecutorInstance(), runnerAndConfigurationSettings);

        if (builder != null) {
            //ExecutionManager.getInstance(panel.getProject()).restartRunProfile(builder.build());
            builder.build();
            //builder.activeTarget().build();
            try {
                builder.buildAndExecute();
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }


        }

        final CheckinHandler checkinHandler = new CheckinHandler() {

            @Override
            public void checkinSuccessful(){
                /*

                ExecutionEnvironmentBuilder.create(panel.getProject(), executorToUse, configs.get(valore)).build();
                Module @NotNull [] modules = ModuleManager.getInstance(panel.getProject()).getModules();
                List<String> paths = new ArrayList<>();

                final List<String> libraryNames = new ArrayList<String>();
                for(Module m : modules){
                    paths.add(Objects.requireNonNull(Objects.requireNonNull(CompilerModuleExtension.getInstance(m)).getCompilerOutputUrl()).replace("file://", ""));
                    paths.add(Objects.requireNonNull(Objects.requireNonNull(CompilerModuleExtension.getInstance(m)).getCompilerOutputUrlForTests()).replace("file://", ""));;


                    ModuleRootManager.getInstance(m).orderEntries().forEachLibrary(library -> {
                        for (VirtualFile vf: library.getFiles(OrderRootType.SOURCES)){
                            libraryNames.add(vf.getPath().replace("!/", ""));
                        }
                        for (VirtualFile vf: library.getFiles(OrderRootType.CLASSES)){
                            libraryNames.add(vf.getPath().replace("!/", ""));
                        }

                        return true;
                    });

                 */

                //TODO: read the config file and copy the file from outpuPat to tmp folder
                ConfigWrapper configWrapper = new ConfigWrapper(panel.getProject().getBasePath());
                String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
                List<String> classPath = configWrapper.getCONFIG().getOutputPath();
                for(String cp : classPath ){
                    try {
                        File src = new File(cp);
                        File dest = new File(Paths.get(panel.getProject().getBasePath(), tempFolder).toString());
                        FileUtils.copyDirectory(src, dest);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public ReturnResult beforeCheckin() {
                int value;
                // final RunManager runManager = RunManager.getInstance(panel.getProject());


                // List<RunConfiguration> configs = runManager.getAllConfigurationsList();
                // value= JOptionPane.showOptionDialog(null, "Choose one configuration", "Select a configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, configs.toArray(), configs.get(0));
                // if (value != JOptionPane.CLOSED_OPTION) {

                //Executor executorToUse = DefaultRunExecutor.getRunExecutorInstance();

                //TODO: spostare codice da qui
                ConfigWrapper configWrapper = new ConfigWrapper(panel.getProject().getBasePath());
                String tempFolder = Paths.get(panel.getProject().getBasePath(), configWrapper.getCONFIG().getTempFolderPath()).toString();
                if(!new File(tempFolder).exists()){
                   List<String> classPath = configWrapper.getCONFIG().getOutputPath();
                    for(String cp : classPath ){
                        try {
                            File src = new File(cp);
                            File dest = new File(tempFolder);
                            FileUtils.copyDirectory(src, dest);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }

                try {
                    //ExecutionEnvironmentBuilder.create(panel.getProject(), executorToUse, configs.get(value)).build();





                    ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();
                    String jdkPath = "";

                    Sdk[] sq = jdkTable.getAllJdks();
                    String Java8InstallationPath = "";
                    for (Sdk k : sq) {
                        if (k.getName().equals("1.8")) {
                            if (k.getHomePath() != null)
                                Java8InstallationPath = k.getHomePath();
                        }
                    }

                    if (Java8InstallationPath.equals("")) {
                        Messages.showInfoMessage("whatTest could not find java 8 installation on yout system. Please install it and relunch the plugin", "WhatTests:JAVA V.1.8 not Installed");
                    } else {

                        Path tmp = Paths.get(Objects.requireNonNull(panel.getProject().getBasePath()), ".whatTests", "jarTmp");
                        String whatTestTmpPath = extractContentFromJar(Objects.requireNonNull(getClass().getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", ""), tmp.toString());

                        String binJava8 = Paths.get(Java8InstallationPath, "bin", "java").toString();

                        ToolWindow toolWindow = ToolWindowManager.getInstance(panel.getProject()).getToolWindow("whatTests");
                        if (toolWindow == null) {
                            //toolWindow = ToolWindowManager.getInstance(panel.getProject()).registerToolWindow(RegisterToolWindowTask.closable("whatTests", IconLoader.findIcon(getClass().getClassLoader().getResource("close.svg"))));
                            toolWindow = ToolWindowManager.getInstance(panel.getProject()).registerToolWindow(RegisterToolWindowTask.notClosable("whatTests"));

                        }
                        Content content = toolWindow.getContentManager().findContent("Output");
                        ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(panel.getProject()).getConsole();
                        if (content != null) {
                            toolWindow.getContentManager().removeContent(content, true);

                        }
                        ContentManager contentManager = toolWindow.getContentManager().getFactory().createContentManager(true, panel.getProject());
                        content = contentManager.getFactory().createContent(consoleView.getComponent(), "Output", true);
                        content.setCloseable(true);
                        toolWindow.getContentManager().addContent(content);


                        toolWindow.activate(new Runnable() {
                            @Override
                            public void run() {

                            }
                        });


                        Integer exitValue = 0;


                        Task.WithResult<Integer, Exception> task1 = new Task.WithResult<Integer, Exception>(panel.getProject(), null, "WhatTest", false) {
                            @Override
                            protected Integer compute(@NotNull ProgressIndicator indicator) throws Exception {
                                OSProcessHandler processHandler = null;
                                try {
                                    processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(new GeneralCommandLine(binJava8, "-jar", whatTestTmpPath, panel.getProject().getBasePath()));
                                } catch (ExecutionException e) {
                                    throw new RuntimeException(e);
                                }
                                ProcessTerminatedListener.attach(processHandler);
                                processHandler.startNotify();
                                consoleView.attachToProcess(processHandler);
                                Process p = processHandler.getProcess();
                                processHandler.waitFor();
                                return p.exitValue();
                            }
                        };
                        exitValue = ProgressManager.getInstance().run(task1);

/*
                            ProcessBuilder pb = new ProcessBuilder(binJava8, "-jar", whatTestTmpPath, panel.getProject().getBasePath());
                            Process p = pb.start();
                            p.waitFor();


                            BufferedReader reader =
                                new BufferedReader(new InputStreamReader(p.getInputStream()));
                        StringBuilder builder = new StringBuilder();
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            consoleView.print(line + "\n", ConsoleViewContentType.NORMAL_OUTPUT);
                            builder.append(line);
                            builder.append(System.getProperty("line.separator"));
                        }

                        String result = builder.toString();


                        reader =
                                new BufferedReader(new InputStreamReader(p.getErrorStream()));
                        builder = new StringBuilder();
                        line = null;
                        while ((line = reader.readLine()) != null) {
                            consoleView.print(line + "\n", ConsoleViewContentType.ERROR_OUTPUT);
                            builder.append(line);
                            builder.append(System.getProperty("line.separator"));
                        }
                        result = builder.toString();
                        */

                        if (exitValue != 0) {
                            Messages.showInfoMessage("Some test fails. Plese see the whaTest consolo for more information", "whatTests Test Failure");
                            value = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit Test fails", JOptionPane.YES_NO_OPTION);
                            if (value == 0)
                                return super.beforeCheckin();

                            return ReturnResult.CANCEL;
                        } else {
                            Messages.showInfoMessage("No test fails!", "whatTests Test Pass");
                            value = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit Test pass", JOptionPane.YES_NO_OPTION);
                            if (value == 0)
                                return super.beforeCheckin();

                            return ReturnResult.CANCEL;
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                //}

                return super.beforeCheckin();
            }


        };





        return checkinHandler;
    }


    public String extractContentFromJar(String uri, String dest) throws Exception {
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
                if (jarEntryName.equals("whatTests.jar")) {
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






