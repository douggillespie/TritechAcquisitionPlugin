# TritechAcquisitionPlugin
Acquisition, display, and analysis of Tritech Gemini sonar data within PAMGuard

The code can be built as a PAMGuard plugin, which can be added to a standard PAMGuard installation. For development though it's much easier to work with the code in an Ecliplse workspace containing this code, the [TritechFiles](https://github.com/douggillespie/TritechFiles) and [GenesisJavaJNAInterface](https://github.com/douggillespie/GenesisJavaJNAInterface) projects and the main PAMGuard project. You can then run, change and debug all four projects at the same time. 

This plugin is dependent on two other repositories [TritechFiles](https://github.com/douggillespie/TritechFiles) and [GenesisJavaJNAInterface](https://github.com/douggillespie/GenesisJavaJNAInterface). The GenesisJavaJNAInterface is in turn dependent on a build of the C++ code in [GenesisJavaCInterface](https://github.com/douggillespie/GenesisJavaCInterface) which itself requires libraries from the Tritech Gemini SDK, available from Tritech on request. 

If you only want to use this for offline analysis of files in PAMGuard, you might get away without having the JNA interface. 
