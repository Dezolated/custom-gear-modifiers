package com.dezolated.customgearmodifiers.geartier;

/**
 * Parsing helper for hex color strings used in tier JSON (e.g. {@code "#FFAA00"}, {@code "FFAA00"},
 * {@code "0xFFAA00"}). Values are packed 0xRRGGBB ints.
 */
public final class HexColor {

    private HexColor() {
    }

    /**
     * @param raw      the hex string, with optional leading {@code #} or {@code 0x}.
     * @param fallback returned when {@code raw} is null or not valid hex.
     * @return the packed 0xRRGGBB value.
     */
    public static int parse(String raw, int fallback) {
        if (raw == null) {
            return fallback;
        }
        String s = raw.trim();
        if (s.startsWith("#")) {
            s = s.substring(1);
        } else if (s.startsWith("0x") || s.startsWith("0X")) {
            s = s.substring(2);
        }
        try {
            return (int) (Long.parseLong(s, 16) & 0xFFFFFF);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
