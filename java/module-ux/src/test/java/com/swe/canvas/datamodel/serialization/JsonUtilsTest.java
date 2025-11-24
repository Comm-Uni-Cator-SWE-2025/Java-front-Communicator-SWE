/*
 * -----------------------------------------------------------------------------
 * File: JsonUtils.java
 * Owner: Gajjala Bhavani Shankar
 * Roll Number : 112201026
 * Module : Canvas
 *
 * -----------------------------------------------------------------------------
 */

package com.swe.canvas.datamodel.serialization;

import org.junit.jupiter.api.Test;
import java.awt.Color;
import java.util.List;
import com.swe.canvas.datamodel.shape.Point;

import static org.junit.jupiter.api.Assertions.*;

class JsonUtilsTest {

    @Test
    void testColorToHex() {
        assertEquals("#00000000", JsonUtils.colorToHex(null));
        // Black with full alpha
        assertEquals("#FF000000", JsonUtils.colorToHex(Color.BLACK));
        // White with full alpha
        assertEquals("#FFFFFFFF", JsonUtils.colorToHex(Color.WHITE));
    }

    @Test
    void testJsonEscape() {
        assertEquals("null", JsonUtils.jsonEscape(null));
        assertEquals("\"test\"", JsonUtils.jsonEscape("test"));
        assertEquals("\"test\\\"quote\"", JsonUtils.jsonEscape("test\"quote"));
    }

    @Test
    void testHexToColor() {
        assertEquals(Color.BLACK, JsonUtils.hexToColor(null));
        assertEquals(Color.BLACK, JsonUtils.hexToColor("#123")); // Too short
        assertEquals(Color.BLACK, JsonUtils.hexToColor("123456789")); // No hash
        assertEquals(Color.BLACK, JsonUtils.hexToColor("#ZZZZZZZZ")); // Invalid hex

        Color c = JsonUtils.hexToColor("#FF00FF00"); // Green
        assertEquals(0, c.getRed());
        assertEquals(255, c.getGreen());
        assertEquals(0, c.getBlue());
        assertEquals(255, c.getAlpha());
    }

    @Test
    void testExtractString() {
        String json = "{\"key\": \"value\", \"other\": \"foo\"}";
        assertEquals("value", JsonUtils.extractString(json, "key"));
        assertEquals("foo", JsonUtils.extractString(json, "other"));
        assertNull(JsonUtils.extractString(json, "missing"));
    }

    @Test
    void testExtractDouble() {
        String json = "{\"val\": 12.34, \"int\": 100, \"bad\": \"abc\"}";
        assertEquals(12.34, JsonUtils.extractDouble(json, "val"), 0.001);
        assertEquals(100.0, JsonUtils.extractDouble(json, "int"), 0.001);
        assertEquals(0.0, JsonUtils.extractDouble(json, "bad"), 0.001);
        assertEquals(0.0, JsonUtils.extractDouble(json, "missing"), 0.001);
    }

    @Test
    void testExtractLong() {
        String json = "{\"val\": 123456789}";
        assertEquals(123456789L, JsonUtils.extractLong(json, "val"));
        assertEquals(0L, JsonUtils.extractLong(json, "missing"));
    }

    @Test
    void testExtractBoolean() {
        String json = "{\"t\": true, \"f\": false}";
        assertTrue(JsonUtils.extractBoolean(json, "t"));
        assertFalse(JsonUtils.extractBoolean(json, "f"));
        assertFalse(JsonUtils.extractBoolean(json, "missing"));
    }

    @Test
    void testExtractPoints() {
        String json = "\"Points\": [ {\"X\": 1.0, \"Y\": 2.0}, {\"X\": 3.5, \"Y\": 4.5} ]";
        List<Point> points = JsonUtils.extractPoints(json);
        assertEquals(2, points.size());
        assertEquals(1.0, points.get(0).getX());
        assertEquals(2.0, points.get(0).getY());
        assertEquals(3.5, points.get(1).getX());
        assertEquals(4.5, points.get(1).getY());

        // Test empty/invalid
        assertTrue(JsonUtils.extractPoints("").isEmpty());
    }

    @Test
    void testExtractNestedJson() {
        String json = "{\"outer\": {\"inner\": 1}, \"nullObj\": null}";

        // Valid object
        String nested = JsonUtils.extractNestedJson(json, "outer");
        assertNotNull(nested);
        assertTrue(nested.contains("\"inner\": 1"));

        // Null object
        String nullObj = JsonUtils.extractNestedJson(json, "nullObj");
        assertEquals("null", nullObj);

        // Missing key
        assertNull(JsonUtils.extractNestedJson(json, "missing"));

        // Malformed/Partial
        assertNull(JsonUtils.extractNestedJson("{\"key\": ", "key")); // Ends abruptly
        assertNull(JsonUtils.extractNestedJson("{\"key\": \"not-obj\"}", "key")); // Not an object
    }
}