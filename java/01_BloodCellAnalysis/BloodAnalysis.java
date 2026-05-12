import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.ContrastEnhancer;
import ij.plugin.filter.BackgroundSubtracter;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import ij.process.FloodFiller;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.measure.ResultsTable;

public class BloodAnalysis implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Validate input is RGB before any processing
        if (!(imp.getProcessor() instanceof ColorProcessor)) {
            IJ.error("BloodAnalysis", "Input image must be an RGB color image.");
            return;
        }

        // Set spatial calibration: 1 pixel = 0.298 um (from calibration slide)
        double pixelWidth = 0.298; // um/pixel
        Calibration cal = imp.getCalibration();
        cal.setUnit("um");
        cal.pixelWidth  = pixelWidth;
        cal.pixelHeight = pixelWidth;
        imp.setCalibration(cal);

        // Duplicate original for overlay; maskImp will be modified throughout
        ImagePlus originalImp = imp.duplicate();
        ImagePlus maskImp     = imp.duplicate();
        maskImp.setTitle("Mask");

        // Convert to 8-bit grayscale for thresholding and morphological ops
        new ImageConverter(maskImp).convertToGray8();
        ImageProcessor ip = maskImp.getProcessor();

        // Rolling-ball background subtraction (radius 20 px ≈ 6 um)
        BackgroundSubtracter bs = new BackgroundSubtracter();
        bs.rollingBallBackground(ip, 20, false, true, false, true, true);

        // Stretch histogram to improve threshold robustness (0.35% saturation)
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, 0.35);

        // Otsu thresholding: cells are darker than background in brightfield
        int threshold = ip.getAutoThreshold();
        IJ.log("Otsu threshold: " + threshold);

        int width  = ip.getWidth();
        int height = ip.getHeight();
        int size   = width * height;

        // Binarize: pixels below threshold → white (cell), above → black (background)
        for (int i = 0; i < size; i++) {
            ip.set(i, ip.get(i) < threshold ? 255 : 0);
        }

        // Flood-fill from (0,0) to clear mis-thresholded border pixels
        // NOTE: assumes top-left corner is background — verify on your dataset
        FloodFiller ff = new FloodFiller(ip);
        ip.setValue(0);
        ff.fill(0, 0);

        // Fill enclosed holes (e.g., biconcave RBC centers) to solidify cell blobs
        IJ.run(maskImp, "Fill Holes", "");

        // EDM watershed to separate touching or overlapping cells
        EDM edm = new EDM();
        edm.toWatershed(ip);

        // Particle analysis: collect area and intensity stats per detected cell
        // Size filter: 1.33 um^2 minimum (~15 pixels at 0.298 um/px)
        // Circularity: 0.3–1.0 rejects elongated artifacts
        int measurements = Measurements.AREA | Measurements.MEAN | Measurements.MIN_MAX;
        int options = ParticleAnalyzer.SHOW_OUTLINES         |
                      ParticleAnalyzer.DISPLAY_SUMMARY       |
                      ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES |
                      ParticleAnalyzer.CLEAR_WORKSHEET;

        ResultsTable rt = new ResultsTable();
        ParticleAnalyzer pa = new ParticleAnalyzer(
            options, measurements, rt,
            1.33, Double.POSITIVE_INFINITY, // area range (um^2)
            0.3, 1.0                        // circularity range
        );
        pa.analyze(maskImp);

        // Build masked overlay: copy original color only where mask is white
        ColorProcessor originalCp = (ColorProcessor) originalImp.getProcessor();
        ColorProcessor finalCp    = new ColorProcessor(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                finalCp.putPixel(x, y,
                    ip.getPixel(x, y) > 0
                        ? originalCp.getPixel(x, y)
                        : 0
                );
            }
        }

        // Display final masked image and binary mask
        new ImagePlus("Masked Image", finalCp).show();
        maskImp.updateAndDraw();
        maskImp.show();

        // Free duplicates from memory
        originalImp.close();
    }
}