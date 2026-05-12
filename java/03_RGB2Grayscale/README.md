\# Project 03a — RGB Color Bar Generator



An ImageJ plugin that produces a synthetic 800x150 px test image containing

eight solid color bars: Red, Green, Blue, Yellow, Cyan, Magenta, White, and Black.



\---



\## Overview



The plugin requires no open image. It constructs the color bars programmatically

using packed 24-bit RGB integers (`pixel = (R << 16) | (G << 8) | B`) and

opens the result in a new window. It is primarily a diagnostic tool for

verifying that color channel encoding, splitting, and merging behave correctly

on known, exact color values.



\---



\# Project 03b — RGB to Grayscale Conversion

&#x20;

An ImageJ plugin that converts an RGB image to 8-bit grayscale using one of

three selectable luminance weighting methods.

&#x20;

\---

&#x20;

\## Overview

&#x20;

Grayscale conversion is not a single standard — the perceived brightness of a

color depends on the display technology. This plugin exposes three approaches:

&#x20;

| Method | Weights (R, G, B) | Standard |

|---|---|---|

| Arithmetic Mean | 1/3, 1/3, 1/3 | — |

| Weighted Luminance (BT.601) | 0.299, 0.587, 0.114 | ITU-R BT.601 (analog / SD TV) |

| Weighted Luminance (BT.709) | 0.2126, 0.7152, 0.0722 | ITU-R BT.709 (HDTV / sRGB) |

&#x20;

The output can either replace the original image or open as a new window,

labeled with a suffix indicating the method used.

&#x20;
