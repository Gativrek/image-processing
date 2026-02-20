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

        // Calibration based on scale image (0.298um/pixel)
        double pixelWidth = 0.298; 
        Calibration cal = imp.getCalibration();
        cal.setUnit("um");
        cal.pixelWidth = pixelWidth;
        cal.pixelHeight = pixelWidth;
        imp.setCalibration(cal);

        // For better processing, duplicate original image
        // This is needed since the original image will be used with the mask
        ImagePlus originalImp = imp.duplicate();
        ImagePlus maskImp = imp.duplicate();
        maskImp.setTitle("Mask");

        // Pre-processing (convert to gray scale for thresholding)
        new ImageConverter(maskImp).convertToGray8();
        ImageProcessor ip = maskImp.getProcessor();

        // Background subtraction using Rolling Ball
        BackgroundSubtracter bs = new BackgroundSubtracter();
        bs.rollingBallBackground(ip, 20, false, true, false, true, true);

        // Increase contrast for easier thresholding
        ContrastEnhancer ce = new ContrastEnhancer();
        ce.stretchHistogram(ip, 0.35);

        // Using Otsu's algorithm to find the threshold, and display the value
        // Based on the value from the algorithm, lower than that will be background, higher will be cells
        int threshold = ip.getAutoThreshold();
        IJ.log("Threshold: " + threshold);

        // Pre-initialize variables
        int width = ip.getWidth();
        int height = ip.getHeight();
        int size = width * height;
        
        // Check every pixel to see if its a cell or background comparing to threshold
        // This will be the black and white mask
        for (int i = 0; i < size; i++) {
            int pixelValue = ip.get(i);

            if (pixelValue < threshold) {
                ip.set(i, 255);
            } else {
                ip.set(i, 0);
            }
        }

        // Fill holes using flood filler to distinguish outer backgrounds
        FloodFiller ff = new FloodFiller(ip);
        ip.setValue(0); 
        ff.fill(0, 0); 
        IJ.run(maskImp, "Fill Holes", ""); 

        // Use watershed to separate cells that are touching each other
        EDM edm = new EDM();
        edm.toWatershed(ip);

        // Display measurements of the image, such as area and mean
        int measurements = Measurements.AREA | Measurements.MEAN | Measurements.MIN_MAX;
        int options = ParticleAnalyzer.SHOW_OUTLINES | ParticleAnalyzer.DISPLAY_SUMMARY | 
                      ParticleAnalyzer.EXCLUDE_EDGE_PARTICLES | ParticleAnalyzer.CLEAR_WORKSHEET;
        
        // Create the results table
        ResultsTable rt = new ResultsTable();
        ParticleAnalyzer pa = new ParticleAnalyzer(options, measurements, rt, 15, Double.POSITIVE_INFINITY, 0.3, 1.0); // Ignore anything smaller than 15pixels
        pa.analyze(maskImp); 

        // Create the processor to make the mask overlay
        ColorProcessor originalCp = (ColorProcessor) originalImp.getProcessor();
        ColorProcessor finalCp = new ColorProcessor(width, height);
        
        // Check every pixel
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // If mask pixel is white
                if (ip.getPixel(x, y) > 0) {
                    // Copy the original pixel
                    finalCp.putPixel(x, y, originalCp.getPixel(x, y));
                } else {
                    // Otherwise, make it black
                    finalCp.putPixel(x, y, 0);
                }
            }
        }
        
        // Show the final result
        ImagePlus finalImp = new ImagePlus("Masked Image", finalCp);
        finalImp.show();
        maskImp.updateAndDraw();
        maskImp.show();
    }
}