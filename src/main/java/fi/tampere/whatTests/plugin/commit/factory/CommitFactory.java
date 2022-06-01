package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.task.ProjectTaskListener;
import com.intellij.task.ProjectTaskManager;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.whatTests.ConfigWrapper;
import fi.tampere.whatTests.plugin.build.listener.MyCompilerListener;
import fi.tampere.whatTests.plugin.config.PluginConfigurationWrapper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Paths;


public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {


        //load and read the configuration (to see if the plugin si enabled or not)
        PluginConfigurationWrapper pluginConfigurationWrapper = new PluginConfigurationWrapper(panel.getProject());

        if (!pluginConfigurationWrapper.getConfig().isEnabled()) {
            //return the default CheckinHandler
            return CheckinHandler.DUMMY;

        }

        if (pluginConfigurationWrapper.getConfig().isEnabled()) {
            if(!ifClassesInTmp(panel.getProject())) {
                NotificationGroupManager.getInstance()
                        .getNotificationGroup("whatTests.plugin.notification")
                        .createNotification("There are no previous commit to analyze. Please proceed to the commit. WhatTests plugin will be available from the next commit.", NotificationType.WARNING)
                        .setTitle("WhatTests: no previous commit to analyze")
                        .notify(panel.getProject());
                //return the default CheckinHandler
                return new CheckInHandler(panel.getProject());
            }

        }


        //create a listener to handle the starting and the finishing of the build task
        MyCompilerListener compilerListener = new MyCompilerListener(panel.getProject());
        //register the listener on the message bus
        MessageBusConnection messageBusConnection = panel.getProject().getMessageBus().connect();
        messageBusConnection.subscribe(ProjectTaskListener.TOPIC, compilerListener);
        //build the project
        ProjectTaskManager.getInstance(panel.getProject()).buildAllModules();
        //return the CheckinHandler to use when the plugin si enabled
        return new InitializedCheckinHandler(panel.getProject(), compilerListener, pluginConfigurationWrapper.getConfig().getJarPath());


    }

    private boolean ifClassesInTmp(Project project) {
        //create the object to read the options of whatTests (jar)
        ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
        //read path of the tmp folder
        String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
        //read the path of the output folder
        File dest = new File(Paths.get(project.getBasePath(), tempFolder).toString());
        return dest.exists();
    }
}






