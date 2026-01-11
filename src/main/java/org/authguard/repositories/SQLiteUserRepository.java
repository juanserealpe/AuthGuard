package org.authguard.repositories;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.authguard.models.User;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class SQLiteUserRepository implements IUserRepository {

    private final HikariDataSource dataSource;
    private final Logger logger;
    private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public SQLiteUserRepository(String databasePath, Logger logger) {
        this.logger = logger;
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + databasePath);
        config.setDriverClassName("org.sqlite.JDBC");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.setMaximumPoolSize(10);
        config.setConnectionInitSql("PRAGMA journal_mode=WAL;");


        this.dataSource = new HikariDataSource(config);
        createTable();
    }

    private void createTable() {
        String sql = """
        CREATE TABLE IF NOT EXISTS users (
            uuid TEXT PRIMARY KEY,
            username TEXT NOT NULL,
            password_hash TEXT NOT NULL,
            rank TEXT NOT NULL,
            created_at TEXT NOT NULL,
            last_login TEXT NOT NULL
        );
        """;

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.severe("Could not create users table: " + e.getMessage());
        }
    }


    @Override
    public CompletableFuture<Optional<User>> findByUuid(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM users WHERE uuid = ?";
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, uuid.toString());
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(new User(
                                UUID.fromString(rs.getString("uuid")),
                                rs.getString("username"),
                                rs.getString("password_hash"),
                                rs.getString("rank"),
                                LocalDateTime.parse(rs.getString("created_at"), formatter),
                                LocalDateTime.parse(rs.getString("last_login"), formatter)));
                    }
                }
            } catch (SQLException e) {
                logger.severe("Error finding user by UUID: " + e.getMessage());
            }
            return Optional.empty();
        });
    }

    @Override
    public CompletableFuture<Void> save(User user) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT OR REPLACE INTO users
                (uuid, username, password_hash, rank, created_at, last_login)
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            try (Connection conn = dataSource.getConnection();
                    PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, user.getUuid().toString());
                pstmt.setString(2, user.getUsername());
                pstmt.setString(3, user.getPasswordHash());
                pstmt.setString(4, user.getRank());
                pstmt.setString(5, user.getCreatedAt().format(formatter));
                pstmt.setString(6, user.getLastConnection().format(formatter));
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logger.severe("Error saving user: " + e.getMessage());
            }
        });
    }

    @Override
    public CompletableFuture<Void> updateLastLogin(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE users SET last_login = ? WHERE uuid = ?";

            try (Connection conn = dataSource.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {

                pstmt.setString(1, LocalDateTime.now().format(formatter));
                pstmt.setString(2, uuid.toString());
                pstmt.executeUpdate();

            } catch (SQLException e) {
                logger.severe("Error updating last connection: " + e.getMessage());
            }
        });
    }


    @Override
    public void close() {
        if (dataSource != null) {
            dataSource.close();
        }
    }
}
