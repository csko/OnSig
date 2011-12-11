#!/usr/bin/env python2

# Prediction script, uses method between algorithms.

import sys
import os
import subprocess

RESULT="output.txt"

# Available classifiers.
methods = {'anomaly': 'python2 anomaly.py %s %s %s'}

# Parameters to use. Assign '' is no parameters.
params  = {'anomaly': '1e-40'}

# Weights of the classifiers.
weights = {'anomaly': 1}

if __name__ == "__main__":
  if len(sys.argv) < 3:
    print "Usage: %s TEST TRAIN1 [TRAIN2 [TRAIN3 ...]]" % sys.argv[0]
    quit()

  test = sys.argv[1]
  train = sys.argv[2:]

  # Call each classifier and assign a weight.
  res = 0.0

  for method, cmd in methods.items():
    cmd = cmd % (params[method], test, ' '.join(train))
#    print cmd
    subprocess.call(cmd.split(" "))

    # Read results.
    result = int(open("output.txt").read(1))

    # Add this to the final result using the weigth assigned.
    res += result * weights[method]
    print method, result

  # Return res >= 0.5 as our final result.
  with open('output.txt', 'w') as f:
    print >>f, '1' if res >= 0.5 else '0'
