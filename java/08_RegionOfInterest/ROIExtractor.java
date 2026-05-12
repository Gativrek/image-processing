import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.PlugIn;
import ij.plugin.frame.RoiManager;
import java.io.File;

public class ROIExtractor implements PlugIn {

    public void run(String arg) {
        // Directory selection dialog
        GenericDialog gd = new GenericDialog("ROI Extractor");
        gd.addMessage("Select directories for ROI extraction.");
        gd.addDirectoryField("Input Directory:",  "");
        gd.addDirectoryField("Output Directory:", "");
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String inputDir  = gd.getNextString();
        String outputDir = gd.getNextString();

        // Validate output directory before starting the batch
        File outputFolder = new File(outputDir);
        if (!outputFolder.exists() || !outputFolder.isDirectory()) {
            IJ.error("ROIExtractor", "Output directory does not exist:\n" + outputDir);
            return;
        }

        // Collect supported image files from the input directory
        File inputFolder = new File(inputDir);
        File[] files = inputFolder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg")  || lower.endsWith(".jpeg") ||
                   lower.endsWith(".png")  || lower.endsWith(".tif")  ||
                   lower.endsWith(".tiff") || lower.endsWith(".bmp");
        });

        if (files == null || files.length == 0) {
            IJ.error("ROIExtractor", "No supported image files found in:\n" + inputDir);
            return;
        }

        RoiManager roiManager = RoiManager.getRoiManager();

        // Process each image with progress feedback
        for (int i = 0; i < files.length; i++) {
            IJ.showStatus("Processing " + files[i].getName() + " (" + (i + 1) + "/" + files.length + ")");
            IJ.showProgress(i, files.length);
            processImage(files[i], outputDir, roiManager);
        }

        IJ.showProgress(1.0);
        IJ.showStatus("ROI extraction complete. " + files.length + " image(s) processed.");
    }

    /** Detects and saves all ROIs found in a single image. */
    private void processImage(File imageFile, String outputDir, RoiManager roiManager) {
        roiManager.reset();

        // Load image; skip with a log warning if the file cannot be opened
        ImagePlus originalImp = IJ.openImage(imageFile.getAbsolutePath());
        if (originalImp == null) {
            IJ.log("WARNING: Could not open file, skipping: " + imageFile.getName());
            return;
        }

        // Preserve color original for cropping; process a working copy for segmentation
        ImagePlus colorOriginal = originalImp.duplicate();

        IJ.run(originalImp, "8-bit", "");
        IJ.run(originalImp, "Invert", "");
        IJ.run(originalImp, "Auto Threshold", "method=Otsu white");
        IJ.run(originalImp, "Fill Holes", "");
        IJ.run(originalImp, "Erode", "");
        IJ.run(originalImp, "Dilate", "");
        IJ.run(originalImp, "Analyze Particles...", "size=100-Infinity show=Nothing exclude add");
        originalImp.close();

        // Crop and save each detected ROI from the color original
        Roi[] rois = roiManager.getRoisAsArray();
        String baseName = getFileNameWithoutExtension(imageFile.getName());

        for (int i = 0; i < rois.length; i++) {
            colorOriginal.setRoi(rois[i]);
            ImagePlus roiImage = colorOriginal.crop();
            String outputPath = new File(outputDir, baseName + "_ROI_" + String.format("%03d", i + 1) + ".png")
                                    .getAbsolutePath();
            IJ.saveAs(roiImage, "PNG", outputPath);
            roiImage.close();
        }

        colorOriginal.close();
        IJ.log(imageFile.getName() + ": " + rois.length + " ROI(s) saved.");
    }

    /** Returns the filename without its extension. */
    private String getFileNameWithoutExtension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return (dot > 0) ? fileName.substring(0, dot) : fileName;
    }
}