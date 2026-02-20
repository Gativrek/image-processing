import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;

public class RGBToGrayscale implements PlugIn {

    public void run(String arg) {
        // Generate error if no image is open
    	ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Initialize generic dialog
        GenericDialog gd = new GenericDialog("RGB -> GrayScale Plugin");

        // Create the methods for grayscale conversion
        String[] methods = {
            "Arithmetic Mean (Average)",
            "Weighted Luminance (Analog TV)",
            "Weighted Luminance (Digital Systems)"
        };
        
        // Create radio buttons
        gd.addRadioButtonGroup("Choose the conversion method:", methods, 3, 1, methods[1]);
        
        // Option to keep original image
        gd.addCheckbox("Create new image?", true);

        // If cancel button selected, do nothing
        gd.showDialog();
        if (gd.wasCanceled()) return;

        // Get values for method chosen
        String selectedMethod = gd.getNextRadioButton();
        boolean createNew = gd.getNextBoolean();

        // Get size of image and create processors
        ColorProcessor cp = (ColorProcessor) imp.getProcessor();
        int width = cp.getWidth();
        int height = cp.getHeight();
        ByteProcessor bp = new ByteProcessor(width, height);
        
        // Create variable to hold RGB data
        int[] rgb = new int[3];
        
        // Initialize variable for value of grey
        int grayVal = 0;
        
        double w1 = 0; double w2 = 0; double w3 = 0;

        // Check every pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                
                // Attain RGB data from each pixel
                cp.getPixel(x, y, rgb);
                
                // Based on method, find gray value
                if (selectedMethod.equals(methods[0])) {
                    // Average
                    w1 = 0.333; w2 = 0.333; w3 = 0.333;

                } else if (selectedMethod.equals(methods[1])) {
                    // Weighted (for old analog TVs)
                    w1 = 0.299; w2 = 0.587; w3 = 0.114;

                } else {
                    // Weighted (for new digital systems)
                	w1 = 0.2125; w2 = 0.7154; w3 = 0.072;
                }
                
                // Get the value for gray
                grayVal = getGrayVal(w1, w2, w3, rgb);
                
                // Store result in a new image
                bp.putPixel(x, y, grayVal);
            }
        }

        // Prepare to make the new image
        if (createNew) {
        	
            // The new image will have a suffix for the method chosen
            String suffix = "";
            if (selectedMethod.contains("Average")) suffix = " (Average)";
            else if (selectedMethod.contains("Analog")) suffix = " (Analog)";
            else suffix = " (Digital)";
            
            // Create new image
            ImagePlus newImp = new ImagePlus(imp.getShortTitle() + suffix, bp);
            newImp.show();
        } else {
            // If chosen to replace the original figure
        	// Change color processor to the byte processor
            imp.setProcessor(bp);
            
            // Refresh image
            imp.updateAndDraw();
        }
    }
    private int getGrayVal(double w1, double w2, double w3, int[] rgb) {
    
    	int r = rgb[0];
        int g = rgb[1];
        int b = rgb[2];
        
    	int grayVal = (int) (w1 * r + w2 * g + w3 * b);
    	return grayVal;
    
    }
}