import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.util.Arrays;

public class NonLinearFilters implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Filter selection dialog
        String[] filters = {"Sobel", "Median"};
        GenericDialog gd = new GenericDialog("Non-Linear Spatial Filters");
        gd.addMessage("Choose the filter to be applied:");
        gd.addRadioButtonGroup("Filter Type:", filters, 2, 1, filters[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String selected = gd.getNextRadioButton();
        if (selected.equals(filters[0])) {
            applySobel(imp);
        } else {
            applyMedian(imp);
        }
    }

    /** Computes Gx, Gy, and gradient magnitude using the Sobel operator. Shown as float images. */
    private void applySobel(ImagePlus imp) {
        FloatProcessor base = imp.getProcessor().convertToFloatProcessor();

        float[] kernelX = { -1,  0,  1,
                            -2,  0,  2,
                            -1,  0,  1 };

        float[] kernelY = {  1,  2,  1,
                              0,  0,  0,
                             -1, -2, -1 };

        // Convolve with each directional kernel
        FloatProcessor ipX = customConvolve(base, kernelX, 3, 3);
        FloatProcessor ipY = customConvolve(base, kernelY, 3, 3);

        new ImagePlus("Horizontal Sobel (Gx)", ipX).show();
        new ImagePlus("Vertical Sobel (Gy)",   ipY).show();

        // Compute gradient magnitude: |G| = sqrt(Gx^2 + Gy^2)
        int width  = ipX.getWidth();
        int height = ipX.getHeight();
        FloatProcessor ipMagnitude = new FloatProcessor(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float gx = ipX.getPixelValue(x, y);
                float gy = ipY.getPixelValue(x, y);
                ipMagnitude.putPixelValue(x, y, (float) Math.sqrt(gx * gx + gy * gy));
            }
        }

        new ImagePlus("Sobel Magnitude", ipMagnitude).show();
    }

    /** Replaces each pixel with the median of its 3x3 neighborhood. Requires GRAY8 input. */
    private void applyMedian(ImagePlus imp) {
        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.error("NonLinearFilters", "Median filter requires an 8-bit grayscale image.");
            return;
        }

        ImageProcessor originalIp = imp.getProcessor();
        int width  = originalIp.getWidth();
        int height = originalIp.getHeight();
        ImageProcessor medianIp = originalIp.duplicate();

        int[] window = new int[9]; // 3x3 neighborhood buffer, reused per pixel

        // Skip the 1-pixel border (out-of-bounds neighbors not handled for simplicity)
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int k = 0;
                for (int ky = -1; ky <= 1; ky++)
                    for (int kx = -1; kx <= 1; kx++)
                        window[k++] = originalIp.getPixel(x + kx, y + ky);
                Arrays.sort(window);
                medianIp.putPixel(x, y, window[4]); // center element of sorted 9-value window
            }
        }

        new ImagePlus("Median", medianIp).show();
    }

    /** Convolves a FloatProcessor with the given kernel; border pixels skip out-of-bounds taps. */
    private FloatProcessor customConvolve(FloatProcessor ip, float[] kernel,
                                          int kernelWidth, int kernelHeight) {
        int width    = ip.getWidth();
        int height   = ip.getHeight();
        int kCenterX = kernelWidth  / 2;
        int kCenterY = kernelHeight / 2;

        FloatProcessor output = new FloatProcessor(width, height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float sum = 0;
                for (int ky = 0; ky < kernelHeight; ky++) {
                    for (int kx = 0; kx < kernelWidth; kx++) {
                        int ix = x + (kx - kCenterX);
                        int iy = y + (ky - kCenterY);
                        if (ix >= 0 && ix < width && iy >= 0 && iy < height)
                            sum += ip.getPixelValue(ix, iy) * kernel[ky * kernelWidth + kx];
                    }
                }
                output.putPixelValue(x, y, sum);
            }
        }
        return output;
    }
}