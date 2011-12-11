#!/bin/bash

export FNAME=$1
export OUTNAME=$2

gnuplot << gptend
set encoding utf8
set term png large nocrop enhanced 14 size 1280,1024
set output "$OUTNAME"

set style line 1 lt 1 lw 1 pt 3 lc rgb "red"
set style line 2 lt 1 lw 1 pt 3 lc rgb "blue"
set style line 3 lt 3 lw 1 pt 3 lc rgb "green"
set style line 4 lt 3 lw 1 pt 3 lc rgb "orange"

#set logscale x
set xrange [*:*]
set yrange [*:*]

set title "$FNAME"

set ylabel "True Positive Rate"
set xlabel "False Positive Rate"

plot "$FNAME" using 3:2 with lines t 'Anomaly detection (global)'
gptend
