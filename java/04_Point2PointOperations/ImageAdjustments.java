import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;

public class ImageAdjustments implements PlugIn, DialogListener {

    private ImagePlus imp;
    private ImageProcessor originalIp;

    // Current slider values; defaults represent neutral (no-op) state
    private int brightness   = 0;
    private int contrast     = 0;
    private int solarization = 255; // 255 = no inversion; lower values invert brighter pixels
    private int desaturation = 0;

    public void run(String arg) {
        imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Validate input is RGB before any processing
        if (!(imp.getProcessor() instanceof ColorProcessor)) {
            IJ.error("ImageAdjustments", "Input image must be an RGB color image.");
            return;
        }

        // Preserve original for reset-on-cancel and per-update reapplication
        originalIp = imp.getProcessor().duplicate();

        // Build slider dialog and attach live-preview listener
        GenericDialog gd = new GenericDialog("Point-to-Point Operations");
        gd.addMessage("Adjust the sliders to change the image:");
        gd.addSlider("Brightness:",   -100, 100, brightness);
        gd.addSlider("Contrast:",     -100, 100, contrast);
        gd.addSlider("Solarization:",    1, 255, solarization);
        gd.addSlider("Desaturation:",    0, 100, desaturation);
        gd.addDialogListener(this);
        gd.showDialog();

        // Restore original if the user cancels
        if (gd.wasCanceled()) {
            imp.setProcessor(originalIp);
            imp.updateAndDraw();
        }
    }

    /** Called by ImageJ on every slider change; reads values and reapplies all ops. */
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        brightness   = (int) gd.getNextNumber();
        contrast     = (int) gd.getNextNumber();
        solarization = (int) gd.getNextNumber();
        desaturation = (int) gd.getNextNumber();

        try {
            applyAdjustments();
        } catch (Exception ex) {
            return false;
        }
        return true;
    }

    /** Reapplies all active operations onto a fresh copy of the original. */
    private void applyAdjustments() {
        ImageProcessor ip = originalIp.duplicate();

        if (brightness != 0)     adjustBrightness(ip, brightness);
        if (contrast != 0)       adjustContrast(ip, contrast);
        if (solarization < 255)  applySolarization(ip, solarization); // 255 = no-op: no pixel value exceeds 255
        if (desaturation > 0)    applyDesaturation(ip, desaturation);

        imp.setProcessor(ip);
        imp.updateAndDraw();
    }

    /** Shifts all channels by a fixed offset; clamps to [0, 255]. */
    private void adjustBrightness(ImageProcessor ip, int value) {
        int[] pixels = (int[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = truncate(((c >> 16) & 0xff) + value, 0, 255);
            int g = truncate(((c >>  8) & 0xff) + value, 0, 255);
            int b = truncate(( c        & 0xff) + value, 0, 255);
            pixels[i] = (r << 16) | (g << 8) | b;
        }
    }

    /** Scales channel deviation from mid-gray by contrast factor F; clamps to [0, 255]. */
    private void adjustContrast(ImageProcessor ip, int C) {
        // F > 1 expands contrast; F < 1 compresses it. Formula keeps F = 1 when C = 0.
        double F = (259.0 * (C + 255.0)) / (255.0 * (259.0 - C));
        int[] pixels = (int[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = truncate((int)(F * (((c >> 16) & 0xff) - 128) + 128), 0, 255);
            int g = truncate((int)(F * (((c >>  8) & 0xff) - 128) + 128), 0, 255);
            int b = truncate((int)(F * (( c        & 0xff) - 128) + 128), 0, 255);
            pixels[i] = (r << 16) | (g << 8) | b;
        }
    }

    /** Inverts each channel independently if its value exceeds the threshold. */
    private void applySolarization(ImageProcessor ip, int threshold) {
        int[] pixels = (int[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xff;
            int g = (c >>  8) & 0xff;
            int b =  c        & 0xff;
            if (r > threshold) r = 255 - r;
            if (g > threshold) g = 255 - g;
            if (b > threshold) b = 255 - b;
            pixels[i] = (r << 16) | (g << 8) | b;
        }
    }

    /** Linearly interpolates each pixel between its original color and its BT.601 grayscale value. */
    private void applyDesaturation(ImageProcessor ip, int percentage) {
        double S_col = 1.0 - (percentage / 100.0); // 1.0 = full color, 0.0 = full grayscale
        int[] pixels = (int[]) ip.getPixels();
        for (int i = 0; i < pixels.length; i++) {
            int c = pixels[i];
            int r = (c >> 16) & 0xff;
            int g = (c >>  8) & 0xff;
            int b =  c        & 0xff;
            int gray = (int)(0.299 * r + 0.587 * g + 0.114 * b);
            r = (int)(S_col * r + (1 - S_col) * gray);
            g = (int)(S_col * g + (1 - S_col) * gray);
            b = (int)(S_col * b + (1 - S_col) * gray);
            pixels[i] = (r << 16) | (g << 8) | b;
        }
    }

    /** Clamps value to [min, max]. */
    private int truncate(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }
}