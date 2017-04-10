General
=======
* replace "/" slashes with platform-independent path separator
* Javadoc skeletons exist, but are not fully complete in all places. Fill in...
* Use databases instead of file based information transfer between stages of the workflow
* clean up mess of Vector3d vs. Point3d... what really is the purpose of the two? 
* Use System.getProperty("line.separator") as defined in PCTSettings for all occurrences
of "\n" etc...
* Make more elaborate status bar (http://ui.netbeans.org/docs/ui/progress_indication/progress_indication_spec.html)


Code design
===========
* The project, the project state and the project properties are pretty entangled, i.e.,
the state needs the properties and vice versa at project-construction-time. We
work around this with the method postInit() of the state, but this is somewhat messy. The
proper way of doing things would be to have a factory method for the whole project, and
only be able to obtain a fully initialized project through this. [not urgent]
* The color coding of the nodes is done with a custom ovserver pattern. In the netbeans
openide realm there exist provisions for so-called "badging" of nodes, which would actually
be well suited for our purposes. It's worth considering a move to this paradigm.
* File handling, and visiting folder structures is handled in several different ways in
the code. It would be nice to have some unified structure in the toolbox. As of now, we
have a swing filter in PCTCustomFileFilter and functionality to walk file trees in
Bundler External. Also, in several places we access files manually, which could also
be integrated in the unified approach.


Project workflow
================
* Stop-build button. Not sure how this can be achieved exactly because most of the work is done
in somewhat lengthy external processes. With some kind of control loop, maybe, that holds
references to all involved sub-threads.
* When checking whether the "clean" project option should be enabled, we have to
consider hidden systems files as well, e.g. .DS_store files. They make it look like
as if the project was not clean.
* If transformation file is modifed while PCT is not running, there's no warning
about changed state, i.e., later bullets will be outdated without notice


Property Persistence
====================
* Date in sample .properties file of empty and of sample project have old date -> use
current date always
* If .properties files are accidentally deleted, this is not recognized as a change by
the project, i.e., they are not automatically regenerated at closing time. Safeguard
against that.
* Make properties loading robust, i.e. regeneration of files if they are missing


Bundler
=======
* Add functionality to scale input pictures (this is necessary for a streamlined workflow
regardless of the input picture quality. Should be easy to implement, because the input
pictures are copied to the bundler folder)
* Add functionality to process only a subset of the input pictures, i.e., add pictures on
the fly. The bundler command line software actually supports such operation, but there is
no interface for this in PCT yet.
* IMPORTANT: rename pictures to lower case .jpg !!! With other capitalizations bundler won't work.
* Refresh camera properties after new computation of bundler.



Image manipulation
==================
* Input pictures are used exactly as they are. There is no rescaling. If the pictures are
very big (resolution) the bundler process might take unduly long, without any benefit
to the quality of the reconstruction. As a matter of fact, the reconstruction sometimes
degrades.
* When generating the final output overlays we use ImageUtilities.mergeImages. This might
not be a very efficient implementation. Possibly we could use some openly available image
manipulation library (especially if we start expanding on the image manipulation
features of PCT).



Logging/GUI
============
* Output is not properly synchronized, i.e., if different threads do output at the same
time, output text might be interleaved. [not urgent; only rare cases]
* Make convenient logger class, so that we dont have to do output manually every time
(i.e. select tab, timestamp, text, etc...)
* More elaborate logging with colors and stuff -> maybe java.util.logger
* Exchange the icons with something meaningful


Nebeans warnings
================
* relative ordering vs. numeric position attributes in layer.xml of povray module


Known Issues
============
* when starting the application from a clean slate (clean&rebuild) the first time one
tries to open a project, the file chooser dialog appears and disappears very quickly. It
looks as if the project type is not recognized because it is not registered. When trying
the second time there is usually no problem.
* permission denied bug when running the bundler process. In a new project, or when the
bundler subdirectory has been deleted, the command line thread sometimes complains about
permission denied issues. The dirt fix is to delete the WHOLE bundler subdirectory, try
again, repeat this process a second time and then it should usually work.
* Sometimes when building the whole project there's a exception:
`java.lang.IllegalStateException: Too many org.netbeans.modules.image.ImageOpenSupport$1$1 in shared RequestProcessor; create your own`
This happens because the opencookies for the newly created renderings and merged pictures
somehow get confused (too much going on while the user is still being prompted for input).
This problem is not tracked down yet, it could be a [Netbeans bug](https://netbeans.org/bugzilla/show_bug.cgi?id=221545)
along the lines of
in which case it will ultimately go away hopefully (when the PCT is compiled with a clean
version of Netbeans)

