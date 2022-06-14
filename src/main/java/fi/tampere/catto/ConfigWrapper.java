package fi.tampere.catto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ConfigWrapper {
    private static final String CONFIG_FILE = "config.yaml";
    private static Configurator CONFIG;

    public Configurator getCONFIG() {
        return CONFIG;
    }

    private void setCONFIG(File f) throws IOException {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        CONFIG = om.readValue(f, Configurator.class);
    }


    public ConfigWrapper(String path) {
        File config;
        try {
            config = Paths.get(path, CONFIG_FILE).toFile();
            setCONFIG(config);

        } catch (IOException | RuntimeException e) {
            File f = Paths.get(path, CONFIG_FILE).toFile();
            try {
                ObjectMapper om = new ObjectMapper(new YAMLFactory());
                Configurator configurator = new Configurator(new ArrayList<String>(), new ArrayList<String>(),".tmp");
                om.writeValue(f, configurator);
            } catch (IOException ex) {
                System.out.println("NO CONFIG FILE FOUND");
            }



        }
    }

}

