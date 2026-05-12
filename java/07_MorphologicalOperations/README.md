\# Project 07 — Morphological Operations

&#x20;

An ImageJ plugin applying binary morphological operations using a cross-shaped

(plus) structuring element. Both operations are implemented from scratch.

&#x20;

\---

&#x20;

\## Operations

&#x20;

\*\*Outline Extraction\*\* computes the set difference between the original image

and its erosion:

&#x20;

```

Outline(A) = A - erode(A)

```

&#x20;

The result isolates the object boundary — pixels that would be consumed by

a single erosion step.

&#x20;

\*\*Skeletonization\*\* computes the morphological skeleton via iterative erosion

and opening:

&#x20;

```

S(A) = union\_k { A\_k - open(A\_k) },  k = 0, 1, 2, ...

```

&#x20;

where `A\_k` is the k-th erosion of A and `open = erode then dilate`.

The loop runs until the eroded image is completely empty. The result is a

one-pixel-wide medial axis representation of the original shape.

