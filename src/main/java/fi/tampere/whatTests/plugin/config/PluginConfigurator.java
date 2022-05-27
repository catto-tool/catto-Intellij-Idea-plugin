package fi.tampere.whatTests.plugin.config;


public class PluginConfigurator {
    private boolean enable;

    PluginConfigurator(){
    }

    public PluginConfigurator(boolean enable){
        this.enable = enable;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }


}
