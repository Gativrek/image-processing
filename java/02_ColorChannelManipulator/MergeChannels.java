import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

public class MergeChannels implements PlugIn {

    public void run(String arg) {

        // Require at least three open images
        if (WindowManager.getImageCount() < 3) {
            IJ.showMessage("Error", "Three images are required.");
            return;
        }

        // Collect titles of open images for the dialog drop-downs
        int[] ids = WindowManager.getIDList();
        String[] titles = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            titles[i] = WindowManager.getImage(ids[i]).getTitle();
        }

        // Build dialog with one drop-down per channel
        GenericDialog gd = new GenericDialog("Merge RGB Channels");
        gd.addMessage("Select one 8-bit grayscale image per channel:");
        gd.addChoice("Red Channel:",   titles, titles[0]);
        gd.addChoice("Green Channel:", titles, titles.length > 1 ? titles[1] : titles[0]);
        gd.addChoice("Blue Channel:",  titles, titles.length > 2 ? titles[2] : titles[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Retrieve the image selected for each channel
        ImagePlus imgR = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus imgG = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus imgB = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);

        // Validate that all three images are 8-bit grayscale
        if (imgR.getType() != ImagePlus.GRAY8 ||
            imgG.getType() != ImagePlus.GRAY8 ||
            imgB.getType() != ImagePlus.GRAY8) {
            IJ.error("MergeChannels", "All three images must be 8-bit grayscale.");
            return;
        }

        // Validate that all three images share the same dimensions
        int width  = imgR.getWidth();
        int height = imgR.getHeight();
        if (imgG.getWidth() != width || imgG.getHeight() != height ||
            imgB.getWidth() != width || imgB.getHeight() != height) {
            IJ.error("MergeChannels", "All three images must have the same dimensions.");
            return;
        }

        // Get raw pixel arrays from each channel
        byte[] rPix = (byte[]) imgR.getProcessor().getPixels();
        byte[] gPix = (byte[]) imgG.getProcessor().getPixels();
        byte[] bPix = (byte[]) imgB.getProcessor().getPixels();

        // Pack R, G, B into a single int per pixel via bit shifting (inverse of SplitChannels)
        int size = width * height;
        int[] colorPixels = new int[size];
        for (int i = 0; i < size; i++) {
            int r = rPix[i] & 0xff;
            int g = gPix[i] & 0xff;
            int b = bPix[i] & 0xff;
            colorPixels[i] = (r << 16) | (g << 8) | b;
        }

        // Build and display the merged RGB image
        new ImagePlus("Reconstructed RGB Image", new ColorProcessor(width, height, colorPixels)).show();
    }
}