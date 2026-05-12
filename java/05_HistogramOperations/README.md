\# Project 05 — Histogram Operations

&#x20;

An ImageJ plugin that applies one of two histogram-based contrast enhancement

techniques to an 8-bit grayscale image, then displays before/after histogram plots.

&#x20;

\---

&#x20;

\## Methods

&#x20;

\*\*Histogram Expansion\*\* linearly stretches the image's actual intensity range

\[min, max] to the full \[0, 255] range:

&#x20;

```

new\_value = (old\_value - min) / (max - min) \* 255

```

&#x20;

Effective when the image uses only a narrow band of intensities

(e.g., underexposed or low-contrast acquisitions). If the image is perfectly

uniform, expansion is undefined and no change is applied.

&#x20;

\*\*Histogram Equalization\*\* redistributes intensities so that the output histogram

is approximately uniform, using the cumulative distribution function (CDF):

&#x20;

```

new\_value = round(CDF(old\_value) \* 255)

```

&#x20;

Effective when intensity is unevenly concentrated in a narrow range. Produces

higher global contrast at the cost of potentially introducing artifacts in

already well-distributed images.

