package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleView;
import com.intellij.internal.statistic.collectors.fus.fileTypes.FileTypeUsageCounterCollector;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.ProjectJdkTable;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.wm.RegisterToolWindowTask;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentManager;
import fi.tampere.whatTests.ConfigWrapper;
import fi.tampere.whatTests.plugin.build.listener.MyExecutionListener;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import fi.tampere.whatTests.plugin.util.Util;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class EnabledCheckinHandler extends CheckinHandler {
      Project project;
     MyExecutionListener executionListener;
    
      public EnabledCheckinHandler(Project project, MyExecutionListener executionListener) {
          this.project = project;
          this.executionListener = executionListener;

      }

        @Override
        public void checkinSuccessful() {

           

                //TODO: read the config file and copy the file from outpuPat to tmp folder
                ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
                String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
                List<String> classPath = configWrapper.getCONFIG().getOutputPath();
                for (String cp : classPath) {
                    try {
                        File src = new File(cp);
                        File dest = new File(Paths.get(project.getBasePath(), tempFolder).toString());
                        FileUtils.copyDirectory(src, dest);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            
        }

        @Override
        public CheckinHandler.ReturnResult beforeCheckin() {
           


            final int[] value = new int[1];

            // final RunManager runManager = RunManager.getInstance(project);


            // List<RunConfiguration> configs = runManager.getAllConfigurationsList();
            // value= JOptionPane.showOptionDialog(null, "Choose one configuration", "Select a configuration", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, configs.toArray(), configs.get(0));
            // if (value != JOptionPane.CLOSED_OPTION) {

            //Executor executorToUse = DefaultRunExecutor.getRunExecutorInstance();

            if(!executionListener.isFinished()){
                Messages.showInfoMessage("Please wait before the build has been completed", "WhatTests: Build not Yet Completed");
                return CheckinHandler.ReturnResult.CANCEL;
            }

            //TODO: spostare codice da qui
            ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
            String tempFolder = Paths.get(project.getBasePath(), configWrapper.getCONFIG().getTempFolderPath()).toString();
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
                //ExecutionEnvironmentBuilder.create(project, executorToUse, configs.get(value)).build();





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

                    Path tmp = Paths.get(Objects.requireNonNull(project.getBasePath()), ".whatTests", "jarTmp");
                    String whatTestTmpPath = Util.extractContentFromJar(Objects.requireNonNull(getClass().getClassLoader().getResource("whatTests.jar")).toString().replace("whatTests.jar", ""), tmp.toString());

                    String binJava8 = Paths.get(Java8InstallationPath, "bin", "java").toString();

                    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("whatTests");
                    if (toolWindow == null) {
                        //toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(RegisterToolWindowTask.closable("whatTests", IconLoader.findIcon(getClass().getClassLoader().getResource("close.svg"))));
                        toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(RegisterToolWindowTask.notClosable("whatTests"));

                    }
                    Content content = toolWindow.getContentManager().findContent("Output");
                    ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
                    if (content != null) {
                        toolWindow.getContentManager().removeContent(content, true);

                    }
                    ContentManager contentManager = toolWindow.getContentManager().getFactory().createContentManager(true, project);
                    content = contentManager.getFactory().createContent(consoleView.getComponent(), "Output", true);
                    content.setCloseable(true);
                    toolWindow.getContentManager().addContent(content);


                    toolWindow.activate(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });


                    Integer exitValue = 0;


                    Task.WithResult<Integer, Exception> task1 = new Task.WithResult<Integer, Exception>(project, null, "WhatTest", false) {
                        @Override
                        protected Integer compute(@NotNull ProgressIndicator indicator) throws Exception {
                            OSProcessHandler processHandler = null;
                            try {
                                processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(new GeneralCommandLine(binJava8, "-jar", whatTestTmpPath, project.getBasePath()));
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
                            ProcessBuilder pb = new ProcessBuilder(binJava8, "-jar", whatTestTmpPath, project.getBasePath());
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
                        value[0] = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit Test fails", JOptionPane.YES_NO_OPTION);
                        if (value[0] == 0)
                            return super.beforeCheckin();

                        return CheckinHandler.ReturnResult.CANCEL;
                    } else {
                        Messages.showInfoMessage("No test fails!", "whatTests Test Pass");
                        value[0] = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit Test pass", JOptionPane.YES_NO_OPTION);
                        if (value[0] == 0)
                            return super.beforeCheckin();

                        return CheckinHandler.ReturnResult.CANCEL;
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            //}

            return super.beforeCheckin();
        }


    };



