package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.filters.TextConsoleBuilderFactory;
import com.intellij.execution.process.OSProcessHandler;
import com.intellij.execution.process.ProcessHandlerFactory;
import com.intellij.execution.process.ProcessTerminatedListener;
import com.intellij.execution.ui.ConsoleView;
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
import fi.tampere.whatTests.plugin.build.listener.MyCompilerListener;
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
     MyCompilerListener executionListener;
    
      public EnabledCheckinHandler(Project project, MyCompilerListener compilerListener) {
          this.project = project;
          this.executionListener = compilerListener;

      }

        @Override
        public void checkinSuccessful() {
          Util.copyClassesInTmp(project);


            
        }

        @Override
        public CheckinHandler.ReturnResult beforeCheckin() {
           


            int value;
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

                ProjectJdkTable jdkTable = ProjectJdkTable.getInstance();

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

                    //TODO: gestire meglio i path
                    String whatTestTmpPath = Paths.get(project.getBasePath(), Util.WHATESTS_JAR_FILE_RELATIVE_PATH).toString();

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

                    if (exitValue != 0) {
                        Messages.showInfoMessage("Some test fails. Plese see the whaTest consolo for more information", "whatTests Test Failure");
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit Test fails", JOptionPane.YES_NO_OPTION);

                    } else {
                        Messages.showInfoMessage("No test fails!", "whatTests Test Pass");
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit Test pass", JOptionPane.YES_NO_OPTION);

                    }
                    if (value == 0)
                        return super.beforeCheckin();
                    return ReturnResult.CANCEL;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return super.beforeCheckin();
        }


    };



