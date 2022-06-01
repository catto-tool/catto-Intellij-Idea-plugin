package fi.tampere.whatTests.plugin;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import fi.tampere.whatTests.plugin.config.PluginConfigurationWrapper;
import org.jetbrains.annotations.NotNull;

public class PluginStartupActivity implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {

        ProgressManager.getInstance().run(new Task.Backgroundable(project, "WhatTests initializing") {
            public void run(ProgressIndicator indicator) {
                 new PluginConfigurationWrapper(project);
            }
        });

    }
}
