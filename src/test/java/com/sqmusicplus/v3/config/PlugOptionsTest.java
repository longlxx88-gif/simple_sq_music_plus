package com.sqmusicplus.v3.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

class PlugOptionsTest {
    @BeforeEach @AfterEach void clear() { SqConfigCache.PlugOptions.clear(); }
    private HashMap<String, String> option(String value, String label) {
        return new HashMap<>(Map.of("value", value, "label", label));
    }
    @Test void settingsAndStartupUseIdNotDisplayName() {
        SqConfigCache.addPlugOptions(option("kw", "某我"));
        SqConfigCache.addPlugOptions(option("qqvip", "鹅厂 VIP下载"));
        SqConfigCache.updatePlugOptions(option("qqvip", "鹅厂VIP下载"));
        assertEquals(2, SqConfigCache.PlugOptions.size());
        assertEquals("qqvip", SqConfigCache.PlugOptions.get(1).get("value"));
        SqConfigCache.removePlugOptions("qqvip");
        assertEquals(1, SqConfigCache.PlugOptions.size());
        assertEquals("kw", SqConfigCache.PlugOptions.getFirst().get("value"));
    }
    @Test void concurrentRegistrationsCannotCreateDuplicates() {
        IntStream.range(0, 200).parallel().forEach(i ->
                SqConfigCache.addPlugOptions(option("qqvip", "QQ" + i)));
        assertEquals(1, SqConfigCache.PlugOptions.size());
    }
}
