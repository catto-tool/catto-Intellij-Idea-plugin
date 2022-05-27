package fi.tampere.whatTests.plugin.config;


public class PluginConfiguration {
    private boolean enable = true;

    PluginConfiguration(){
    }

    public PluginConfiguration(boolean enable){
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    @Override
    public String toString() {
        return "enable: true";
    }
}
