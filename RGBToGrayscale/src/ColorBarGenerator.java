import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

public class ColorBarGenerator implements PlugIn {

    public void run(String arg) {
        int barWidth = 100;
        int barHeight = 150;
        int numBars = 8;
        
        int width = barWidth * numBars;
        int height = barHeight;
        
        ColorProcessor cp = new ColorProcessor(width, height);
        
        // Define 8 test colors: Red, Green, Blue, Yellow, Cyan, Magenta, White, Black
        int[][] colors = {
            {255, 0, 0},     // Red
            {0, 255, 0},     // Green
            {0, 0, 255},     // Blue
            {255, 255, 0},   // Yellow
            {0, 255, 255},   // Cyan
            {255, 0, 255},   // Magenta
            {255, 255, 255}, // White
            {0, 0, 0}        // Black
        };
        
        String[] labels = {"Red", "Green", "Blue", "Yellow", "Cyan", "Magenta", "White", "Black"};
        
        // Fill each bar with its color
        for (int bar = 0; bar < numBars; bar++) {
            int r = colors[bar][0];
            int g = colors[bar][1];
            int b = colors[bar][2];
            
            // Pack RGB into single int
            int colorValue = (r << 16) | (g << 8) | b;
            
            // Fill the rectangular region for this bar
            for (int y = 0; y < height; y++) {
                for (int x = bar * barWidth; x < (bar + 1) * barWidth; x++) {
                    cp.putPixel(x, y, colorValue);
                }
            }
        }
        
        ImagePlus imp = new ImagePlus("RGB Color Test Bars", cp);
        imp.show();
    }
}