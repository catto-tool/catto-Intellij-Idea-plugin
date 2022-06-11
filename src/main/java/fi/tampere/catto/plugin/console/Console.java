package fi.tampere.catto.plugin.console;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import org.jetbrains.annotations.NotNull;

public class Console implements ToolWindowFactory {
    //TODO: CAPIRE COME SPOSTARE IL CODICE PER LA GESTIONE DELLA CONSOLE DA InitializedCheckinHandler A QUI
    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {

    }
}
