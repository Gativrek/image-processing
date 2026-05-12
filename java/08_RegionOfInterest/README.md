\# Project 08 — Batch ROI Extractor

&#x20;

An ImageJ plugin that processes a directory of images, detects objects via

Otsu thresholding and particle analysis, and saves each detected region of

interest (ROI) as a cropped PNG file.

&#x20;

\---

&#x20;

\## Overview

&#x20;

For each image in the input directory, the plugin runs the following pipeline:

&#x20;

```

open image

&#x20; → duplicate for color output

&#x20; → convert to 8-bit grayscale

&#x20; → invert

&#x20; → Otsu auto-threshold

&#x20; → fill holes

&#x20; → erode (1x)

&#x20; → dilate (1x)

&#x20; → particle analysis (size ≥ 100 px², excluding edge particles)

&#x20; → crop each ROI from the color original

&#x20; → save as PNG

```

&#x20;

Output files are named `<original\_filename>\_ROI\_001.png`, `\_ROI\_002.png`, etc.

A per-image summary is printed to the ImageJ log.

