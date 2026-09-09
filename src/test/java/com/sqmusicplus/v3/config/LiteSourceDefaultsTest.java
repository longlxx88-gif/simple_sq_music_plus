package com.sqmusicplus.v3.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

class LiteSourceDefaultsTest {
    @Test
    void repeatedStartupSeedsMissingSourcesWithoutOverwritingUserSettings() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:sourceDefaults;MODE=MySQL")) {
            ScriptUtils.executeSqlScript(connection, new ClassPathResource("db/n1-schema.sql"));
            var defaults = new ClassPathResource("db/lite-source-defaults.sql");
            ScriptUtils.executeSqlScript(connection, defaults);
            try (var statement = connection.createStatement()) {
                statement.executeUpdate("UPDATE sq_config SET config_value='false' WHERE config_key='plug.mg.open'");
                statement.executeUpdate("UPDATE sq_config SET config_value='saved-token' WHERE config_key='plug.tidal.token'");
                ScriptUtils.executeSqlScript(connection, defaults);
                try (var rows = statement.executeQuery("SELECT COUNT(*) FROM sq_config")) {
                    assertTrue(rows.next());
                    assertEquals(4, rows.getInt(1));
                }
                try (var rows = statement.executeQuery("SELECT config_value FROM sq_config WHERE config_key='plug.tidal.token'")) {
                    assertTrue(rows.next());
                    assertEquals("saved-token", rows.getString(1));
                }
                try (var rows = statement.executeQuery("SELECT config_value FROM sq_config WHERE config_key='plug.mg.open'")) {
                    assertTrue(rows.next());
                    assertEquals("false", rows.getString(1));
                }
                try (var rows = statement.executeQuery("SELECT config_value FROM sq_config WHERE config_key='system.download.file.template'")) {
                    assertTrue(rows.next());
                    assertEquals("${artists}/${album}/${musicName} - ${artists}", rows.getString(1));
                }
            }
        }
    }
}
