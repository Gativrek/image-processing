# Project 05 — Histogram Operations

An ImageJ plugin that applies one of two histogram-based contrast enhancement techniques to an 8-bit grayscale image, then displays before/after histogram plots.

## Methods

**Histogram Expansion** linearly stretches the image's actual intensity range [min, max] to the full [0, 255] range:

```
new_value = (old_value - min) / (max - min) * 255
```

Effective when the image uses only a narrow band of intensities (e.g., underexposed or low-contrast acquisitions). If the image is perfectly uniform, expansion is undefined and no change is applied.

**Histogram Equalization** redistributes intensities so that the output histogram is approximately uniform, using the cumulative distribution function (CDF):

```
new_value = round(CDF(old_value) * 255)
```

Effective when intensity is unevenly concentrated in a narrow range. Produces higher global contrast at the cost of potentially introducing artifacts in already well-distributed images.

