#!/usr/bin/env python2

# Cross validation evaluation framework.
# Computes the FAR, FRR, ROC curves and the EER value.

import sys
import os
import subprocess
from collections import defaultdict

DATADIR = "../data/"

methods = {'anomaly': 'python2 anomaly.py %s %s'}

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

def simple_test(gen_list, for_list, method):
  """Evaluate for a test set using a reference training set."""

  # For each author.
  for author, gens in gen_list.items():
    # TODO
    # For each test subject.
    for test in for_list[author]:
      # Evaluate using the specified method.
      cmd = method % (test, ' '.join(gens))
      print cmd
      subprocess.call(cmd.split(" "))

      # Read results.
      result = int(open("output.txt").read(1))
#      far 

      print result

if __name__ == "__main__":
  gen_list = find_genuine()
  authors = gen_list.keys()

  for_list = find_forgery()
  assert(sorted(for_list.keys()) == sorted(authors)) # Authors are the same.

#  print gen_list
#  print for_list
#  print authors

  simple_test(gen_list, for_list, methods['anomaly'])
