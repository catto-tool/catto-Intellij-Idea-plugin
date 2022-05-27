package fi.tampere.whatTests.plugin.build.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import org.jetbrains.annotations.NotNull;

public class MyExecutionListener implements ExecutionListener {

    boolean finished = false;

    @Override
    public void processStarted(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler) {
        ExecutionListener.super.processStarting(executorId, env, handler);
        showMyMessage( "Wait before the build is complete before commit", "whatTests: Building" ,  env.getProject());

    }

    @Override
    public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
        ExecutionListener.super.processTerminated(executorId, env, handler, exitCode);
        finished = true;
        showMyMessage( "Now it is possible to proceed with the commit", "whatTests: Build Terminated" , env.getProject());

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
