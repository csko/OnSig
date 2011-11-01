import os
import matplotlib
matplotlib.use('Agg') # No X
import matplotlib.pyplot as plt
from numpy import array, append, std, mean

PRESSURE=False

# Data path
F = "../data/"

# Save path
SPATH="../plots/regular/"
#SPATH="/srv/http/onsig2/"

def normalize(x):
  mean = x.mean()
  ran = x.ptp()
  x = 1 + (x - mean) / ran
  return x

def plotfile(dataset, datafile, fname):
  print "Plotting %s/%s." % (dataset, datafile)

  x = array([])
  y = array([])
  z = array([])

  with open(fname) as f:
    for line in f:
      w = line.split()
      x = append(x, int(w[0]))
      y = append(y, int(w[1]))
      z = append(z, int(w[2]))

  plt.xlabel('X')
  plt.ylabel('Y')
  plt.title(dataset + "/" + datafile)
  color = "blue" if dataset == "genuine" else "red"

  if PRESSURE:
    z = normalize(z) * 50
    plt.scatter(x, y, z, c=color)

  plt.plot(x, y, c=color)
  plt.savefig(SPATH + dataset + "/" + datafile + ".png")
  plt.close()


for dataset in os.listdir(F):
  datadir = F + dataset + "/"
  if not os.path.exists(SPATH + dataset):
    os.mkdir(SPATH + dataset)
  for datafile in os.listdir(datadir):
    fname = datadir + datafile
    plotfile(dataset, datafile, fname)
#    break
#  break
