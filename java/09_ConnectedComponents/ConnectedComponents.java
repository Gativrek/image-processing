import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import java.util.LinkedList;
import java.util.Queue;

public class ConnectedComponents implements PlugIn {

    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Duplicate to avoid modifying the original image
        ImagePlus workImp = imp.duplicate();
        IJ.run(workImp, "8-bit", "");
        IJ.run(workImp, "Auto Threshold", "method=Default white");

        ImagePlus result = labelConnectedComponents(workImp);
        workImp.close();
        result.show();
    }

    /**
     * Labels connected foreground components using 8-connectivity BFS.
     * Each component receives a unique integer label stored in J[][].
     * Output is a grayscale image with each component mapped to a distinct intensity.
     */
    private ImagePlus labelConnectedComponents(ImagePlus imp) {
        ByteProcessor input = (ByteProcessor) imp.getProcessor();
        int width  = input.getWidth();
        int height = input.getHeight();

        int[][] J = new int[height][width]; // label map; 0 = background or unvisited

        // 8-connectivity offsets: 4 cardinal + 4 diagonal neighbors
        int[] dx = {-1, 1,  0, 0, -1, -1,  1, 1};
        int[] dy = { 0, 0, -1, 1, -1,  1, -1, 1};

        // BFS uses flat int indices (y * width + x) to avoid per-pixel Point allocation
        Queue<Integer> Q = new LinkedList<>();
        int label = 1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (input.get(x, y) == 255 && J[y][x] == 0) {
                    J[y][x] = label;
                    Q.add(y * width + x);

                    while (!Q.isEmpty()) {
                        int idx = Q.poll();
                        int px  = idx % width;
                        int py  = idx / width;

                        for (int i = 0; i < 8; i++) {
                            int qx = px + dx[i];
                            int qy = py + dy[i];
                            if (qx >= 0 && qx < width && qy >= 0 && qy < height
                                    && J[qy][qx] == 0 && input.get(qx, qy) == 255) {
                                J[qy][qx] = label;
                                Q.add(qy * width + qx);
                            }
                        }
                    }
                    label++;
                }
            }
        }

        int numComponents = label - 1;
        IJ.log("Number of components found: " + numComponents);

        // Guard: if no components found, return a blank image
        if (numComponents == 0) {
            IJ.log("No foreground components detected after thresholding.");
            return new ImagePlus("Result", new ByteProcessor(width, height));
        }

        // Map each label to a grayscale level in [50, 250] for visual separation
        ByteProcessor output = new ByteProcessor(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (J[y][x] > 0) {
                    int gray = 50 + (J[y][x] * 200 / numComponents);
                    output.set(x, y, Math.min(255, gray));
                }
                // else: output pixel stays 0 (background)
            }
        }

        return new ImagePlus("Result", output);
    }
}