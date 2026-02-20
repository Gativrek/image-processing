import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;

public class MergeChannels implements PlugIn {

    public void run(String arg) {
    	
    	// Check if there are at least three images open
        if (WindowManager.getImageCount() < 3) {
            IJ.showMessage("Error", "Three images are required."); return;
        }

        // Gets the filename of each open image, instead of its ID, for better dialog box
        int[] ids = WindowManager.getIDList();
        String[] titles = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            titles[i] = WindowManager.getImage(ids[i]).getTitle();
        }

        // Sets up the generic dialog box
        GenericDialog gd = new GenericDialog("Merge RGB Channels");
        gd.addMessage("Choose the three images to merge:");
        
        // Create the drop down menus for each channel
        String sel1 = titles[0];
        String sel2 = (titles.length > 1) ? titles[1] : titles[0];
        String sel3 = (titles.length > 2) ? titles[2] : titles[0];
        gd.addChoice("Red Channel:", titles, sel1);
        gd.addChoice("Green Channel:", titles, sel2);
        gd.addChoice("Blue Channel:", titles, sel3);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Return to the variable what image was chosen for each channel
        ImagePlus imgR = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus imgG = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);
        ImagePlus imgB = WindowManager.getImage(ids[gd.getNextChoiceIndex()]);

        // Get image information to reconstruct it
        int width = imgR.getWidth();
        int height = imgR.getHeight();

        // Starts getting the data from the processor
        byte[] rPix = (byte[]) imgR.getProcessor().getPixels();
        byte[] gPix = (byte[]) imgG.getProcessor().getPixels();
        byte[] bPix = (byte[]) imgB.getProcessor().getPixels();
        
        // Creates an empty array to store the new image data
        int size = width * height;
        int[] colorPixels = new int[size];

        // Main merging loop, reverse logic to SplitChannels.java
        for (int i = 0; i < size; i++) {
            int r = rPix[i] & 0xff; 
            int g = gPix[i] & 0xff;
            int b = bPix[i] & 0xff;

            // Use bit shifting to organize the colors
            colorPixels[i] = (r << 16) | (g << 8) | b;
        }

        // Shows the completed result
        ColorProcessor cp = new ColorProcessor(width, height, colorPixels);
        ImagePlus finalImp = new ImagePlus("Reconstructed RGB Image", cp);
        finalImp.show();
    }
}