# TritechAcquisitionPlugin

This is the PAMGuard Tritech acquisition plugin which allows PAMGuard to 
1) Acquire data directly from Tritech Gemini sonars
2) Run track detectors in real time on the sonar data
3) Process data from Tritech GLF data files offline
4) View and annotate detection data using the PAMGuard viewer. 


## Installation
1) Ensure that you have an up to date PAMGuard installation (v.2.02.13 or later)
2) locate the PAMGuard installation directory (probably C:\Program Files\Pamguard\)
3) copy the two folders 'plugins' and 'lib64' into the folder

Any previous versions of the plugin in the plugins folder (e.g. TritechAcquisitionV1_27.jar)
should be deleted. 

The lib64 folder will already exist in your PAMGuard installation folder, so you
are effectively just adding the additional library files to the installation. The 
plugins folder may or may not exist, depending on whether or not you have existing 
plugins (for a standard installation the folder will not exist). Even if you have 
a previous installation of the Tritech lib64 files, copy the release files over 
the existing ones, since they may contain important updates.  

## Limitations
This version of the software correctly synchronizes pulses from multiple sonars
so is suitable for multi-sonar use. Note however, that this has only been tested in 
small tanks, so I strongly suggest that you use the frame rate display and keep an 
eye on performance.

Latest Features and bug fixes

**V1.43**

Larger field for text in time tail dialog.

Improve data loading speed in viewer mode. 

**V1.41**

Offline display improvements, so that most display of detections will now work 
even if raw sonar image data (glf files) is not available. Note however, that
in it's current form you are strongly advised to keep raw glf data. 

Significant speed improvements in image rendering (conversion from rectangular data
to fan image).

Detector improvements: Filtering in range for the detector, and setting maximum track
gaps in frames as well as in time. 

**V1.27**

Fixed display of spatial vetos on sonar image.

Added option to colour tracks sequentially from blue to red (so you can easily see
direction of travel).

## Files
/plugins/TritechAcquisitionV1_27.jar is a pure Java PAMGuard plugin that
adds new modules and functionality to the PAMGuard software

/lib64/GENESISJAVAINTERFACE.dll is a Windows C library developed by D. Gillespie
which wraps the Tritech Gemini software development kit (SDK(=), making the
functions in the SDK available to Java (the language used for PAMGuard)

lib64/Svs5SeqLib.dll, GenesisSerializer.dll, GeminiComms.dll are libraries developed
by Tritech which form part of their SDK. 

Other files in the lib64 folder are other standard Windows libraries required for 
the operation of the Tritech SDK and hence the PAMGuard plugin. 

Note that all the files in lib64 are only required if you wish to acquire and process
Tritech data in real time. If you are only processing Gemini files offline then you 
should only need the PAMGuard plugin. 


## Building and Source code

The code should be built as a PAMGuard plugin, which can be added to a standard PAMGuard installation. For development though it's much easier to work with the code in an Ecliplse workspace containing this code, the TritechFiles and GenesisJavaJNAInterface projects and the main PAMGuard project. You can then run, change and debug all four projects at the same time.

Additional information on building PAMGuard plugins is available at http://www.pamguard.org/16_HowtomakePlug-Ins.html

Source code is split across four different repositories. All are available under
[the GPL3 license](https://www.gnu.org/licenses/gpl-3.0.en.html).

[https://github.com/douggillespie/TritechAcquisitionPlugin](https://github.com/douggillespie/TritechAcquisitionPlugin)
contains the code for making the Plugin. This is dependent on PAMGuard classes
for data management, display, etc, so requires PAMGuard to build and run. It also 
requires TritechFiles and

[https://github.com/douggillespie/TritechFiles](https://github.com/douggillespie/TritechFiles)
is a pure Java file reader for Tritech
GLF and ECD files. It can be built for earlier Java versions (e.g. Java 8) and does
not require PAMGuard, so can be used to import Gemini data into other Java programmes
or Matlab.

[https://github.com/douggillespie/GenesisJavaJNAInterface](https://github.com/douggillespie/GenesisJavaJNAInterface)
is a JNA interface to the 
C functions in the GENESISJAVAINTERFACE.dll. It does not require PAMGuard, so can 
be used to import Gemini data into other Java programmes.

[https://github.com/douggillespie/GenesisJavaCInterface](https://github.com/douggillespie/GenesisJavaCInterface) is the C/C++ code that acts
as an interface between the Java code in GenesisJavaJNAInterface and the Tritech
SDK libraries. 

The main repository for PAMGuard source code is at 
[https://github.com/PAMGuard/PAMGuard](https://github.com/PAMGuard/PAMGuard)
