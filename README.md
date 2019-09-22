# PThreading
A framework to allow multithreaded drawing in Processing.

## Motivation

Natively, Processing runs on a single thread -- the *draw()* loop . Any attempts to call Processing drawing functions (like *rect()*) from other threads (by using Processing's thread()) will cause errors and instability. 

Processing does indeed provide an easy way to thread, but this is suited for computationally intensive tasks -- you can compute on other threads but not draw from them, leaving draw-heavy sketches lacking.

For most sketches, this is ok, but draw-heavy sketches will experience slowdown when rasterization cannot keep up with the target framerate.

Processing's thread() is suitable for CPU heavy. THen update state (like physics sims).

## Solution

Drawing into `PGraphics` objects is thread-safe -- `PThreading` simplifies this process.

## Useage
