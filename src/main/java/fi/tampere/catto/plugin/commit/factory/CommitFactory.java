package fi.tampere.catto.plugin.commit.factory;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.task.ProjectTaskContext;
import com.intellij.task.ProjectTaskListener;
import com.intellij.task.ProjectTaskManager;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.catto.ConfigWrapper;
import fi.tampere.catto.plugin.build.listener.MyCompilerListener;
import fi.tampere.catto.plugin.config.PluginConfigurationWrapper;
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
                        .getNotificationGroup("CATTOPlugin.plugin.notification")
                        .createNotification("There are no previous commit to analyze. Please proceed to the commit. CATTOPlugin plugin will be available from the next commit.", NotificationType.WARNING)
                        .setTitle("CATTOPlugin: no previous commit to analyze")
                        .notify(panel.getProject());
                //return the default CheckinHandler
                return new CheckInHandler(panel.getProject());
            }

        }


        //create a listener to handle the starting and the finishing of the build task
        MyCompilerListener compilerListener = MyCompilerListener.getInstance(panel.getProject(), true);
        //register the listener on the message bus
       /* MessageBusConnection messageBusConnection = panel.getProject().getMessageBus().connect();
        messageBusConnection.subscribe(ProjectTaskListener.TOPIC, compilerListener);*/

        //build the project
        ProjectTaskManager.getInstance(panel.getProject()).buildAllModules();
        //return the CheckinHandler to use when the plugin si enabled
        return new InitializedCheckinHandler(panel.getProject(), compilerListener, pluginConfigurationWrapper.getConfig().getJarPath());


    }

    private boolean ifClassesInTmp(Project project) {
        //create the object to read the options of CATTOPlugin (jar)
        ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
        //read path of the tmp folder
        String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
        //read the path of the output folder
        File dest = new File(Paths.get(project.getBasePath(), tempFolder).toString());
        return dest.exists();
    }
}






