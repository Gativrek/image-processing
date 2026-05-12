import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class SpatialFilters implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Method selection dialog
        String[] items = {
            "Low-Pass (Mean)",
            "High-Pass (Sharpen)",
            "Border Detection (South)"
        };
        GenericDialog gd = new GenericDialog("Spatial Filters");
        gd.addMessage("Choose the filter to be applied:");
        gd.addRadioButtonGroup("Filter Type:", items, 3, 1, items[0]);
        gd.addMessage("Descriptions:\n" +
                      "- Mean: Smoothes the image by averaging over a 3x3 neighborhood.\n" +
                      "- Sharpen: Emphasizes detail via a Laplacian kernel (center = 9).\n" +
                      "- Border: Detects horizontal edges in the south direction.");
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String selected = gd.getNextRadioButton();

        // Build the selected 3x3 kernel
        float[] kernel;
        if (selected.equals(items[0])) {
            float v = 1f / 9f;
            kernel = new float[] { v, v, v,
                                   v, v, v,
                                   v, v, v };
        } else if (selected.equals(items[1])) {
            kernel = new float[] { -1, -1, -1,
                                   -1,  9, -1,
                                   -1, -1, -1 };
        } else {
            kernel = new float[] { -1, -1, -1,
                                    1, -2,  1,
                                    1,  1,  1 };
        }

        // Apply convolution and show result in a new window
        ImageProcessor result = customConvolve(imp.getProcessor(), kernel, 3, 3);
        new ImagePlus(selected + " - Result", result).show();
    }

    /** Dispatches to grayscale or RGB convolution based on processor type. */
    private ImageProcessor customConvolve(ImageProcessor ip, float[] kernel,
                                          int kernelWidth, int kernelHeight) {
        if (ip instanceof ColorProcessor)
            return customConvolveRGB((ColorProcessor) ip, kernel, kernelWidth, kernelHeight);
        return customConvolveGray(ip, kernel, kernelWidth, kernelHeight);
    }

    /** Convolves a single-channel (grayscale) processor; output clamped to [0, 255]. */
    private ImageProcessor customConvolveGray(ImageProcessor ip, float[] kernel,
                                              int kernelWidth, int kernelHeight) {
        int width    = ip.getWidth();
        int height   = ip.getHeight();
        int kCenterX = kernelWidth  / 2;
        int kCenterY = kernelHeight / 2;

        ImageProcessor output = ip.duplicate();

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
                output.putPixelValue(x, y, Math.max(0, Math.min(255, sum)));
            }
        }
        return output;
    }

    /** Convolves each RGB channel independently; output clamped to [0, 255] per channel. */
    private ColorProcessor customConvolveRGB(ColorProcessor ip, float[] kernel,
                                             int kernelWidth, int kernelHeight) {
        int width    = ip.getWidth();
        int height   = ip.getHeight();
        int kCenterX = kernelWidth  / 2;
        int kCenterY = kernelHeight / 2;

        ColorProcessor output = new ColorProcessor(width, height);
        int[] rgb = new int[3]; // reused per pixel to avoid per-pixel allocation

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float sumR = 0, sumG = 0, sumB = 0;
                for (int ky = 0; ky < kernelHeight; ky++) {
                    for (int kx = 0; kx < kernelWidth; kx++) {
                        int ix = x + (kx - kCenterX);
                        int iy = y + (ky - kCenterY);
                        if (ix >= 0 && ix < width && iy >= 0 && iy < height) {
                            ip.getPixel(ix, iy, rgb);
                            float kv = kernel[ky * kernelWidth + kx];
                            sumR += rgb[0] * kv;
                            sumG += rgb[1] * kv;
                            sumB += rgb[2] * kv;
                        }
                    }
                }
                output.putPixel(x, y, new int[] {
                    (int) Math.max(0, Math.min(255, sumR)),
                    (int) Math.max(0, Math.min(255, sumG)),
                    (int) Math.max(0, Math.min(255, sumB))
                });
            }
        }
        return output;
    }
}