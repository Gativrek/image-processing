# Project 10 — Synthetic YOLO Dataset Generation and Training

Three Google Colab notebooks implementing an end-to-end pipeline for training a YOLOv5 object detector on synthetically generated data. The notebooks are designed to be run in sequence.

## Pipeline Overview

```
roiExtraction.ipynb
→ extracts individual object images (ROIs) from a source image (e.g. a sprite sheet)
→ outputs a ZIP of cropped PNGs

yoloDatasetGen.ipynb
→ takes the ROI ZIP as input
→ downloads background images via BingImageCrawler
→ composites ROIs onto backgrounds with alpha blending
→ generates YOLO-format annotations (normalized center\_x, center\_y, width, height)
→ outputs a ZIP of images + label files + dataset.yaml

yoloTraining.ipynb
→ takes the dataset ZIP as input
→ performs an 80/20 train/val split
→ applies 7 augmentations per training image (flip, rotation ±15°, brightness ±, blur, noise)
→ trains YOLOv5m for 100 epochs at 640px
→ runs detection on validation images and displays results
→ downloads the full YOLOv5 run directory
```

## Notebook Details

### roiExtraction.ipynb

Segments objects from a single uploaded image using a classical pipeline:

| Step | Method |
|---|---|
| Smoothing | Gaussian blur (5x5) |
| Thresholding | Otsu (inverted) |
| Morphology | Close (fill holes) → Open (remove noise), elliptical kernel 5x5 |
| Segmentation | 8-connectivity connected components |
| Extraction | Bounding boxes via `cv2.findContours` |

Intermediate images at each pipeline stage are saved alongside the ROI crops. All output is packaged into a single ZIP for download.

**Input**: any image where objects appear on a relatively uniform background. Designed for sprite sheets, but works on any similarly structured source.

### yoloDatasetGen.ipynb

Builds a synthetic training dataset by compositing extracted ROIs onto downloaded background images.

- Backgrounds are downloaded from Bing Image Search across 10 search terms and resized to 640x640.
- White background removal is applied to each ROI (threshold = 240) before compositing, producing an alpha mask for clean blending.
- Each generated image receives 1 to `objects_per_image + 1` randomly placed ROIs, with corresponding YOLO-format label files.
- Default output: 200 synthetic images, 3 objects per image, single class `sprite`.

**Output structure**:

```
yolo_dataset/
├── images/
├── labels/
├── classes.txt
└── dataset.yaml
```

### yoloTraining.ipynb

Trains a YOLOv5m detector on the synthetic dataset.

- Clones the official [Ultralytics YOLOv5](https://github.com/ultralytics/yolov5) repository.
- Splits the uploaded dataset 80% train / 20% val (seeded at 42 for reproducibility).
- Augments the training set 7x: horizontal flip, rotation ±15°, brightness ×0.6 and ×1.4, Gaussian blur, and Gaussian noise. Bounding boxes are correctly recomputed after rotation by transforming all four box corners through the rotation matrix.
- Trains with: `--img 640 --batch 16 --epochs 100 --weights yolov5m.pt --cache`.
- Downloads the full run directory including weights, metrics, and plots.

## How to Run

1. Open each notebook in Google Colab in order.
2. Set the runtime to GPU before running `yoloTraining.ipynb`.
3. Run all cells top to bottom within each notebook.
4. When prompted, upload the ZIP output from the previous notebook.

## Notes

- The synthetic dataset uses a single class (`sprite` / class ID 0). To train on multiple classes, each ROI type must be tracked separately and assigned distinct class IDs before generating labels.
- Background image availability depends on Bing Search results at runtime; the crawler may return fewer images than requested for some search terms.
- YOLOv5 saves successive runs as `exp`, `exp2`, `exp3`, etc. If the training or detection cells are re-run, update the experiment paths in the detect and visualization cells accordingly.

