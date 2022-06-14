package fi.tampere.catto.plugin.notification;

import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

public class NotificationAndMessage {

    public static void notifyNotFileInTmp(Project project) {
        NotificationGroup notificationGroup = NotificationGroupManager.getInstance()
                .getNotificationGroup("CATTOPlugin.plugin.notification");
        if (notificationGroup != null) {
            notificationGroup.createNotification("There are no previous commit to analyze. Please proceed to the commit. CATTOPlugin plugin will be available from the next commit.", NotificationType.WARNING)
                    .setTitle("CATTOPlugin: no previous commit to analyze")
                    .notify(project);
        }
    }

    public static void notifyBuildCompleted(Project project){
        showMyMessage("Now it is possible to proceed with the commit", "CATTOPlugin: Build Terminated", project);
    }

    public static void notifyBuildStarted(Project project){
        showMyMessage("Wait before the build is complete before commit", "CATTOPlugin: Building", project);
    }



    public static void notifyNotJava8Installed() {
        Messages.showInfoMessage("CATTOPlugin could not find java 8 installation on your system. please install it and relaunch the plugin", "CATTOPlugin:JAVA V.1.8 not Installed");
    }

    public static void notifyBuildNotCompletedYet(){
        Messages.showInfoMessage("Please wait before the build has been completed", "CATTOPlugin: Build not Yet Completed");
    }

    public static void notifyCattoExecutionFailed() {
        Messages.showErrorDialog("CATTOPlugin terminated with errors. See the CATTOPlugin for more information", "CATTOPlugin: Error");
    }

    public static void notifyNoTestsFound() {
        Messages.showInfoMessage("No test to execute found.", "CATTOPlugin No Test to Execute");
    }

    public static void notifyNoTestFailed() {
        Messages.showInfoMessage("No test fails!", "CATTOPlugin Test Pass");
    }

    public static void notifyTestFailure() {
        Messages.showWarningDialog("Some test fails. Please see the CATTOPlugin console for more information", "CATTOPlugin Test Failure");
    }



    private static void showMyMessage(String content, String title, Project project) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("CATTOPlugin.plugin.notification")
                .createNotification(content, NotificationType.INFORMATION)
                .setTitle(title)
                .notify(project);

    }




}
