# Project 07 — Morphological Operations

An ImageJ plugin applying binary morphological operations using a cross-shaped (plus) structuring element. Both operations are implemented from scratch.

## Operations

**Outline Extraction** computes the set difference between the original image and its erosion:

```
Outline(A) = A - erode(A)
```

The result isolates the object boundary — pixels that would be consumed by a single erosion step.

**Skeletonization** computes the morphological skeleton via iterative erosion and opening:

```
S(A) = union_k { A_k - open(A_k) },  k = 0, 1, 2, ...
```

where `A_k` is the k-th erosion of A and `open = erode then dilate`. The loop runs until the eroded image is completely empty. The result is a one-pixel-wide medial axis representation of the original shape.

