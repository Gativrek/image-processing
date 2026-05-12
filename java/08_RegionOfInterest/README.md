# Project 08 — Batch ROI Extractor

An ImageJ plugin that processes a directory of images, detects objects via Otsu thresholding and particle analysis, and saves each detected region of interest (ROI) as a cropped PNG file.

## Overview

For each image in the input directory, the plugin runs the following pipeline:

```
open image
→ duplicate for color output
→ convert to 8-bit grayscale
→ invert
→ Otsu auto-threshold
→ fill holes
→ erode once
→ dilate once
→ particle analysis (size ≥ 100 px², excluding edge particles)
→ crop each ROI from the color original
→ save as PNG
```

Output files are named `<original_filename>_ROI_001.png`, `_ROI_002.png`, etc. A per-image summary is printed to the ImageJ log.

