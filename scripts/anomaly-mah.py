#!/usr/bin/env python2

# Anomaly detection based classifier.
# Computes a unimodal Gaussian fit for each global attribute. then it
# decides if the test signature is an outlier.

import sys
from numpy import array, pi, exp, sqrt, cov, transpose, mean, dot
from math import isnan, log

from parse import *

def mahalanobis(x, data):
  """Calculates the Mahalanobis distance."""

  x = array(x)
  data = array(data)

  S = cov(data, rowvar = 0) # Covariance matrix of the training data.
  means = mean(data, axis=0) # Means of the training data.

  res = dot(dot((x-means), S**-1), (x-means).T) # Squared Mahalanobis distance.
  print res

  return res

def anomaly_classify(test_global, train_global, eps):

  # Compute Mahalanobis distance.
  px = mahalanobis(test_global, train_global)

  # Anomaly (reject) iff p(x) < eps.
  return True if px < eps else False

if __name__ == "__main__":
  if len(sys.argv) < 4:
    print "Usage: %s EPS TEST TRAIN1 [TRAIN2 [TRAIN3 ...]]" % sys.argv[0]
    quit()

  eps = float(sys.argv[1])

  #local, test_global = makedata('?', sys.argv[2], sys.argv[2]) # non-cached
  test_global = makedata_cached('?', sys.argv[2], sys.argv[2], ['global']) # cached

  test_global = test_global[1]
  refs = []

  for i in range(3, len(sys.argv)):
#    local, glob = makedata('genuine', sys.argv[i], sys.argv[i]) # non-cached

    glob = makedata_cached('genuine', sys.argv[i], sys.argv[i], ['global']) # cached
    glob = glob[1]
    refs.append(glob)

  # Write 1 if accepted, 0 otherwise.
  with open("output.txt", "w") as f:
    print >>f, 1 if anomaly_classify(test_global, refs, eps) else 0
