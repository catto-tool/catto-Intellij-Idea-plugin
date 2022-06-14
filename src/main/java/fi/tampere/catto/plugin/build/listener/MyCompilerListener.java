package fi.tampere.catto.plugin.build.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
import com.intellij.task.*;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.catto.plugin.notification.NotificationAndMessage;
import org.jetbrains.annotations.NotNull;

public class MyCompilerListener implements ProjectTaskListener {

    boolean finished = false;

    boolean isCommit;
    Project project;
    private static MyCompilerListener myCompilerListener = null;
    private static MessageBusConnection messageBusConnection = null;

    private MyCompilerListener(Project project, boolean isCommit){
        this.project = project;
        this.isCommit = isCommit;


    }

    public static MyCompilerListener getInstance(Project project, boolean isCommit){
        if(myCompilerListener == null)
            myCompilerListener = new MyCompilerListener(project, isCommit);

        myCompilerListener.setCommit(isCommit);
        myCompilerListener.finished = false;

        if(messageBusConnection == null){
            messageBusConnection = project.getMessageBus().connect();
            messageBusConnection.subscribe(ProjectTaskListener.TOPIC, myCompilerListener);
        }

        return  myCompilerListener;
    }

    private void setCommit(boolean commit) {
        isCommit = commit;
    }

    @Override
   public void started(@NotNull ProjectTaskContext context) {
        if(myCompilerListener.isCommit) {
            ProjectTaskListener.super.started(context);
           NotificationAndMessage.notifyBuildStarted(project);
        }
   }

    @Override
    public void finished(@NotNull ProjectTaskManager.Result result) {
        if(myCompilerListener.isCommit) {
            ProjectTaskListener.super.finished(result);
            myCompilerListener.finished = true;
            NotificationAndMessage.notifyBuildCompleted(project);
            myCompilerListener.setCommit(false);
        }
    }

    public boolean isFinished() {
        return finished;
    }
}
