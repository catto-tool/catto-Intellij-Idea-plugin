package fi.tampere.whatTests.plugin.build.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.task.*;
import org.jetbrains.annotations.NotNull;

public class MyCompilerListener implements ProjectTaskListener {

    boolean finished = false;
    Project project;

    public MyCompilerListener(Project project){
        this.project = project;

    }

   public void started(@NotNull ProjectTaskContext context) {
        ProjectTaskListener.super.started(context);
        showMyMessage( "Wait before the build is complete before commit", "whatTests: Building" , project);



   }

    @Override
    public void finished(@NotNull ProjectTaskManager.Result result) {
       ProjectTaskListener.super.finished(result);
       finished = true;
       showMyMessage( "Now it is possible to proceed with the commit", "whatTests: Build Terminated" ,project);




    }


    public boolean isFinished() {
        return finished;
    }


    void showMyMessage(String content, String title, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("whatTests.plugin.notification")
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(title)
                .notify(project);

    }
}
