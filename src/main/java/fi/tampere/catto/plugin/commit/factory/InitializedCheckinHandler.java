package fi.tampere.catto.plugin.commit.factory;

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
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import fi.tampere.catto.plugin.build.listener.MyCompilerListener;
import fi.tampere.catto.plugin.notification.NotificationAndMessage;
import org.jetbrains.annotations.NotNull;
import javax.swing.*;
import java.nio.file.Paths;


public class InitializedCheckinHandler extends CheckInHandler {

    MyCompilerListener executionListener;
    String jarPath;

      public InitializedCheckinHandler(Project project, MyCompilerListener compilerListener, String jarPath) {
          super(project);
          this.executionListener = compilerListener;
          this.jarPath = jarPath;

      }

        @Override
        public CheckinHandler.ReturnResult beforeCheckin() {
            int value;

            if(!executionListener.isFinished()){
                NotificationAndMessage.notifyBuildNotCompletedYet();
                return CheckinHandler.ReturnResult.CANCEL;
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
                    NotificationAndMessage.notifyNotJava8Installed();
                } else {

                    String binJava8 = Paths.get(Java8InstallationPath, "bin", "java").toString();

                    //TODO: SPOSTARE LA GESTIONE E LA CREAZIONE DELLA CONSOLE IN console.CONSOLE
                    ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
                    ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CATTOPlugin");
                    Content content = toolWindow.getContentManager().findContent("Output");
                    if (content != null) {
                        toolWindow.getContentManager().removeContent(content, true);
                    }

                    Content newContent = toolWindow.getContentManager().getFactory().createContent(consoleView.getComponent(), "Output", false);
                    toolWindow.getContentManager().addContent(newContent);



                 //   ToolWindow toolWindow = ToolWindowManager.getInstance(project).getToolWindow("CATTOPlugin");
                 //   if (toolWindow == null) {
                        //toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(RegisterToolWindowTask.closable("CATTOPlugin", IconLoader.findIcon(getClass().getClassLoader().getResource("close.svg"))));
                       // toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(RegisterToolWindowTask.notClosable("CATTOPlugin"));

                 //   }
                 //   Content content = toolWindow.getContentManager().findContent("Output");
                 //   ConsoleView consoleView = TextConsoleBuilderFactory.getInstance().createBuilder(project).getConsole();
                 //   if (content != null) {
                  //      toolWindow.getContentManager().removeContent(content, true);

                  //  }




//                    ContentManager contentManager = toolWindow.getContentManager().getFactory().createContentManager(true, project);
 //                   content = contentManager.getFactory().createContent(consoleView.getComponent(), "Output", true);
 //                   content.setCloseable(true);
  //                  toolWindow.getContentManager().addContent(content);


                    toolWindow.activate(new Runnable() {
                        @Override
                        public void run() {

                        }
                    });


                    Integer exitValue = 0;


                    Task.WithResult<Integer, Exception> task1 = new Task.WithResult<Integer, Exception>(project, null, "CATTOPlugin", false) {
                        @Override
                        protected Integer compute(@NotNull ProgressIndicator indicator) throws Exception {
                            OSProcessHandler processHandler = null;
                            try {
                                processHandler = ProcessHandlerFactory.getInstance().createColoredProcessHandler(new GeneralCommandLine(binJava8, "-jar", jarPath , project.getBasePath()));
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
                    if (exitValue == 1) {
                        NotificationAndMessage.notifyCattoExecutionFailed();
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit CATTO terminated with error", JOptionPane.YES_NO_OPTION);
                    }else if(exitValue == 2){
                        NotificationAndMessage.notifyNoTestsFound();
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit No Test", JOptionPane.YES_NO_OPTION);
                    } else if(exitValue == 0) {
                        NotificationAndMessage.notifyNoTestFailed();
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit?", "Commit Test pass", JOptionPane.YES_NO_OPTION);
                    }else {
                        NotificationAndMessage.notifyTestFailure();
                        value = JOptionPane.showConfirmDialog(null, "Do you want commit anyway?", "Commit Test fails", JOptionPane.YES_NO_OPTION);
                    }
                    if (value == 0)
                        return super.beforeCheckin();
                    return ReturnResult.CLOSE_WINDOW;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return super.beforeCheckin();
        }

    };



