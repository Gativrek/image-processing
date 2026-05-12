import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;

public class MorphologicalOps implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Validate input is 8-bit grayscale
        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.error("MorphologicalOps", "This plugin requires an 8-bit grayscale binary image.");
            return;
        }

        // Operation selection dialog
        String[] ops = {"Outline Extraction", "Skeletonization"};
        GenericDialog gd = new GenericDialog("Morphological Operations");
        gd.addMessage("Note: input must be a binary image (pixel values 0 and 255 only).");
        gd.addRadioButtonGroup("Operation:", ops, 2, 1, ops[0]);
        gd.showDialog();
        if (gd.wasCanceled()) return;

        String selected = gd.getNextRadioButton();
        ByteProcessor ip = (ByteProcessor) imp.getProcessor();

        ByteProcessor resultIp = selected.equals(ops[0])
            ? applyOutline(ip)
            : applySkeleton(ip);

        new ImagePlus(selected, resultIp).show();
    }

    /** Outline = A - erode(A): keeps only pixels that would be removed by erosion. */
    private ByteProcessor applyOutline(ByteProcessor ip) {
        ByteProcessor original = (ByteProcessor) ip.duplicate();
        ByteProcessor eroded   = (ByteProcessor) ip.duplicate();
        erodeCross(eroded);
        return subtract(original, eroded);
    }

    /**
     * Morphological skeleton: S(A) = union_k { A_k - open(A_k) }
     * where A_k is the k-th erosion of A and open = erode then dilate.
     * Iterates until the eroded image is empty.
     */
    private ByteProcessor applySkeleton(ByteProcessor ip) {
        ByteProcessor temp     = (ByteProcessor) ip.duplicate();
        ByteProcessor skeleton = new ByteProcessor(temp.getWidth(), temp.getHeight());

        while (hasContent(temp)) {
            // Compute opening of current temp: erode then dilate
            ByteProcessor opening = (ByteProcessor) temp.duplicate();
            erodeCross(opening);
            dilateCross(opening);

            // Accumulate skeleton contribution: pixels in temp not recovered by opening
            union(skeleton, subtract(temp, opening));

            // Erode temp for next iteration
            erodeCross(temp);
        }

        return skeleton;
    }

    /**
     * Erosion with a cross structuring element.
     * A pixel is kept (255) only if it and all four cross neighbors are foreground.
     * Border pixels are always set to 0 (incomplete neighborhood).
     * Operates out-of-place then copies back to avoid read-write conflicts.
     */
    private void erodeCross(ByteProcessor ip) {
        int width  = ip.getWidth();
        int height = ip.getHeight();
        byte[] pixels       = (byte[]) ip.getPixels();
        byte[] outputPixels = new byte[pixels.length]; // zero-initialized

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int p = x + y * width;
                boolean match =
                    (pixels[p]         != 0) &&
                    (pixels[p - 1]     != 0) &&
                    (pixels[p + 1]     != 0) &&
                    (pixels[p - width] != 0) &&
                    (pixels[p + width] != 0);
                outputPixels[p] = match ? (byte) 255 : 0;
            }
        }
        System.arraycopy(outputPixels, 0, pixels, 0, pixels.length);
    }

    /**
     * Dilation with a cross structuring element.
     * Any foreground pixel spreads to its four cross neighbors.
     * Operates out-of-place then copies back to avoid read-write conflicts.
     */
    private void dilateCross(ByteProcessor ip) {
        int width  = ip.getWidth();
        int height = ip.getHeight();
        byte[] pixels       = (byte[]) ip.getPixels();
        byte[] outputPixels = new byte[pixels.length]; // zero-initialized

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int p = x + y * width;
                if (pixels[p] != 0) {
                    outputPixels[p]         = (byte) 255;
                    outputPixels[p - 1]     = (byte) 255;
                    outputPixels[p + 1]     = (byte) 255;
                    outputPixels[p - width] = (byte) 255;
                    outputPixels[p + width] = (byte) 255;
                }
            }
        }
        System.arraycopy(outputPixels, 0, pixels, 0, pixels.length);
    }

    /** Returns a new image where pixel = 255 iff a=255 and b=0 (set difference A \ B). */
    private ByteProcessor subtract(ByteProcessor a, ByteProcessor b) {
        int size = a.getWidth() * a.getHeight();
        ByteProcessor result = new ByteProcessor(a.getWidth(), a.getHeight());
        for (int i = 0; i < size; i++)
            result.set(i, (a.get(i) > 0 && b.get(i) == 0) ? 255 : 0);
        return result;
    }

    /** Sets any pixel in dest to 255 where source is foreground (union in place). */
    private void union(ByteProcessor dest, ByteProcessor source) {
        int size = dest.getWidth() * dest.getHeight();
        for (int i = 0; i < size; i++)
            if (source.get(i) > 0) dest.set(i, 255);
    }

    /** Returns true if any pixel in ip is non-zero (image is not yet fully eroded). */
    private boolean hasContent(ByteProcessor ip) {
        int size = ip.getWidth() * ip.getHeight();
        for (int i = 0; i < size; i++)
            if (ip.get(i) > 0) return true;
        return false;
    }
}