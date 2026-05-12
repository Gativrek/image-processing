\# Project 06 — Spatial Filters (Linear and Non-Linear)

&#x20;

Two ImageJ plugins implementing a set of 3x3 spatial filters from scratch,

without relying on ImageJ's built-in convolution.

&#x20;

\---

&#x20;

\## SpatialFilters.java — Linear Filters

&#x20;

Applies one of three linear 3x3 filters via custom convolution.

Works on both grayscale and RGB images; for RGB, each channel is convolved independently.

The result is shown in a new window, leaving the original unchanged.

&#x20;

| Filter | Kernel | Effect |

|---|---|---|

| Low-Pass (Mean) | All entries = 1/9 | Smoothing via neighborhood average |

| High-Pass (Sharpen) | Center = 9, surroundings = -1 | Edge emphasis via Laplacian boost |

| Border Detection (South) | See code | Detects horizontal edges brighter toward the south |

&#x20;

\*\*Note on border pixels:\*\* out-of-bounds taps are skipped rather than padded.

For the mean filter this causes a subtle darkening ring at the image boundary.

&#x20;

\---

&#x20;

\## NonLinearFilters.java — Non-Linear Filters

&#x20;

Applies one of two non-linear filters.

&#x20;

\*\*Sobel\*\* (any image type): computes the horizontal gradient Gx and vertical

gradient Gy via the Sobel kernels, then derives the gradient magnitude

`|G| = sqrt(Gx² + Gy²)`. Three float images are displayed: Gx, Gy, and

magnitude. ImageJ auto-scales float images on display.

&#x20;

\*\*Median\*\* (GRAY8 only): replaces each pixel with the median of its 3x3

neighborhood, which suppresses salt-and-pepper noise while preserving edges

better than a mean filter. The 1-pixel image border is left unchanged.

