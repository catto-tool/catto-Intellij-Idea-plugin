package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;
import com.intellij.task.ProjectTaskListener;
import com.intellij.task.ProjectTaskManager;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.whatTests.plugin.build.listener.MyCompilerListener;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import fi.tampere.whatTests.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;


public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {


        //load and read the configuration (to see if the plugin si enabled or not)
        PluginConfigWrapper pluginConfigWrapper = new PluginConfigWrapper(Paths.get(panel.getProject().getBasePath()).toString());

        if (pluginConfigWrapper.getConfig().isEnabled()) {
            if(!pluginConfigWrapper.getConfig().isInitialized())               {
                Util.initialize(panel.getProject());
            }

            //create a listener to handle the starting and the finishing of the build task
            MyCompilerListener compilerListener = new MyCompilerListener(panel.getProject());
            //register the listener on the message bus
            MessageBusConnection messageBusConnection = panel.getProject().getMessageBus().connect();
            messageBusConnection.subscribe(ProjectTaskListener.TOPIC, compilerListener);
            //build the project
            ProjectTaskManager.getInstance(panel.getProject()).buildAllModules();
            //return the CheckinHandler to use when the plugin si enabled
            return new EnabledCheckinHandler(panel.getProject(), compilerListener);
            }
        //return a void CheckinHandler to use when the plugin is disabled                     
        return new DisabledCheckinHandler();

    }
}






