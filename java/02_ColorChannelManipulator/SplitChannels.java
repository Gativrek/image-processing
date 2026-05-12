import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

public class SplitChannels implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Validate input is RGB
        if (!(imp.getProcessor() instanceof ColorProcessor)) {
            IJ.error("SplitChannels", "Input image must be an RGB color image.");
            return;
        }

        // Get packed int pixel array from the color processor
        ColorProcessor cp = (ColorProcessor) imp.getProcessor();
        int[] pixels = (int[]) cp.getPixels();

        int width  = imp.getWidth();
        int height = imp.getHeight();
        int size   = width * height;

        // Allocate one byte array per channel
        byte[] rPixels = new byte[size];
        byte[] gPixels = new byte[size];
        byte[] bPixels = new byte[size];

        // Extract each channel via bit shifting; cast to byte since result is int
        for (int i = 0; i < size; i++) {
            int c      = pixels[i];
            rPixels[i] = (byte) ((c >> 16) & 0xff);
            gPixels[i] = (byte) ((c >> 8)  & 0xff);
            bPixels[i] = (byte)  (c        & 0xff);
        }

        // Build and display one grayscale image per channel
        new ImagePlus(imp.getShortTitle() + " (Red)",   new ByteProcessor(width, height, rPixels, null)).show();
        new ImagePlus(imp.getShortTitle() + " (Green)", new ByteProcessor(width, height, gPixels, null)).show();
        new ImagePlus(imp.getShortTitle() + " (Blue)",  new ByteProcessor(width, height, bPixels, null)).show();
    }
}