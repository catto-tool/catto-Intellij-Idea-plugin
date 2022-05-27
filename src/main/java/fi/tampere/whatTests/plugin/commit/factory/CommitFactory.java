package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;

import com.intellij.execution.target.java.JavaTargetParameter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;


import com.intellij.task.BuildTask;
import com.intellij.testFramework.CompilerTester;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.whatTests.plugin.build.listener.MyExecutionListener;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import org.apache.maven.model.Build;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.List;
import org.jetbrains.jps.*;


public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        PluginConfigWrapper pluginConfigWrapper = new PluginConfigWrapper(Paths.get(panel.getProject().getBasePath()).toString());
        JComponent p = panel.getComponent();
        if (pluginConfigWrapper.getConfig().isEnable()) {
            RunManager instance = RunManager.getInstance(panel.getProject());
            List<RunnerAndConfigurationSettings> allSettings = instance.getAllSettings();
            RunnerAndConfigurationSettings runnerAndConfigurationSettings = allSettings.get(0);
            ExecutionEnvironmentBuilder builder = ExecutionEnvironmentBuilder
                    .createOrNull(DefaultRunExecutor.getRunExecutorInstance(), runnerAndConfigurationSettings);
            MyExecutionListener executionListener = new MyExecutionListener();


            if (pluginConfigWrapper.getConfig().isEnable()) {
                MessageBusConnection messageBusConnection = panel.getProject().getMessageBus().connect();


                if (builder != null) {
                       ExecutionManager.getInstance(panel.getProject()).restartRunProfile(builder.build());
                       messageBusConnection.subscribe(ExecutionManager.EXECUTION_TOPIC, executionListener);



                }

            }

            return new EnabledCheckinHandler(panel.getProject(), executionListener);

        }

        return new DisabledCheckinHandler();

    }
}






