package fi.tampere.catto;

import java.util.List;

public class Configurator {
    List<String> outputPath;
    List<String> dependencies;

    public String getTempFolderPath() {
        return tempFolderPath;
    }

    public void setTempFolderPath(String tempFolderPath) {
        this.tempFolderPath = tempFolderPath;
    }

    String tempFolderPath;

    public Configurator(){

    }

    public Configurator(List<String> outputPath, List<String> dependencies, String tempFolderPath){
        this.outputPath = outputPath;
        this.dependencies = dependencies;
        this.tempFolderPath = tempFolderPath;


    }

    public List<String> getOutputPath() {
        return outputPath;
    }

    public void setOutputPath(List<String> outputPath) {
        this.outputPath = outputPath;
    }

    public List<String> getDependencies() {
        return dependencies;
    }

    public void setDependencies(List<String> dependencies) {
        this.dependencies = dependencies;
    }





}
