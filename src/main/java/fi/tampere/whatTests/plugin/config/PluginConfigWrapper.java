package fi.tampere.whatTests.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;

public class PluginConfigWrapper {

    private static PluginConfiguration CONFIG;
    private String CONFIG_RELATIVE_PATH = ".whatTests" + File.separator + "pluginConfiguration.yaml";
    private String folder =".whatTests";
    private String basePath;

    private String path;

    public PluginConfigWrapper(String path) {
        File config;
        try {

            this.path = path + File.separator + CONFIG_RELATIVE_PATH;
            this.basePath = path;
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
            File folder = new File(Paths.get(basePath, this.folder).toString());
            folder.mkdirs();
            f.createNewFile();
            FileWriter fWriter = new FileWriter(f);
            PluginConfiguration pluginConfiguration = new PluginConfiguration();
            fWriter.write(pluginConfiguration.toString());
            fWriter.close();
            CONFIG = pluginConfiguration;
        }else
            CONFIG = om.readValue(f, PluginConfiguration.class);
    }

    public void updateConfig() throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        om.writeValue(new File(path), CONFIG);
    }

}

