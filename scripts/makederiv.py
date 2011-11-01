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

  outfname = SPATH + dataset + "/" + datafile

  print "Writing derivatives into %s/%s." % (dataset, datafile)
  with open(outfname, "w") as f:
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

      print >>f, " ".join([str(x) for x in list(v) + list(d) + list(d2) + list(d3)])

for dataset in os.listdir(F):
  datadir = F + dataset + "/"
  if not os.path.exists(SPATH + dataset):
    os.mkdir(SPATH + dataset)
  for datafile in os.listdir(datadir):
    fname = datadir + datafile
    makefile(dataset, datafile, fname)
#    break
#  break
