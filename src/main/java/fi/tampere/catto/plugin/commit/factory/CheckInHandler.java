package fi.tampere.catto.plugin.commit.factory;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vcs.VcsException;
import com.intellij.openapi.vcs.checkin.CheckinHandler;
import com.intellij.task.ProjectTaskListener;
import com.intellij.task.ProjectTaskManager;
import com.intellij.util.messages.MessageBusConnection;
import fi.tampere.catto.ConfigWrapper;
import fi.tampere.catto.plugin.build.listener.MyCompilerListener;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class CheckInHandler extends CheckinHandler {

    Project project;
    CheckInHandler(Project project){
        this.project = project;
    }

    @Override
    public void checkinSuccessful() {
        this.copyClassesInTmp();
    }




    void copyClassesInTmp(){
        //create the object to read the options of whatTests (jar)
        ConfigWrapper configWrapper = new ConfigWrapper(project.getBasePath());
        //read path of the tmp folder
        String tempFolder = configWrapper.getCONFIG().getTempFolderPath();
        //read the path of the output folder
        List<String> classPath = configWrapper.getCONFIG().getOutputPath();
        //copy all files in the output path in the tmp folder
        for (String cp : classPath) {
            try {
                File src = new File(cp);
                File dest = new File(Paths.get(project.getBasePath(), tempFolder).toString());
                if(!dest.exists()){
                    dest.mkdirs();
                }
                if(!src.exists()){
                    ProjectTaskManager.getInstance(project).buildAllModules().onSuccess(result -> copyClassesInTmp());
                }else {
                    FileUtils.copyDirectory(src, dest);
                }
            } catch (IOException ignored) {

            }
        }
    }


}
