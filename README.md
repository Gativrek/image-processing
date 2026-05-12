\# Image Processing Projects



Collection of image processing projects developed for an Image Processing course that I attended during my undergraduate days at the Instituto Federal Fluminense. Covers classical image analysis techniques implemented from scratch in Java as ImageJ plugins, and a deep learning pipeline built in Python on Google Colab.



\## Repository Structure



```

image-processing-projects/

├── README.md

├── java/

│   ├── 01\_BloodCellAnalysis/

│   │   ├── BloodAnalysis.java

│   │   └── README.md

│   ├── 02\_ColorChannelManipulator/

│   │   ├── SplitChannels.java

│   │   ├── MergeChannels.java

│   │   └── README.md

│   ├── 03\_RGB2Grayscale/

│   │   ├── ColorBarGenerator.java

│   │   ├── RGBToGrayscale.java

│   │   └── README.md

│   ├── 04\_Point2PointOperations/

│   │   ├── ImageAdjustments.java

│   │   └── README.md

│   ├── 05\_HistogramOperations/

│   │   ├── HistogramOperations.java

│   │   └── README.md

│   ├── 06\_SpatialFilters/

│   │   ├── SpatialFilters.java

│   │   ├── NonLinearFilters.java

│   │   └── README.md

│   ├── 07\_MorphologicalOperations/

│   │   ├── MorphologicalOps.java

│   │   └── README.md

│   ├── 08\_RegionOfInterest/

│   │   ├── ROIExtractor.java

│   │   └── README.md

│   └── 09\_ConnectedComponents/

│       ├── ConnectedComponents.java

│       └── README.md

└── colab/

&#x20;   ├── 10\_YOLOTrainingPipeline/

&#x20;   │   ├── roiExtraction.ipynb

&#x20;   │   ├── yoloDatasetGen.ipynb

&#x20;   │   ├── yoloTraining.ipynb

&#x20;   │   └── README.md

```



\## Java Projects (01–09)



All Java projects are ImageJ plugins written and compiled in Eclipse IDE. Each implements its processing pipeline from scratch without relying on ImageJ's built-in filters, except where noted.



| # | Project | Topics |

|---|---|---|

| 01 | Blood Cell Analysis | Finding white blood cells in a microscope picture |

| 02 | Channel Split and Merge | RGB bit manipulation, packed integer encoding |

| 03a | Color Bar Generator | Synthetic image generation, RGB encoding |

| 03b | RGB to Grayscale | Luminance weighting (BT.601, BT.709), method comparison |

| 04 | Image Adjustments | Brightness, contrast, solarization, desaturation, live preview via `DialogListener` |

| 05 | Histogram Operations | Histogram expansion, histogram equalization via CDF |

| 06 | Spatial Filters | Linear convolution (mean, sharpen, edge); Sobel operator; median filter |

| 07 | Morphological Operations | Erosion, dilation, outline extraction, morphological skeleton |

| 08 | Batch ROI Extractor | Batch image processing, Otsu segmentation, particle analysis, file I/O |

| 09 | Connected Components | BFS flood-fill labeling, 8-connectivity |



\### General Installation



1\. Copy the `.java` file(s) into the `plugins/` folder of your ImageJ installation.

2\. Compile via \*\*Plugins > Compile and Run\*\*, selecting the `.java` file. Alternatively, compile with `javac` against `ij.jar` and drop the `.class` file into `plugins/`. The plugin will appear in the Plugins menu after restarting ImageJ.

3\. Open an appropriate input image and run the plugin from the Plugins menu.



Input type requirements vary per plugin — check the individual README for each project.



\## Colab Projects (10)



| # | Project | Topics |

|---|---|---|

| 10 | Synthetic YOLO Pipeline | ROI extraction, synthetic dataset generation, data augmentation, YOLOv5 training |



\### Overview



Project 10 consists of three notebooks designed to run in sequence on Google Colab with a GPU runtime:



1\. `roiExtraction.ipynb` — segments objects from a source image (e.g. a sprite sheet) using Gaussian blur, Otsu thresholding, and contour detection, and exports the cropped ROIs as a ZIP.

2\. `yoloDatasetGen.ipynb` — composites the extracted ROIs onto downloaded background images to generate a synthetic labeled dataset in YOLO format.

3\. `yoloTraining.ipynb` — splits the dataset, applies augmentation, and trains a YOLOv5m detector for 100 epochs.

