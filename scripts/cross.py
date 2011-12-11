#!/usr/bin/env python2

# Cross validation evaluation framework.
# Computes the FAR, FRR, ROC curves and the EER value.

import sys
import os
import subprocess

from collections import defaultdict
from math import log

DATADIR = "../data/"

EERFILE="eer.txt"
ROCFILE="roc.txt"

# Available classifiers.
methods = {'anomaly': 'python2 anomaly.py %s %s %s'}

# Parameters to try.
params  = {'anomaly': ['1e-30', '1e-40', '1e-50', '1e-60']}

def find_genuine():
  """Returns genuine signature file names grouped by the author."""
  dataset = "genuine/"

  res = defaultdict(list)

  for fname in os.listdir(DATADIR + dataset):
    # Author is the first 3 characters.
    author = fname[0:3]
    res[author].append(DATADIR + dataset + fname)

  return res

def find_forgery():
  """
  Returns forgery signature file names grouped by the corresponding
  genuine author name.
  """
  res = defaultdict(list)

  dataset = "forgery/"
  for fname in os.listdir(DATADIR + dataset):
    # Target author is the second number.
    author = fname[4:7]
    res[author].append(DATADIR + dataset + fname)

  return res

def simple_test(gen_list, for_list, method, eps, handlers):
  """
  Evaluate for a test set using a reference training set.
  Uses the whole forgery database and does leave-one-out when working 
  out the FRR.
  This method writes the results to eer and roc file handlers for later
  use.
  """

  far = 0
  far_n = 0

  # True Positive, False Positive, True Negative, False Negative
  tp = fp = tn = fn = 0

  # For each author.
  for author, gens in gen_list.items():
    # For each forgery test subject.
    for test in for_list[author]:
      # Evaluate using the specified method.
      cmd = method % (eps, test, ' '.join(gens))
#      print cmd
      subprocess.call(cmd.split(" "))

      # Read results.
      result = int(open("output.txt").read(1))
      # 1 iff accepted; these should not be accepted
      if result == 1:
        fp += 1
      else:
        tn += 1
#      break
#    break

  frr = 0
  frr_n = 0

  # For each author.
  for author, gens in gen_list.items():
    # For each genuine signature, train using leave-one-out.
    for i, sig in enumerate(gens):
      test = sig
      train = gens[0:i] + gens[i+1:]

      # Evaluate using the specified method.
      cmd = method % (eps, test, ' '.join(train))
#      print cmd
      subprocess.call(cmd.split(" "))

      # Read results.
      result = int(open("output.txt").read(1))
      # 1 iff accepted; these should be accepted
      if result == 0:
        fn += 1
      else:
        tp += 1
#      break
#    break

  far = 1.0 * fp / (fp + tn)
  frr = 1.0 * fn / (fn + tp)

  eertxt = handlers[0]
  roctxt = handlers[1]

  print >>eertxt, "%f %f %f" % (log(float(eps)), far, frr)
  print >>roctxt, "%f %f %f" % (log(float(eps)), tp / (tp + fn), fp / (tn + fp))

  eertxt.flush()
  roctxt.flush()

if __name__ == "__main__":
  gen_list = find_genuine()
  authors = gen_list.keys()

  for_list = find_forgery()
  assert(sorted(for_list.keys()) == sorted(authors)) # Authors are the same.

  method = 'anomaly'

  # Open log files.
  eertxt = open(EERFILE, "w")
  roctxt = open(ROCFILE, "w")

  handlers = (eertxt, roctxt)

  # Write headers.
  print >>eertxt, '# log(eps) FAR FRR'
  print >>roctxt, '# log(eps) TPR FPR'

  # Try for each parameter and write the resulting data points.
  for eps in params[method]:
    simple_test(gen_list, for_list, methods[method], eps, handlers)

  # Close log files.
  eertxt.close()
  roctxt.close()
