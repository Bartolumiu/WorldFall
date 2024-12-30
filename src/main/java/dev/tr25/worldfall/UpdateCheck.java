package dev.tr25.worldfall;

import org.bukkit.Bukkit;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URI;

/**
 * This class checks for updates on Modrinth.
 * It sends a GET request to the Modrinth API to get the latest version of the plugin.
 * The current version is read from the pom.xml file.
 * The version numbers are compared to check if an update is available.
 */
public class UpdateCheck {
    private final WorldFall wfr;
    private static final String MODRINTH_URL = "https://api.modrinth.com/v3/project/BKZW4Asv/version";

    /**
     * Constructor
     * @param wfr WorldFall instance
     */
    public UpdateCheck(WorldFall wfr) {
        this.wfr = wfr;
    }

    /**
     * Check if an update is available for the plugin.
     * @param platform Platform to check for updates (e.g. spigot, fabric)
     * @return true if an update is available, false otherwise
     */
    public boolean isUpdateAvailable(String platform) {
        try {
            // Read current version from pom.xml
            String currentVersion = getCurrentVersion();

            // Send GET request to Modrinth API
            URI uri = new URI(MODRINTH_URL);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            // Check response code
            if (connection.getResponseCode() != 200) {
                wfr.getLogger().warning("Failed to check updates, Modrinth returned "+connection.getResponseCode());
                return false;
            }
            // Read response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parse JSON response
            JSONArray versions = (JSONArray) new JSONParser().parse(response.toString());

            // Get latest version
            String latestVersion = null;
            for (Object obj : versions) {
                JSONObject version = (JSONObject) obj;
                JSONArray loaders = (JSONArray) version.get("loaders");
                if (loaders.contains(platform)) {
                    String versionNumber = (String) version.get("version_number");
                    if (isNewerVersion(versionNumber, latestVersion)) {
                        latestVersion = versionNumber;
                    }
                }
            }

            // Compare latest version with current version
            return isNewerVersion(currentVersion, latestVersion);
        } catch (Exception e) {
            wfr.getLogger().warning("Failed to check updates: "+e.getMessage());
            return false;
        }
    }

    /**
     * Get the current version of the plugin.
     * @return Current version
     */
    private String getCurrentVersion() {
        return wfr.version;
    }

    /**
     * Compare two version numbers to check if the latest version is newer.
     * Version format: major.minor-platform-env
     * Environment order: dev < beta < release
     * Example: 0.1-paper-dev
     * @param currentVersion Current version
     * @param latestVersion Latest version
     * @return true if the latest version is newer, false otherwise
     */
    private boolean isNewerVersion(String currentVersion, String latestVersion) {
        if (latestVersion == null) {
            return true;
        }
        // Split version strings into parts
        // Version format: major.minor-platform-env
        // Example: 0.1-paper-dev
        String[] currentParts = currentVersion.split("-");
        String[] latestParts = latestVersion.split("-");

        // Extract version components
        String currentVersionStr = currentParts[0];
        String currentEnv = currentParts.length > 1 ? currentParts[1] : "";
        String latestVersionStr = latestParts[0];
        String latestEnv = latestParts.length > 1 ? latestParts[1] : "";

        // Split version components into major and minor parts
        String[] currentVersionNums = currentVersionStr.split("\\.");
        String[] latestVersionNums = latestVersionStr.split("\\.");

        // Parse version numbers
        int currentMajor = Integer.parseInt(currentVersionNums[0]);
        int currentMinor = Integer.parseInt(currentVersionNums[1]);
        int latestMajor = Integer.parseInt(latestVersionNums[0]);
        int latestMinor = Integer.parseInt(latestVersionNums[1]);

        // Compare versions
        // Check major version first
        if (latestMajor > currentMajor) {
            return true;
        }
        // Check minor version if major versions are equal
        if (latestMajor == currentMajor && latestMinor > currentMinor) {
            return true;
        }
        // Check environment if minor versions are equal
        if (latestMajor == currentMajor && latestMinor == currentMinor) {
            // Check if environment indicates a newer version
            if (currentEnv.equals("dev") && !latestEnv.equals("dev")) {
                return true;
            }
            if (currentEnv.equals("beta") && latestEnv.equals("release")) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get the latest version of the plugin.
     * @param platform Platform to check for updates (e.g. spigot, fabric)
     * @return Latest version
     */
    public String getLatestVersion(String platform) {
        try {
            // Send a GET request to the Modrinth API
            URI uri = new URI(MODRINTH_URL);
            HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod("GET");

            // Check if the response code is 200
            if (connection.getResponseCode() != 200) {
                Bukkit.getLogger().warning("Failed to get the latest version from Modrinth.");
                Bukkit.getLogger().warning("Response code: "+connection.getResponseCode());
                return null;
            }

            // Read the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            // Parse the response
            JSONArray versions = (JSONArray) new JSONParser().parse(response.toString());

            // Get latest version
            String latestVersion = null;
            for (Object obj : versions) {
                JSONObject version = (JSONObject) obj;
                JSONArray loaders = (JSONArray) version.get("loaders");
                if (loaders.contains(platform)) {
                    String versionNumber = (String) version.get("version_number");
                    if (isNewerVersion(versionNumber, latestVersion)) {
                        latestVersion = versionNumber;
                    }
                }
            }

            return latestVersion;
        } catch (Exception e) {
            Bukkit.getLogger().warning("Failed to get the latest version from Modrinth.");
            Bukkit.getLogger().warning("Error: " + e.getMessage());
            return null;
        }
    }
}