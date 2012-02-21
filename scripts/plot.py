#!/usr/bin/env python2

# This file is used for visualizing signatures, mostly for amusement
# and manual inspection.

import os
import matplotlib
matplotlib.use('Agg') # No X
import matplotlib.pyplot as plt
from numpy import array, append, std, mean

# Do we want to show pressure values?
#PRESSURE=False
PRESSURE=True

# Data path
F = "../data/"

# Save path
#SPATH="../plots/regular/"
SPATH="../plots/pressure/"

def normalize(x):
  """Mean normalization of a vector."""
  mean = x.mean()
  ran = x.ptp()
  x = 1 + (x - mean) / ran
  return x

def plotfile(dataset, datafile, fname):
  """Plots a signature."""
  print "Plotting %s/%s." % (dataset, datafile)

  x = array([])
  y = array([])
  z = array([])

  # Gather data into numpy arrays.
  with open(fname) as f:
    for line in f:
      w = line.split()
      x = append(x, int(w[0]))
      y = append(y, int(w[1]))
      z = append(z, int(w[2]))

  # Labels.
  plt.xlabel('X')
  plt.ylabel('Y')
  plt.title(dataset + "/" + datafile)
  # Color, blue for genuine, red for forgery.
  color = "blue" if dataset == "genuine" else "red"

  # Some normalization on the pressure value.
  if PRESSURE:
    z = normalize(z) * 50
    plt.scatter(x, y, z, c=color)

  # Plot and save as a png.
  plt.plot(x, y, c=color)
  plt.savefig(SPATH + dataset + "/" + datafile + ".png")
  plt.close()


# dataset in ['genuine', 'forgery']
for dataset in os.listdir(F):
  datadir = F + dataset + "/"
  # We'll need this directory for results, create it if it does not exists.
  if not os.path.exists(SPATH + dataset):
    os.mkdir(SPATH + dataset)
  # For each file in the dataset.
  for datafile in os.listdir(datadir):
    fname = datadir + datafile
    # Plot the file.
    plotfile(dataset, datafile, fname)
#    break
#  break
