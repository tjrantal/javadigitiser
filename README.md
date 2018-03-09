Work in progress, does not do anything useful at the moment.


This Java Digitiser is a tool to manually digitise markers (computer-assisted colour-based tracking) from a video for [motion analysis purposes] (https://www.researchgate.net/post/How_to_analyze_motion_in_3D_with_standard_video_recordings). Uses [JCodec] (http://jcodec.org/) to read the video, and therefore only supports a few [codecs] (http://jcodec.org/) and only in an .MP4 video container. I wrote the digitiser for video from my Android phone, which happened to be compatible with [JCodec] (http://jcodec.org/). There are many other  offerings that provide similar functionality along with many other useful features [e.g. MATLAB tools for digitizing video files and calibrating cameras] (http://www.unc.edu/~thedrick/software1.html), [Kinovea] (http://www.kinovea.org/). I was nevertheless unable to find an open source solution that does exactly what I wanted to do (colour-based tracking is, or at least used to be, on offer in closed source motion analysis software), and hence this re-invention of the wheel.

Use [gradle] (http://gradle.org/) to get the dependensies and to compile the project, i.e. on command line; gradle build 

Requires [openjfx] (http://openjdk.java.net/projects/openjfx/) to run on linux (e.g. on Ubuntu: sudo aptitude install openjfx)

Written by Timo Rantalainen tjrantal at gmail dot com 2018. Released to the public domain with the creative commons [CC0] (http://creativecommons.org/publicdomain/zero/1.0/) licence.
