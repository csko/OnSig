#!/usr/bin/env python2

# This file is used to parse the original data files and create 
# a derived local format along with global features.

import os
from numpy import array, append

# Data files in the original format are in this directory.
F = "../data/"

# Save path.
SPATH="../data-deriv/"

def load_file(fname):
  """Loads an x,y,z-file and returns a list of tuples."""
  origdata = []
  with open(fname) as f:
    for line in f:
      w = line.split()

      x = int(w[0])
      y = int(w[1])
      z = int(w[2])

      origdata.append(array([x, y, z]))
  return origdata

def makedata_cached(dataset, datafile, fname, types=['global', 'local']):
  """
  Similar to makedata() except it tries to load global attributes
  from precomputed files created by parse.py's main().
  """

  if 'global' in types:
    p = SPATH + dataset + "/global.data"

    if not os.path.exists(p): # No database exists.
      local_data, global_data = makedata(dataset, datafile, fname)
    else:
      # Strip directories.
      idx = fname.split("/")[-1]
      data = None
      with open(p) as f:
        header = f.readline()[:-1].split() # not completely correct header
        for line in f:
          w = line[:-1].split()
          if w[0] == idx: # Found the file in the database.
            data = [float(x) for x in w[1:]] # No name column, convert to float.
            break

      if data == None: # Not found.
        local_data, global_data = makedata(dataset, datafile, fname)
      else:
        global_data = (header, data)

  if 'local' in types:
    pass # Not implemented yet.

  if 'global' in types:
    if 'local' in types:
      return global_data, local_data
    else:
      return global_data
  else:
    if 'local' in types:
      return local_data
    else:
      return None

def makedata(dataset, datafile, fname):
  """
  Normalizes into mass center and initiates local and global feature
  extraction.
  """

  data = []

  origdata = load_file(fname)
  n = len(origdata)

  # Normalize into mass center.
  c = [0, 0, 0]

  # Calculate the mass center.
  for v in origdata:
    c[0] += v[0]
    c[1] += v[1]
    # Don't use z.

  c[0] /= 1.0 * n
  c[1] /= 1.0 * n

  # Shift every data point.
  for i, v in enumerate(origdata):
    origdata[i] -= array(c)

  local_data = makelocal(dataset, datafile, origdata)
  global_data = makeglobal(dataset, datafile, local_data)

  return local_data, global_data

def makelocal(dataset, datafile, origdata):
  """Differentiates x,y,z local features up to 3 times."""
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
  """
  Creates some global features and returns an index header list along with
  the data computed.
  The features mostly come from
  Jonas Richiardi, Hamed Ketabdar, Andrzej Drygajlo:
  Local and global feature selection for on-line signature verification,
  In Proc. IAPR 8th International Conference on Document Analysis and
    Recognition (ICDAR 2005).
  """
  features = {}

  minx = maxx = miny = maxy = maxz = minz = max_dx = max_dy = None
  xvelsum = 0.0
  posxvel = 0.0
  pres_sum = 0.0
  dx_sum = 0.0
  xacc_sum = 0.0
  ddy_sum = 0.0

  pendownnum = 0.0
  num_neg_vel = 0.0
  num_pos_vel = 0.0

  for i, v in enumerate(data):
    x = v[0]
    y = v[1]
    z = v[2]

    xvel = v[3]
    yvel = v[4]
    zvel = v[5]

    xacc = v[6]
    yacc = v[7]
    zacc = v[8]

    xvelsum += xvel
    if xvel > 0:
      posxvel += 1

    pendown = True if z > 0 else False
    if pendown:
      pendownnum += 1

    if xvel < 0 or yvel < 0:
      num_neg_vel += 1

    if xvel > 0 or yvel > 0:
      num_pos_vel += 1

    pres_sum += z
    dx_sum += xvel
    xacc_sum += xacc
    ddy_sum += yacc

    if not minx or minx > x:
      minx = x
    if not maxx or maxx < x:
      maxx = x
    if not miny or miny > y:
      miny = y
    if not maxy or maxy < y:
      maxy = y
    if not maxz or maxz < z:
      max_press_point = (x,y)
      maxz = z
    if not minz or minz > z:
      minz = z
    if not max_dx or max_dx < xvel:
      max_dx = xvel
    if not max_dy or max_dy < yvel:
      max_dy = yvel

  features['00num_samples'] = len(data)
  features['01height'] = maxy-miny
  features['02width'] = maxx-minx
  features['03samples/width'] = 1.0 * features['00num_samples'] / features['02width']

  features['09avg_x_vel'] = xvelsum / features['00num_samples']
  features['09var_x_vel'] = xvelsum / features['00num_samples']
  features['11num_pos_x_vel'] = posxvel
  features['14pen_down_samples'] = pendownnum
  features['20avg_pressure'] = pres_sum / features['00num_samples']
  features['21max_pressure'] = maxz
  features['22max_pressure_x'] = max_press_point[0]
  features['22max_pressure_y'] = max_press_point[1]
  features['28pressure_range'] = maxz - minz
  features['29max_dx'] = max_dx
  features['30avg_ddx'] = xacc_sum / features['00num_samples']
  features['31max_dy'] = max_dy
  features['32avg_ddy'] = ddy_sum / features['00num_samples']
#  features['33var_pressure'] = 0.0
  features['34num_neg_vel/Td'] = num_neg_vel / features['14pen_down_samples']
  features['37num_pos_vel/Td'] = num_pos_vel / features['14pen_down_samples']

  return ([k[2:] for k in sorted(features.keys())], [v for k, v in sorted(features.items())])

def writeglobal(dataset, data):
  """
  Writes the global features into a file called global.data.
  """

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
  """
  Writes the local features into a file called local.data.
  """

  outfname = SPATH + dataset + "/" + datafile

  print "Writing local feature files into %s/%s." % (dataset, datafile)

  with open(outfname, "w") as f:
    for i, v in enumerate(data):
      print >>f, " ".join([str(x) for x in v])

if __name__ == "__main__":
  # dataset in ['genuine', 'forgery']
  for dataset in os.listdir(F):
    datadir = F + dataset + "/"
    global_data = {}
    # We'll need this directory for results, create it if it does not exists.
    if not os.path.exists(SPATH + dataset):
      os.mkdir(SPATH + dataset)
    # For each file in the dataset.
    for datafile in os.listdir(datadir):
      fname = datadir + datafile
      # Gather local and global features. Collect global features grouped by file name.
      local_data, global_data[datafile] = makedata(dataset, datafile, fname)
      writelocal(dataset, datafile, local_data)
#      break
    # When finished loading the database, write the global features into a file.
    writeglobal(dataset, global_data)
#    break
