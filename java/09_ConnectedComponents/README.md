\# Project 09 — Connected Components Labeling

&#x20;

An ImageJ plugin that detects and labels connected regions (components) in an

image using 8-connectivity breadth-first search (BFS). Each component is

rendered at a distinct grayscale level for visual identification.

&#x20;

\---

&#x20;

\## Overview

&#x20;

The plugin duplicates the input image, converts it to 8-bit grayscale, and

applies automatic Otsu thresholding to produce a binary image. It then scans

every foreground pixel and, for each unlabeled one, runs a BFS flood-fill to

assign a unique integer label to all 8-connected neighbors. The labeled map

is rendered into a grayscale output image where each component's intensity is

linearly scaled across the range \[50, 250].

&#x20;

The original image is never modified. The component count is printed to the

ImageJ log.

