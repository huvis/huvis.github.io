# huvis - PCT 

This package implements the Point Cloud Tool (PCT) that can be used to
reconstruct 3D scenes from a bunch of pictures of the scene and then use the
result to render the recognized features and/or external objects.

The software builds on the the [NetBeans openide platform](http://platform.netbeans.org).
It can be packaged as
application on any platform that supports NetBeans (development version 7.2.1).
However, it will only be functional in an OS X environment, because it contains
external dependencies that are not easily ported. The software is tested in
OS X versions 10.7 up to 10.8.4.

This repository is source-only, that is, in order to obtain a runnable application
one needs to employ NetBeans. The top-level project resides in the top-level folder
`PCT/`.

# Table of Contents
* [Prerequisites](#prerequisites)
* [Instructions](#instructions)
* [External Dependencies](#external-dependencies)
	* [JAG3D](#jag3d)
	* [Bundler](#bundler)
	* [Povray](#povray)
* [Revision History](#revision-history)
* [License](#license)
* [Known Issues](#known-issues)



# Prerequisites

Two external command line utilities need to be installed in order to use the functionality
of PCT:
* `ant` (Java based build tool; tested with version 1.8.2)
* `povray` (open source renderer tool; tested with version 1.6.1)


When going through the process of reconstructing a scene for the first time,
as briefly described below, you will probably run
into the issue that PCT does not know where those external dependencies are.
A file chooser dialogue will open and ask you to locate each in turn.
The former is usually preinstalled in Mac OS X; the
executable resides in `/usr/bin/`. The latter has to be manually installed; more detailed
instructions are included below in the item [Povray](#povray).


# Instructions


There is no user manual for the software yet. Therefore, let us summarize the most
important points.

1. Take a series of pictures that capture the 3D features of a scene. Usually it is
sufficient to take at least 3 or 4 pictures from a slightly varying perspective.

2. Create a new project in PCT and place the picture files in the folder `input/`.
The input files will appear in PCT. The files need to have the `.jpg` extention
(lower case!).

3. From now on you can use the project view in PCT to develop the scene, that is
generate the reconstruction and renderings. This is usually done by right-clicking
on a certain node and then choosing the appropriate action.

4. It is also possible to include objects to be included in the final renderings, e.g.,
the building to be built on the construction site. This data has to be present as
povray render directives. The points can be in an arbitrary coordinate system, but then
one has to provide a file called `transformation.jag3d` in the input folder. These
additional input files have to have the file ending `.inc` and reside in the input folder.

The colors of the nodes in the project view indicates the availability and/or status
of an item. If a node is red, it cannot be developed because some prerequisite is missing.
If a node is yellow, it is ready for development (but has not been developed yet). If
a node is green, it is fully developed. Occasionally, if a node turns orange, that means
the node contains a valid development, but this is not in accordance with previous data
in the development tree.

The `Transformation` node takes a special role. Contrary to the other nodes in the development
tree, it may or may not be developed for later nodes to be functional.
Developing it means to look for the file
`transformation.jag3d` in the input folder. If found, this files defines the coordinate
transformation between the additional input files (`*.inc`) and the reconstruction
obtained from the input pictures.



# External Dependencies


## Jag3D

As described on the [JAG3D project page](http://derletztekick.com/software/netzausgleichung)
this is a tool for coordinate transformations and geodesic analysis. The source code
can be obtained at 
[sourceforge](http://javagraticule3d.sourceforge.net), the application is available as
a self-contained .jar file for convenience. In oder to obtain a transformation file
suitable for the PCT process, follow these steps:

1. Create two text files, containing points from the source and the destination likewise.
The format should be a series of lines of the form `n x y z`, where `n` is the number
of the point, and `x`, `y`, and `z` are the coordinates. There correspondences have to
be manually extracted from the file `bundler/output/bundle.out` and the `.inc` file.

2. Start the JAG3D application. Choose the menu item `Module->Coordinate transformation`.
A new window appears. Load source and target points by selecting the files you created
through the menu `file->source/target system file`.

3. Click on `transform`.

4. Save the file as `transofrmation.jag3d` and place it in the folder `input/`.


## Bundler

The software used for the 3D reconstruction is
["Bundler - Structure from Motion"](http://www.cs.cornell.edu/~snavely/bundler/),
written by Noah Snavely. The
[source code](https://github.com/snavely/bundler_sfm)
can be obtained on Github. You do not have to install this software. A usable version
is completely contained within PCT. This version is based on the 
[Mac distribution](http://openendedgroup.com/field/ReconstructionDistribution) provided
by Marc Downie.

## Povray
Povray is the renderer used to render the reconstruction and models.

### Obtaining Povray 3.6.1 on Mac OSX 10.8 (Mountain Lion and later)

For Mountain Lion there is a much easier option. We can use [Homebrew](http://mxcl.github.com/homebrew/), which
is easily installed as described, see the link. Then, simply type the command
`brew install povray` and there you go. Make sure to add
the line `read+write* = /Users/<your_username>` to
`~/.povray/3.6/povray.conf`. That applies to any other directory that you
would like your PCT projects to reside in. Otherwise, Povray and by extension PCT will not have proper access to the data locations in question.

The main issue with Povray and Mountain Lion is that Povray needs to link
against an early version of libpng (1.2) while the newest available as of
writing this is 1.5. Homebrew nicely forces Povray to use its internal libpng
distribution. Just make sure that you have a
recent  system (`brew update`) and a clean health record (`brew doctor`). With a little more effort, you could also do that during manual compile, of course.


### Compiling Povray 3.6.1 on Mac OSX 10.6 (Snow Leopard)


For PCT to work correctly, it needs the command line version of Povray
available. On OSX there might be issues installing Povray. The MacPorts 
installation is broken as of 20121001 and version OSX 10.6 and above. In the following we describe the workaround as found [here](http://news.povray.org/povray.macintosh/thread/%3Cweb.4e4865211f72b142975cc8f30@news.povray.org%3E/), courtesy Dominique.
This is tested on OSX 10.6 and 10.8 and with Povray 3.6.1. Remark: Quotes are included to indicate beginning and end of a command or config sequence; they are
not to be included when typing the commands.

1. Download the official [Povray Unix sources](http://www.povray.org/redirect/www.povray.org/ftp/pub/povray/Official/Unix/povray-3.6.tar.bz2). This link here is version 3.6.1.

2. Unarchive with `tar xjf povray-3.6.tar.bz2` and view the source
directory `povray-3.6.1`.

3. The following three files (all called `configure.ac`) have to be altered:
    * `povray-3.6.1/configure.ac`
    * `povray-3.6.1/libraries/png/configure.ac`
    * `povray-3.6.1/libraries/zlib/configure.ac`. One has to find the `AM_INIT_AUTOMAKE` macros and add the `foreign` keyword. Like this:
    * `AM_INIT_AUTOMAKE([1.7 ...])` -> `AM_INIT_AUTOMAKE([foreign 1.7 ...])`
    
4. Go to the `povray-3.6.1` directory in Terminal and execute the command
 `autoreconf -i`. An updated configure script is created.
 
5. Edit the configure script `povray-3.6.1/configure` to include the line
  `ac_cv_prog_egrep=/usr/bin/egrep` as the first active line, i.e., after all
  the leading comment lines that start with `#`.
  
6. In Terminal, type the command 
    * `CFLAGS="-m32" CXXFLAGS="-m32" ./configure COMPILED_BY="email" --with-x`. Exclude the `--with-x` option if you want to suppress displaying of the generated image in Povray. We actually recommend leaving it out. Otherwise, in PCT, you will always have an X-window pop up when a rendering process is executed.

7.  We should be set now. Type `make`, and if there are no errors, `make install`. The Terminal command `povray` should be available on your system
now.



# Revision History
* Open Source Release Version 1.0, Oktober 2016
	* Netbeans platform application
	* Basic scene reconstruction
	* Manual transformation between models and reconstruction


# License
see LICENSE

# Author
ybrise, Vanamco AG


# Known Issues

* File name that have whitespaces are not handled well. As of now only use filenames
(especially for the input pictures) that do not contain spaces or similar.
* The bundler process might run into issues with permissions. In the bundler output
pane you would see "permission denied" warnings. Note, however, that the build process
still reports "successful", but nothing was done really. As a temporary fix, try deleting
the whole bundler folder in the project main folder. It will be recreated, this time
with the correct permissions hopefully. Sometimes this has to be done twice.
* The mechanism used to watch for file additions/deletions/modifications,
`java.nio.file.WatchService` (new in Java 7) does not have a native OS X implementation
yet. The way it works is by a fallback to the general file system polling service
`sun.nio.fs.PollingWatchService`. As a consequence of this, the watch service is
undesirably slow. A (partial) remedy for the situation would be to have a `NodeListener`
on the GUI nodes that monitor member addition and deletion. It's not trivial to add
a modification event there. We could also do our own (more frequent) polling, but neither
of these two methods seems to be the conceptually clean way.
* When trying to open a project, the File Chooser Dialog sometimes crashes without warning
or exception. If you try again, it usually works right away. (reason unknown)
* The PCT doesn't react well on deleting the output and/or povray folder. They should only
be cleaned with the clean button. In fact, they may be emptied manually, but if they are
deleted, the corresponding nodes don't get hooked to the newly created folders. The problem
corrects itself after a restart of PCT.

