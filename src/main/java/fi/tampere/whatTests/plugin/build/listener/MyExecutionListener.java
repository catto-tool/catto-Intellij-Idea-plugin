package fi.tampere.whatTests.plugin.build.listener;

import com.intellij.execution.ExecutionListener;
import com.intellij.execution.process.ProcessHandler;
import com.intellij.execution.runners.ExecutionEnvironment;
import org.jetbrains.annotations.NotNull;

public class MyExecutionListener implements ExecutionListener {

    boolean finished = false;



    @Override
    public void processTerminated(@NotNull String executorId, @NotNull ExecutionEnvironment env, @NotNull ProcessHandler handler, int exitCode) {
        ExecutionListener.super.processTerminated(executorId, env, handler, exitCode);
        finished = true;
    }

    public boolean isFinished() {
        return finished;
    }
}
