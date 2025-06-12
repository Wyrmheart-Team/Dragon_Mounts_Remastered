package dmr.DragonMounts.util;

public class MiscUtils {
    /**
     * Convert RGB to HSV color space
     *
     * @return float array with [hue, saturation, value] each in 0-1 range
     */
    public static float[] rgbToHsv(float r, float g, float b) {
        float max = Math.max(r, Math.max(g, b));
        float min = Math.min(r, Math.min(g, b));
        float delta = max - min;

        float hue = 0;
        float saturation = (max == 0) ? 0 : delta / max;

        if (delta != 0) {
            if (max == r) {
                hue = (g - b) / delta;
                if (g < b) hue += 6;
            } else if (max == g) {
                hue = 2 + (b - r) / delta;
            } else {
                hue = 4 + (r - g) / delta;
            }
            hue /= 6;
        }

        return new float[] {hue, saturation, max};
    }

    /**
     * Convert HSV to RGB color space
     *
     * @return float array with [red, green, blue] each in 0-1 range
     */
    public static float[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;

        if (s == 0) {
            // Achromatic (grey)
            r = g = b = v;
            return new float[] {r, g, b};
        }

        h *= 6; // Sector 0 to 5
        int i = (int) Math.floor(h);
        float f = h - i; // Fractional part
        float p = v * (1 - s);
        float q = v * (1 - s * f);
        float t = v * (1 - s * (1 - f));

        switch (i % 6) {
            case 0:
                r = v;
                g = t;
                b = p;
                break;
            case 1:
                r = q;
                g = v;
                b = p;
                break;
            case 2:
                r = p;
                g = v;
                b = t;
                break;
            case 3:
                r = p;
                g = q;
                b = v;
                break;
            case 4:
                r = t;
                g = p;
                b = v;
                break;
            case 5:
                r = v;
                g = p;
                b = q;
                break;
        }

        return new float[] {r, g, b};
    }
    
    @SuppressWarnings("unchecked")
    public static <T extends Number> T upperLower(double value, T lower, T upper) {
        double result = value * (upper.doubleValue() - lower.doubleValue()) + lower.doubleValue();
	    return switch (lower) {
		    case Integer ignored -> (T)Integer.valueOf((int)Math.round(result));
		    case Float ignored   -> (T)Float.valueOf((float)result);
		    case Double ignored  -> (T)Double.valueOf(result);
		    default -> throw new IllegalArgumentException("Unsupported type: " + lower.getClass());
	    };
    }
    
    public static <T extends Number> T randomUpperLower(T lower, T upper) {
        return upperLower(Math.random(), lower, upper);
    }
}
