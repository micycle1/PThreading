# PThreading
An easy multithreading drawing framework for Processing.
A framework to allow multithreaded drawing in Processing.

## Motivation

Natively, Processing runs on a single thread -- the *draw()* loop.

Processing does indeed provide a quick and dirty way to implement a simple thread with its *thread()* method. Though as the [docs](https://processing.org/reference/thread_.html) describe...

> You cannot draw to the screen from a function called by thread(). Because it runs independently, the code will not be synchronized to the animation thread, causing strange or at least inconsistent results.

... leaving *thread()* suitable only for CPU-bound sketches (since calculation can be done in *thread()* so that any code in the *draw()* loop can run without waiting).

However, support for threading in the context of draw-call-heavy sketches k

For most sketches, this is ok, but draw-heavy sketches will experience slowdown when rasterization cannot keep up with the target framerate.

Processing's thread() is suitable for CPU heavy. THen update state (like physics sims).

## Solution

Drawing into `PGraphics` objects is thread-safe -- `PThreading` simplifies this process.

## Useage

### What about keyboard/mouse?
Override 

## Limitations

The `PGraphics` object that each thread draws into uses the `Java2D` renderer, since this is the only renderer that can be drawn into using other threads.
As a consequence you cannot **thread** `OPENGL` sketches, or sketches that require 3D draw functions (your main sketch can still use, but all thread drawing must be 2D).