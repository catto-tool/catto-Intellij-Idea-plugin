package fi.tampere.whatTests.plugin.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

public class PluginConfigWrapper {

    private static PluginConfigurator CONFIG;

    public PluginConfigurator getCONFIG() {
        return CONFIG;
    }

    private void setCONFIG(File f) throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        CONFIG = om.readValue(f, PluginConfigurator.class);
    }


    public PluginConfigWrapper(String path) {
        File config;
        try {
            config = Paths.get(path).toFile();
            setCONFIG(config);

        } catch (UnsupportedOperationException e) {

            System.out.println("NO CONFIG FILE FOUND");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

