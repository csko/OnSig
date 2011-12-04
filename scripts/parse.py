#!/usr/bin/env python2

import os
from numpy import array, append

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
  global_data = makeglobal(dataset, datafile, data)

  writelocal(dataset, datafile, data)

  return global_data


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
  features = {}

  minx = maxx = miny = maxy = None
  xvelsum = 0.0
  posxvel = 0
  pendownnum = 0

  for i, v in enumerate(data):
    x = v[0]
    y = v[1]
    z = v[2]

    xvel = v[3]
    yvel = v[4]
    zvel = v[5]

    xvelsum += xvel
    if xvel > 0:
      posxvel += 1

    pendown = True if z > 0 else False
    if pendown:
      pendownnum += 1

    if not minx or minx > x:
      minx = x
    if not maxx or maxx < x:
      maxx = x
    if not miny or miny > y:
      miny = y
    if not maxy or maxy < y:
      maxy = y

  features['00num_samples'] = len(data)
  features['01height'] = maxy-miny
  features['02width'] = maxx-minx
  features['03samples/width'] = 1.0 * features['00num_samples'] / features['02width']

  features['09avg_x_vel'] = xvelsum / features['00num_samples']
  features['09var_x_vel'] = xvelsum / features['00num_samples']
  features['11num_pos_x_vel'] = posxvel
  features['14pen_down_samples'] = pendownnum

  return ([k[2:] for k in sorted(features.keys())], [v for k, v in sorted(features.items())])

def writeglobal(dataset, data):

  print "Writing local global features for the %s dataset." % (dataset)

  outfname = SPATH + dataset + "/global.data"
  with open(outfname, "w") as f:
    header = False
    for k, v in data.items():
      if not header:
        print >>f, "# name", " ".join(v[0])
        header = True
      print >>f, k, " ".join([str(x) for x in v[1]])

def writelocal(dataset, datafile, data):

  outfname = SPATH + dataset + "/" + datafile

  print "Writing local feature files into %s/%s." % (dataset, datafile)

  with open(outfname, "w") as f:
    for i, v in enumerate(data):
      print >>f, " ".join([str(x) for x in v])

if __name__ == "__main__":
  for dataset in os.listdir(F):
    datadir = F + dataset + "/"
    global_data = {}
    if not os.path.exists(SPATH + dataset):
      os.mkdir(SPATH + dataset)
    for datafile in os.listdir(datadir):
      fname = datadir + datafile
      global_data[datafile] = makefile(dataset, datafile, fname)
#      break
    writeglobal(dataset, global_data)
#    break
