#!/usr/bin/env python2

import os
from numpy import array, append
from collections import defaultdict

# Data path
F = "../data/"

# Save path
SPATH="../data-deriv/"

def makefile(dataset, datafile, fname):
  origdata = []
  data = []

  with open(fname) as f:
    for line in f:
      w = line.split()

      x = int(w[0])
      y = int(w[1])
      z = int(w[2])

      origdata.append(array([x, y, z]))

  n = len(origdata)

  # Normalize into mass center.
  c = [0, 0, 0]

  # Calculate the mass center
  for v in origdata:
    c[0] += v[0]
    c[1] += v[1]
  c[0] /= 1.0 * n
  c[1] /= 1.0 * n

  # Shift every data point.
  for i, v in enumerate(origdata):
    origdata[i] -= array(c)

  data = makelocal(dataset, datafile, origdata)
  globaldata = makeglobal(dataset, datafile, data)

  writelocal(dataset, datafile, globaldata)
#  writeglobal(dataset, datafile, data)

def makelocal(dataset, datafile, origdata):
  data = []

  n = len(origdata)

  # Derivatives.
  for i, v in enumerate(origdata):
    if i < 1 or i >= n - 1:
      d = v
      d2 = v
    else:
      d = origdata[i+1] - origdata[i-1]
      d2 = origdata[i+1] - 2 * origdata[i] + origdata[i-1]

    if i < 2 or i >= n - 2:
      d3 = v
    else:
      d3 = (- origdata[i-2] + 2 * origdata[i-1] - 2 * origdata[i+1] + origdata[i+2]) / 2
    data.append(list(v) + list(d) + list(d2) + list(d3))
  return data

def makeglobal(dataset, datafile, data):
  features = defaultdict(int)
  return data

def writeglobal(dataset, datafile, data):
#  outfname = SPATH + dataset + "/" + datafile
  print "Writing local global features for signature %s/%s." % (dataset, datafile)

def writelocal(dataset, datafile, data):

  outfname = SPATH + dataset + "/" + datafile

  print "Writing local feature files into %s/%s." % (dataset, datafile)

  with open(outfname, "w") as f:
    for i, v in enumerate(data):
      print >>f, " ".join([str(x) for x in v])

for dataset in os.listdir(F):
  datadir = F + dataset + "/"
  if not os.path.exists(SPATH + dataset):
    os.mkdir(SPATH + dataset)
  for datafile in os.listdir(datadir):
    fname = datadir + datafile
    makefile(dataset, datafile, fname)
#    break
#  break