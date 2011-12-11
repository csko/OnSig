#!/usr/bin/env python2

# Anomaly detection based classifier.
# Computes a unimodal Gaussian fit for each global attribute. then it
# decides if the test signature is an outlier.

import sys
from numpy import array, pi, exp, sqrt
from math import isnan, log

from parse import *

def fit_Gaussian(data):
  """Calculates mu = sum(x) / length(x), sigma2 = (x - mu)' * (x-mu) / length(x)"""

  x = array(data)
  m = len(x)

  mu = 1.0 * (sum(x)) / m
  sigma2 = 1.0 * sum((x-mu)**2) / m
  # TODO: if sigma2 == 0.0, error.

  return mu, sigma2

def p(x, mu, sigma2):
  """
  Compute the euclidean distance as in
  The State of the Art in On-line Handwritten Signature Verification.
  """

  x = array(x)

  res = 1.0 / len(x) * sqrt(sum((x-mu)**2 / sigma2))
  return res

def anomaly_classify(test_global, train_global, eps):

  # Compute Gaussian parameters.
  mu, sigma2 = fit_Gaussian(train_global)

  # Compute Euclidean distance.
  px = p(test_global, mu, sigma2)

  # Anomaly (reject) iff p(x) >= eps.
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
