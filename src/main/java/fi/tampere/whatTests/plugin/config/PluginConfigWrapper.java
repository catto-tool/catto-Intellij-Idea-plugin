package fi.tampere.whatTests.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class PluginConfigWrapper {

    private static PluginConfiguration CONFIG;
    private String CONFIG_RELATIVE_PATH = ".whatTests" + File.separator + "pluginConfiguration.yaml";

    private String path;

    public PluginConfigWrapper(String path) {
        File config;
        try {

            this.path = path + File.separator + CONFIG_RELATIVE_PATH;
            this.setConfig();

        } catch (UnsupportedOperationException e) {
            System.out.println("NO CONFIG FILE FOUND");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PluginConfiguration getConfig() {
        return CONFIG;
    }

    private void setConfig() throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        File f = new File(path);

        if(!f.exists()){
            FileWriter fWriter = new FileWriter(f);
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            fWriter.write(pluginConfiguration.toString());
            CONFIG = pluginConfiguration;
        }else
            CONFIG = om.readValue(f, PluginConfiguration.class);
    }

    public void updateConfig(PluginConfiguration pluginConfiguration) throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(new File(path), pluginConfiguration);
        CONFIG = pluginConfiguration;
    }

}

