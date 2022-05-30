package fi.tampere.whatTests.plugin.config;


public class PluginConfiguration {
    private static boolean enabled = true;

    private static boolean initialized = false;

    public PluginConfiguration(){
    }

    private PluginConfiguration(boolean enabled, boolean initialized){
        PluginConfiguration.enabled = enabled;
        PluginConfiguration.initialized = initialized;

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
        PluginConfiguration.initialized = initialized;
    }

    @Override
    public String toString() {
        return "enabled: " + enabled + System.lineSeparator()  +"initialized: " + initialized;
    }
}
