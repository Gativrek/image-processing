\# Project 04 — Live Image Adjustments

&#x20;

An ImageJ plugin that applies four point-to-point operations to an RGB image

with live preview — the image updates on every slider change without closing

the dialog. Canceling the dialog restores the original image.

&#x20;

\---

&#x20;

\## Operations

&#x20;

Operations are applied in sequence on each slider change, always starting

from the preserved original:

&#x20;

| Slider | Range | Neutral | Effect |

|---|---|---|---|

| Brightness | -100 to +100 | 0 | Adds a fixed offset to each channel; clamps to \[0, 255] |

| Contrast | -100 to +100 | 0 | Scales each channel's deviation from mid-gray (128) using the factor `F = (259(C+255)) / (255(259-C))` |

| Solarization | 1 to 255 | 255 | Inverts each channel independently if its value exceeds the threshold; lower values affect more pixels |

| Desaturation | 0 to 100 | 0 | Linearly interpolates each pixel toward its BT.601 grayscale equivalent; 100 = fully grayscale |

&#x20;

\---

