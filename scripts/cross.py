#!/usr/bin/env python2

# Cross validation evaluation framework.
# Computes the FAR, FRR, ROC curves and the EER value.

import sys
import os
import subprocess

from collections import defaultdict
from math import log

DATADIR = "../data/"

methods = {'anomaly': 'python2 anomaly.py %s %s %s'}
params  = {'anomaly': ['1e-20', '1e-30', '1e-40', '1e-50', '1e-60', '1e-70']}

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

def simple_test(gen_list, for_list, method, eps):
  """Evaluate for a test set using a reference training set."""

  far = 0
  far_n = 0

  # For each author.
  for author, gens in gen_list.items():
    # For each test subject.
    for test in for_list[author]:
      # Evaluate using the specified method.
      cmd = method % (eps, test, ' '.join(gens))
#      print cmd
      subprocess.call(cmd.split(" "))

      # Read results.
      result = int(open("output.txt").read(1))
      if result == 1:
        far += 1
      far_n += 1
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
      if result == 0:
        frr += 1
      frr_n += 1
#      break
#    break

  far = 1.0 * far / far_n
  frr = 1.0 * frr / frr_n
  print "%f %f %f" % (log(float(eps)), far, frr)

if __name__ == "__main__":
  gen_list = find_genuine()
  authors = gen_list.keys()

  for_list = find_forgery()
  assert(sorted(for_list.keys()) == sorted(authors)) # Authors are the same.

  method = 'anomaly'

  print '# log(eps) FAR FRR'
  for eps in params[method]:
    simple_test(gen_list, for_list, methods[method], eps)
