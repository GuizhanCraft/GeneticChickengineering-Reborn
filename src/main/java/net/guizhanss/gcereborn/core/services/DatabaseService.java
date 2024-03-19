package net.guizhanss.gcereborn.core.services;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.base.Preconditions;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import net.guizhanss.gcereborn.GeneticChickengineering;

@SuppressWarnings("ConstantConditions")
public final class DatabaseService {

    private static final String DB_NAME = "GCE.db";

    private final String dbPath;

    private Connection conn;
    private boolean hasChanges = false;
    private Map<String, String> cache;

    public DatabaseService(GeneticChickengineering plugin) {
        dbPath = plugin.getDataFolder().getPath() + File.separator + DB_NAME;

        connect();

        if (isConnected()) {
            GeneticChickengineering.log(Level.INFO, GeneticChickengineering.getLocalization().getString(
                "console.database.connected"));
            createTables();
        }
    }

    public boolean isConnected() {
        return conn != null;
    }

    public boolean checkConnection() {
        if (!isConnected()) {
            GeneticChickengineering.log(Level.SEVERE, GeneticChickengineering.getLocalization().getString(
                "console.database.not-connected"));
            return false;
        } else {
            return true;
        }
    }

    @Nonnull
    public Map<String, String> getAllChickens() {
        if (!hasChanges && cache != null) {
            return cache;
        }

        checkConnection();
        Map<String, String> chickens = new HashMap<>();
        try {
            String sql = "SELECT uuid, dna FROM entities;";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                chickens.put(rs.getString("uuid"), rs.getString("dna"));
            }
            hasChanges = false;
            cache = chickens;
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
        return chickens;
    }

    public boolean hasChicken(@Nonnull String uuid) {
        Preconditions.checkArgument(uuid != null, "uuid cannot be null");

        return getAllChickens().containsKey(uuid);
    }

    /**
     * Get the chicken DNA.
     * Make sure you have called {@link #hasChicken(String)} to check if chicken exists first.
     *
     * @param uuid The uuid of the chicken.
     * @return The chicken DNA.
     */
    @Nullable
    public String getChickenDNA(@Nonnull String uuid) {
        Preconditions.checkArgument(uuid != null, "uuid cannot be null");

        return getAllChickens().get(uuid);
    }

    @ParametersAreNonnullByDefault
    public void addChicken(String uuid, String dna) {
        Preconditions.checkArgument(uuid != null, "uuid cannot be null");
        Preconditions.checkArgument(dna != null, "dna cannot be null");

        checkConnection();
        String sql = "INSERT INTO entities (uuid, dna) VALUES (?, ?);";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.setString(2, dna);
            stmt.executeUpdate();
            hasChanges = true;
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
    }

    public void removeChicken(@Nonnull String uuid) {
        Preconditions.checkArgument(uuid != null, "uuid cannot be null");

        checkConnection();
        String sql = "DELETE FROM entities WHERE uuid = ?;";
        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid);
            stmt.executeUpdate();
            hasChanges = true;
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
    }


    /**
     * Clean up the database, remove the records of chickens that no longer exist.
     */
    public void cleanup() {
        var chickens = getAllChickens();
        if (chickens.isEmpty()) {
            return;
        }
        List<World> worlds = GeneticChickengineering.getInstance().getServer().getWorlds();

        List<String> found = new ArrayList<>();
        for (World world : worlds) {
            for (var entry : chickens.entrySet()) {
                String uuid = entry.getKey();
                if (found.contains(uuid)) {
                    continue;
                }
                Entity chick = world.getEntity(UUID.fromString(uuid));
                if (chick != null) {
                    found.add(uuid);
                }
            }
        }
        int deleteCount = 0;

        for (var entry : chickens.entrySet()) {
            String uuid = entry.getKey();
            if (found.contains(uuid)) {
                continue;
            }
            removeChicken(uuid);
            deleteCount++;
        }
        commit();
        if (deleteCount > 0) {
            GeneticChickengineering.log(Level.INFO, GeneticChickengineering.getLocalization().getString(
                "console.database.cleanup", deleteCount));
        }
    }

    public void shutdown() {
        checkConnection();
        try {
            commit();
            conn.close();
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
    }

    private void connect() {
        try {
            String url = "jdbc:sqlite:" + dbPath;
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.connect-fail"));
        }
    }

    private void createTables() {
        try {
            String sql = "CREATE TABLE IF NOT EXISTS entities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "uuid TEXT NOT NULL," +
                "dna TEXT NOT NULL);";
            execSql(sql);
            commit();
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
    }

    private void execSql(@Nonnull String sql) throws SQLException {
        if (!checkConnection()) return;
        GeneticChickengineering.debug("Prepare execute sql entry: {0}", sql);

        try (var stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private void commit() {
        try {
            conn.commit();
        } catch (SQLException ex) {
            GeneticChickengineering.log(Level.SEVERE, ex, GeneticChickengineering.getLocalization().getString(
                "console.database.exception"));
        }
    }
}
