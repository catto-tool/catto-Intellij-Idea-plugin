package fi.tampere.catto.plugin.config;


public class PluginConfiguration {

    public String getJarPath() {
        return jarPath;
    }

    private  String jarPath;

    private  boolean enabled = true;

    private boolean initialized = false;

    private String projectPath;


    private PluginConfiguration(){

    }

    public PluginConfiguration(boolean enabled, boolean initialized, String projectPath, String jarPath){
        this.enabled = enabled;
        this.initialized = initialized;
        this.projectPath = projectPath;
        this.jarPath = jarPath;
    }

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public String toString() {
        return "enabled: " + enabled + System.lineSeparator()  + "initialized: " + initialized + System.lineSeparator() + "basePath: " + projectPath + System.lineSeparator() + "jarPath: " + jarPath;
        //return "enabled: " + enabled + System.lineSeparator()  + "initialized: " + initialized;

    }


}
