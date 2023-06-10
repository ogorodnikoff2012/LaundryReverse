
package tk.xenon98.laundryapp.console.data.serial;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.regex.Pattern;

public class RectangleSerializer extends JsonSerializer<Rectangle> {

    private static final Pattern BOUNDS_PATTERN = Pattern
            .compile("\\[(?<minX>\\d+),(?<minY>\\d+)]\\[(?<maxX>\\d+),(?<maxY>\\d+)]");

    public static Rectangle parseRawBounds(final String rawBounds) {
        final var matcher = BOUNDS_PATTERN.matcher(rawBounds);
        if (!matcher.find()) {
            return null;
        }

        final int minX = Integer.parseInt(matcher.group("minX"));
        final int minY = Integer.parseInt(matcher.group("minY"));
        final int maxX = Integer.parseInt(matcher.group("maxX"));
        final int maxY = Integer.parseInt(matcher.group("maxY"));

        return new Rectangle(minX, minY, maxX - minX, maxY - minY);
    }

    @Override
    public void serialize(final Rectangle value, final JsonGenerator gen, final SerializerProvider serializers)
            throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("x", value.getX());
        gen.writeNumberField("y", value.getY());
        gen.writeNumberField("width", value.getWidth());
        gen.writeNumberField("height", value.getHeight());
        gen.writeEndObject();
    }

    @Override
    public Class<Rectangle> handledType() {
        return Rectangle.class;
    }
}
