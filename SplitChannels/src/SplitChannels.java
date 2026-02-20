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
        
        // Turn the image processor into a color processor
        ColorProcessor cp = (ColorProcessor) imp.getProcessor();
        
        // Information about the pixels (cast is needed because getPixels() returns an object)
        int[] pixels = (int[]) cp.getPixels();
        
        // Information about the image
        int width = imp.getWidth();
        int height = imp.getHeight();
        int size = width * height;

        // Creates a empty array for each channel
        byte[] rPixels = new byte[size];
        byte[] gPixels = new byte[size];
        byte[] bPixels = new byte[size];

        // Loop thru the size of the image to check each pixel
        for (int i = 0; i < size; i++) {
            int c = pixels[i];

            // Use bit shifting to get information about each channel (cast into byte since result is int)
            rPixels[i] = (byte) ((c >> 16) & 0xff); 
            gPixels[i] = (byte) ((c >> 8) & 0xff);  
            bPixels[i] = (byte) (c & 0xff);         
        }

        // Using the populated arrays, generate the image of each channel
        ImagePlus rImp = new ImagePlus(imp.getShortTitle() + " (Red)", new ByteProcessor(width, height, rPixels, null));
        ImagePlus gImp = new ImagePlus(imp.getShortTitle() + " (Green)", new ByteProcessor(width, height, gPixels, null));
        ImagePlus bImp = new ImagePlus(imp.getShortTitle() + " (Blue)", new ByteProcessor(width, height, bPixels, null));

        // Show the images
        rImp.show();
        gImp.show();
        bImp.show();
    }
}