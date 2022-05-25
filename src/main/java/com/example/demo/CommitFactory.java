package com.example.demo;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.RunManager;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.projectRoots.*;
import com.intellij.openapi.roots.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import com.intellij.xdebugger.DefaultDebugProcessHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;

import org.jetbrains.annotations.NotNull;


import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.List;
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
                int value= JOptionPane.showOptionDialog(null, "Choose one configuration", "Select a configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, configs.toArray(), configs.get(0));
                if (value != JOptionPane.CLOSED_OPTION) {

                    Executor executorToUse = DefaultRunExecutor.getRunExecutorInstance();

                    try {
                        ExecutionEnvironmentBuilder.create(panel.getProject(), executorToUse, configs.get(value)).build();

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
                        }else{

                        Path tmp = Paths.get(Objects.requireNonNull(panel.getProject().getBasePath()), ".whatTests", "jarTmp");
                        String whatTestTmpPath = extractContentFromJar(Objects.requireNonNull(getClass().getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", ""), tmp.toString());

                        String binJava8 = Paths.get(Java8InstallationPath, "bin", "java").toString();

                            ToolWindow toolWindow =  ToolWindowManager.getInstance(panel.getProject()).getToolWindow("whatTests");
                            if (toolWindow == null) {
                                //toolWindow = ToolWindowManager.getInstance(panel.getProject()).registerToolWindow(RegisterToolWindowTask.closable("whatTests", IconLoader.findIcon(getClass().getClassLoader().getResource("close.svg"))));
                                toolWindow = ToolWindowManager.getInstance(panel.getProject()).registerToolWindow(RegisterToolWindowTask.notClosable("whatTests"));

                            }
                            Content content = toolWindow.getContentManager().findContent("Output");
                            ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(panel.getProject()).getConsole();
                            if (content != null){
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




                            OSProcessHandler processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(new GeneralCommandLine(binJava8, "-jar", whatTestTmpPath, panel.getProject().getBasePath()));
                            ProcessTerminatedListener.attach(processHandler);
                            processHandler.startNotify();
                            consoleView.attachToProcess(processHandler);
                            Process p = processHandler.getProcess();
                            processHandler.waitFor();

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

                        if(p.exitValue() != 0) {
                            Messages.showInfoMessage("Some test fails. Plese see the whaTest consolo for more information", "whatTests Test Failure");
                            value = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit Test fails", JOptionPane.YES_NO_OPTION);
                            if (value == 0)
                                return super.beforeCheckin();

                            return ReturnResult.CANCEL;
                        }else{
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
                }

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






