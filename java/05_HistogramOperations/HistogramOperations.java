import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import java.awt.Color;

public class HistogramOperations implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // This plugin operates only on 8-bit grayscale images
        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.error("HistogramOperations", "This plugin requires an 8-bit grayscale image.");
            return;
        }

        // Preserve original for the before/after histogram comparison
        ImageProcessor originalIp = imp.getProcessor().duplicate();

        // Method selection dialog
        String[] methods = {"Histogram Expansion", "Histogram Equalization"};
        GenericDialog gd = new GenericDialog("Histogram Operations");
        gd.addMessage("Select the histogram technique to apply:");
        gd.addRadioButtonGroup("Technique:", methods, 2, 1, methods[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Apply selected method and update display
        String selectedMethod = gd.getNextRadioButton();
        ImageProcessor ip = imp.getProcessor();

        if (selectedMethod.equals(methods[0])) {
            applyHistogramExpansion(ip);
            IJ.showStatus("Histogram Expansion applied.");
        } else {
            applyHistogramEqualization(ip);
            IJ.showStatus("Histogram Equalization applied.");
        }

        imp.updateAndDraw();
        showHistogramComparison(originalIp, ip, selectedMethod);
    }

    /** Linearly maps the image's intensity range [min, max] to the full [0, 255] range. */
    private void applyHistogramExpansion(ImageProcessor ip) {
        int width       = ip.getWidth();
        int height      = ip.getHeight();
        int totalPixels = width * height;

        // Collect all pixel values to find the current min and max
        int[] pixelValues = new int[totalPixels];
        int idx = 0;
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                pixelValues[idx++] = ip.getPixel(x, y);

        int a_low  = 255;
        int a_high = 0;
        for (int value : pixelValues) {
            if (value < a_low)  a_low  = value;
            if (value > a_high) a_high = value;
        }

        // Guard: if the image is uniform, expansion is undefined — leave it unchanged
        if (a_low == a_high) {
            IJ.log("Histogram Expansion: Image is uniform (all pixels = " + a_low + "). No change applied.");
            return;
        }

        double range_old = a_high - a_low;

        // Apply linear stretch: new = (old - min) / (max - min) * 255
        idx = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int newValue = (int)(((pixelValues[idx++] - a_low) / range_old) * 255.0);
                ip.putPixel(x, y, truncate(newValue, 0, 255));
            }
        }

        IJ.log("Histogram Expansion: Stretched [" + a_low + ", " + a_high + "] to [0, 255].");
    }

    /** Equalizes the histogram using the cumulative distribution function (CDF). */
    private void applyHistogramEqualization(ImageProcessor ip) {
        int width       = ip.getWidth();
        int height      = ip.getHeight();
        int totalPixels = width * height;

        // Build intensity histogram
        int[] histogram = new int[256];
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                histogram[ip.getPixel(x, y)]++;

        // Compute cumulative distribution function (CDF)
        double[] cdf = new double[256];
        cdf[0] = (double) histogram[0] / totalPixels;
        for (int i = 1; i < 256; i++)
            cdf[i] = cdf[i - 1] + (double) histogram[i] / totalPixels;

        // Build lookup table: map each intensity to its equalized value
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++)
            lut[i] = truncate((int) Math.round(cdf[i] * 255.0), 0, 255);

        // Remap every pixel through the lookup table
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                ip.putPixel(x, y, lut[ip.getPixel(x, y)]);

        IJ.log("Histogram Equalization: CDF-based remapping applied.");
    }

    /** Displays before/after histogram bar plots on a shared y-axis scale. */
    private void showHistogramComparison(ImageProcessor beforeIp, ImageProcessor afterIp, String method) {
        int[] histBefore = calculateHistogram(beforeIp);
        int[] histAfter  = calculateHistogram(afterIp);

        // Shared y-axis maximum for visual comparability
        int maxCount = 0;
        for (int i = 0; i < 256; i++) {
            if (histBefore[i] > maxCount) maxCount = histBefore[i];
            if (histAfter[i]  > maxCount) maxCount = histAfter[i];
        }

        double[] xValues = new double[256];
        double[] yBefore = new double[256];
        double[] yAfter  = new double[256];
        for (int i = 0; i < 256; i++) {
            xValues[i] = i;
            yBefore[i] = histBefore[i];
            yAfter[i]  = histAfter[i];
        }

        Plot plotBefore = new Plot("Histogram - BEFORE " + method, "Intensity (0-255)", "Pixel Count");
        plotBefore.setColor(Color.BLUE);
        plotBefore.add("bar", xValues, yBefore);
        plotBefore.setLimits(0, 255, 0, maxCount * 1.1);
        plotBefore.show();

        Plot plotAfter = new Plot("Histogram - AFTER " + method, "Intensity (0-255)", "Pixel Count");
        plotAfter.setColor(Color.RED);
        plotAfter.add("bar", xValues, yAfter);
        plotAfter.setLimits(0, 255, 0, maxCount * 1.1);
        plotAfter.show();
    }

    /** Returns a 256-bin intensity histogram for the given processor. */
    private int[] calculateHistogram(ImageProcessor ip) {
        int[] histogram = new int[256];
        int width  = ip.getWidth();
        int height = ip.getHeight();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                histogram[ip.getPixel(x, y)]++;
        return histogram;
    }

    /** Clamps value to [min, max]. */
    private int truncate(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}