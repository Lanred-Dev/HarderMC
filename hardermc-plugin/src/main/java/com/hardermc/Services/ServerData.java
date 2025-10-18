package com.hardermc.Services;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hardermc.HarderMC;

/** Manages server data storage and retrieval */
public class ServerData {
    private static final String FILE_NAME = "server_data.json";
    private final File file;
    private final Gson gson = new Gson();
    private final Map<String, Object> data = new HashMap<>();

    public ServerData(HarderMC plugin) {
        // Paper doesn't auto-create the plugin folder
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
            HarderMC.LOGGER.info("Plugin data folder created");
        }

        file = new File(plugin.getDataFolder(), FILE_NAME);
        load();

        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, this::save, 6000L, 6000L);
    }

    private void load() {
        if (!file.exists()) {
            HarderMC.LOGGER.info("No existing server data found");
            return;
        }

        try (var reader = java.nio.file.Files.newBufferedReader(file.toPath())) {
            Map<String, Object> loadedData = this.gson.fromJson(reader, new TypeToken<Map<String, Object>>() {
            }.getType());

            if (loadedData != null)
                this.data.putAll(loadedData);

            HarderMC.LOGGER.info("Loaded server data");
        } catch (IOException error) {
            error.printStackTrace();
            HarderMC.LOGGER.severe("Failed to load server data");
        }
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(data, writer);
            HarderMC.LOGGER.info("Saved server data");
        } catch (IOException error) {
            error.printStackTrace();
            HarderMC.LOGGER.severe("Failed to save server data");
        }
    }

    public void set(String key, Object value) {
        data.put(key, value);
    }

    public Object get(String key, Object defaultValue) {
        return data.getOrDefault(key, defaultValue);
    }
}
