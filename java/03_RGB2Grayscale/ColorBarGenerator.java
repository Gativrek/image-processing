import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

public class ColorBarGenerator implements PlugIn {

    public void run(String arg) {
        int barWidth  = 100;
        int barHeight = 150;
        int numBars   = 8;

        int width  = barWidth * numBars;
        int height = barHeight;

        ColorProcessor cp = new ColorProcessor(width, height);

        // Define 8 standard test colors in RGB
        int[][] colors = {
            {255,   0,   0}, // Red
            {  0, 255,   0}, // Green
            {  0,   0, 255}, // Blue
            {255, 255,   0}, // Yellow
            {  0, 255, 255}, // Cyan
            {255,   0, 255}, // Magenta
            {255, 255, 255}, // White
            {  0,   0,   0}  // Black
        };

        // Fill each bar with its packed RGB color value
        for (int bar = 0; bar < numBars; bar++) {
            int r = colors[bar][0];
            int g = colors[bar][1];
            int b = colors[bar][2];
            int colorValue = (r << 16) | (g << 8) | b;

            for (int y = 0; y < height; y++) {
                for (int x = bar * barWidth; x < (bar + 1) * barWidth; x++) {
                    cp.putPixel(x, y, colorValue);
                }
            }
        }

        new ImagePlus("RGB Color Test Bars", cp).show();
    }
}