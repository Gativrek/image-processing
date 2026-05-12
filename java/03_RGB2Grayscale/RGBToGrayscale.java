import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

public class RGBToGrayscale implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Validate input is RGB
        if (!(imp.getProcessor() instanceof ColorProcessor)) {
            IJ.error("RGBToGrayscale", "Input image must be an RGB color image.");
            return;
        }

        // Build dialog with method choices and new-image option
        String[] methods = {
            "Arithmetic Mean (Average)",
            "Weighted Luminance (BT.601 — Analog TV)",
            "Weighted Luminance (BT.709 — Digital)"
        };
        GenericDialog gd = new GenericDialog("RGB -> Grayscale");
        gd.addRadioButtonGroup("Conversion method:", methods, 3, 1, methods[1]);
        gd.addCheckbox("Create new image?", true);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String selectedMethod = gd.getNextRadioButton();
        boolean createNew     = gd.getNextBoolean();

        // Resolve weights once, outside the pixel loop
        double w1, w2, w3;
        if (selectedMethod.equals(methods[0])) {
            // Equal contribution from each channel
            w1 = 1.0 / 3.0; w2 = 1.0 / 3.0; w3 = 1.0 / 3.0;
        } else if (selectedMethod.equals(methods[1])) {
            // ITU-R BT.601 (standard definition)
            w1 = 0.299; w2 = 0.587; w3 = 0.114;
        } else {
            // ITU-R BT.709 (HDTV / sRGB)
            w1 = 0.2126; w2 = 0.7152; w3 = 0.0722;
        }

        ColorProcessor cp = (ColorProcessor) imp.getProcessor();
        int width  = cp.getWidth();
        int height = cp.getHeight();
        ByteProcessor bp = new ByteProcessor(width, height);

        // Convert each pixel using the selected weights
        int[] rgb = new int[3];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                cp.getPixel(x, y, rgb);
                bp.putPixel(x, y, getGrayVal(w1, w2, w3, rgb));
            }
        }

        // Display result in a new window or replace the original
        if (createNew) {
            String suffix = selectedMethod.contains("Average") ? " (Average)"
                          : selectedMethod.contains("601")    ? " (BT.601)"
                          : " (BT.709)";
            new ImagePlus(imp.getShortTitle() + suffix, bp).show();
        } else {
            imp.setProcessor(bp);
            imp.updateAndDraw();
        }
    }

    /** Computes the weighted grayscale value for a single pixel. */
    private int getGrayVal(double w1, double w2, double w3, int[] rgb) {
        return (int) (w1 * rgb[0] + w2 * rgb[1] + w3 * rgb[2]);
    }
}