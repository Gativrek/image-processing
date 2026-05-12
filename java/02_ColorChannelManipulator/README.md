# Project 02 — RGB Channel Split and Merge

Two complementary ImageJ plugins for manually decomposing an RGB image into its three color channels and reconstructing it from individual channel images.

## Overview

`SplitChannels` takes an RGB image and produces three 8-bit grayscale images, one per channel. `MergeChannels` does the reverse: it takes three 8-bit grayscale images chosen via a dialog and packs them into a single RGB output.

The channel encoding used in both plugins is the standard 24-bit packed integer format: `pixel = (R << 16) | (G << 8) | B`.

