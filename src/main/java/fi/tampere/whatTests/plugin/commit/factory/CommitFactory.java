package fi.tampere.whatTests.plugin.commit.factory;

import com.intellij.execution.*;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironmentBuilder;

import com.intellij.execution.target.java.JavaTargetParameter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.roots.CompilerProjectExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.CheckinProjectPanel;
import com.intellij.openapi.vcs.changes.CommitContext;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.openapi.vcs.checkin.CheckinHandlerFactory;


import com.intellij.task.*;
import com.intellij.testFramework.CompilerTester;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.whatTests.plugin.build.listener.MyCompilerListener;
import fi.tampere.whatTests.plugin.config.PluginConfigWrapper;
import org.apache.maven.model.Build;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.jps.*;
import org.jetbrains.jps.model.java.impl.compiler.JpsJavaCompilerConfigurationImpl;
import org.jetbrains.jps.util.JpsPathUtil;


public class CommitFactory extends CheckinHandlerFactory {

    @NotNull
    @Override
    public CheckinHandler createHandler(@NotNull CheckinProjectPanel panel, @NotNull CommitContext commitContext) {

        PluginConfigWrapper pluginConfigWrapper = new PluginConfigWrapper(Paths.get(panel.getProject().getBasePath()).toString());

        if (pluginConfigWrapper.getConfig().isEnable()) {
            MyCompilerListener compilerListener = new MyCompilerListener(panel.getProject());

            if (pluginConfigWrapper.getConfig().isEnable()) {
                MessageBusConnection messageBusConnection = panel.getProject().getMessageBus().connect();
                messageBusConnection.subscribe(ProjectTaskListener.TOPIC, compilerListener);
                ProjectTaskManager.getInstance(panel.getProject()).buildAllModules();
            }

            return new EnabledCheckinHandler(panel.getProject(), compilerListener);

        }

        return new DisabledCheckinHandler();

    }
}






