######################################################################################
# Build function for SWIG
######################################################################################

#Required because it has to be turned on in the first place, so if we don't have it,
#just die
FIND_PACKAGE(SWIG REQUIRED)
MESSAGE(STATUS "Building SWIG Bindings")

INCLUDE(${SWIG_USE_FILE})

SET(CMAKE_SWIG_FLAGS "")

SET(JLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE jlibFileMappingDroneCharTelemetryClient_swig.i)
SET_SOURCE_FILES_PROPERTIES(${JLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} PROPERTIES CPLUSPLUS ON)

SET(JLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE jlibFileMappingDroneCharTelemetryServeur_swig.i)
SET_SOURCE_FILES_PROPERTIES(${JLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} PROPERTIES CPLUSPLUS ON)

SET(JLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE jlibVirtualDroneCharTelemetry_swig.i)
SET_SOURCE_FILES_PROPERTIES(${JLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} PROPERTIES CPLUSPLUS ON)

SET(PYLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE pylibFileMappingDroneCharTelemetryClient_swig.i)
SET_SOURCE_FILES_PROPERTIES(${PYLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} PROPERTIES CPLUSPLUS ON)

SET(PYLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE pylibFileMappingDroneCharTelemetryServeur_swig.i)
SET_SOURCE_FILES_PROPERTIES(${PYLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} PROPERTIES CPLUSPLUS ON)

SET(PYLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE pylibVirtualDroneCharTelemetry_swig.i)
SET_SOURCE_FILES_PROPERTIES(${PYLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} PROPERTIES CPLUSPLUS ON)

INCLUDE_DIRECTORIES(../include)

######################################################################################
# Build function for Java library bindings JNIFileMappingDroneCharTelemetryClient
######################################################################################

FIND_PACKAGE(JNI)

# Apparently there's no FOUND variable for JNI. I really hate the lack of consistency 
# in Cmake find modules. >.<
IF(JNI_LIBRARIES)
  MESSAGE(STATUS "- Build SWIG Java library")
  INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})
  SET(ARGS "-includeall" "-module" "JNIFileMappingDroneCharTelemetryClient")

  SET_SOURCE_FILES_PROPERTIES(${JLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_JNI ${LIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_EXE_LINK_LIBS} ${JNI_LIBRARIES})
  SWIG_ADD_MODULE(JNIFileMappingDroneCharTelemetryClient java ${JLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} ../src/cFileMappingDroneCharTelemetryClient.cpp)
  SWIG_LINK_LIBRARIES(JNIFileMappingDroneCharTelemetryClient ${LIBS_JNI})
ELSE(JNI_LIBRARIES)
  MESSAGE(STATUS "--- Cannot make Java bindings, JNI libraries not found!")
ENDIF(JNI_LIBRARIES)


######################################################################################
# Build function for Java library bindings JNIFileMappingDroneCharTelemetryServeur
######################################################################################

FIND_PACKAGE(JNI)

# Apparently there's no FOUND variable for JNI. I really hate the lack of consistency 
# in Cmake find modules. >.<
IF(JNI_LIBRARIES)
  MESSAGE(STATUS "- Build SWIG Java library")
  INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})
  SET(ARGS "-includeall" "-module" "JNIFileMappingDroneCharTelemetryServeur")

  SET_SOURCE_FILES_PROPERTIES(${JLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_JNI ${LIBFILEMAPPINGDRONECHARTELEMETRY_EXE_LINK_LIBS} ${JNI_LIBRARIES})
  SWIG_ADD_MODULE(JNIFileMappingDroneCharTelemetryServeur java ${JLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} ../src/cFileMappingDroneCharTelemetryServeur.cpp)
  SWIG_LINK_LIBRARIES(JNIFileMappingDroneCharTelemetryServeur ${LIBS_JNI})
ELSE(JNI_LIBRARIES)
  MESSAGE(STATUS "--- Cannot make Java bindings, JNI libraries not found!")
ENDIF(JNI_LIBRARIES)

######################################################################################
# Build function for Java library bindings JNIVirtualDroneCharTelemetry
######################################################################################

FIND_PACKAGE(JNI)

# Apparently there's no FOUND variable for JNI. I really hate the lack of consistency 
# in Cmake find modules. >.<
IF(JNI_LIBRARIES)
  MESSAGE(STATUS "- Build SWIG Java library")
  INCLUDE_DIRECTORIES(${JNI_INCLUDE_DIRS})
  SET(ARGS "-includeall" "-module" "JNIVirtualDroneCharTelemetry")

  SET_SOURCE_FILES_PROPERTIES(${JLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_JNI ${LIBVIRTUALTELLOEDUTELEMETRY_EXE_LINK_LIBS} ${JNI_LIBRARIES})
  SWIG_ADD_MODULE(JNIVirtualDroneCharTelemetry java ${JLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} ../src/cVirtualDroneCharTelemetry.cpp)
  SWIG_LINK_LIBRARIES(JNIVirtualDroneCharTelemetry ${LIBS_JNI})
ELSE(JNI_LIBRARIES)
  MESSAGE(STATUS "--- Cannot make Java bindings, JNI libraries not found!")
ENDIF(JNI_LIBRARIES)

#--------------------------------------------------------------------------------------------------------------------------------------------------------------


######################################################################################
# Build function for Python library bindings pyFileMappingDroneCharTelemetryClient
######################################################################################

FIND_PACKAGE(PythonLibs)
IF(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "- Build SWIG Python library")
  INCLUDE_DIRECTORIES(${PYTHON_INCLUDE_PATH})
  SET(ARGS "-includeall" "-module" "pyFileMappingDroneCharTelemetryClient")

  SET_SOURCE_FILES_PROPERTIES(${PYLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_PYTHON ${PYLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_EXE_LINK_LIBS} ${PYTHON_LIBRARIES})
  SWIG_ADD_MODULE(pyFileMappingDroneCharTelemetryClient python ${PYLIBFILEMAPPINGDRONECHARTELEMETRYCLIENT_INTERFACE} ../src/cFileMappingDroneCharTelemetryClient.cpp)
  SWIG_LINK_LIBRARIES(pyFileMappingDroneCharTelemetryClient ${LIBS_PYTHON})
ELSE(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "--- Cannot make Python bindings, Python dev libraries not found!")
ENDIF(PYTHONLIBS_FOUND)


######################################################################################
# Build function for Python library bindings pyFileMappingDroneCharTelemetryServeur
######################################################################################

FIND_PACKAGE(PythonLibs)
IF(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "- Build SWIG Python library")
  INCLUDE_DIRECTORIES(${PYTHON_INCLUDE_PATH})
  SET(ARGS "-includeall" "-module" "pyFileMappingDroneCharTelemetryServeur")

  SET_SOURCE_FILES_PROPERTIES(${PYLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_PYTHON ${PYLIBFILEMAPPINGDRONECHARTELEMETRY_EXE_LINK_LIBS} ${PYTHON_LIBRARIES})
  SWIG_ADD_MODULE(pyFileMappingDroneCharTelemetryServeur python ${PYLIBFILEMAPPINGDRONECHARTELEMETRYSERVEUR_INTERFACE} ../src/cFileMappingDroneCharTelemetryServeur.cpp)
  SWIG_LINK_LIBRARIES(pyFileMappingDroneCharTelemetryServeur ${LIBS_PYTHON})
ELSE(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "--- Cannot make Python bindings, Python dev libraries not found!")
ENDIF(PYTHONLIBS_FOUND)

######################################################################################
# Build function for Python library bindings pyVirtualDroneCharTelemetry
######################################################################################

FIND_PACKAGE(PythonLibs)
IF(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "- Build SWIG Python library")
  INCLUDE_DIRECTORIES(${PYTHON_INCLUDE_PATH})
  SET(ARGS "-includeall" "-module" "pyVirtualDroneCharTelemetry")

  SET_SOURCE_FILES_PROPERTIES(${PYLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} PROPERTIES SWIG_FLAGS "${ARGS}")
  SET(LIBS_PYTHON ${PYLIBVIRTUALTELLOEDUTELEMETRY_EXE_LINK_LIBS} ${PYTHON_LIBRARIES})
  SWIG_ADD_MODULE(pyVirtualDroneCharTelemetry python ${PYLIBVIRTUALTELLOEDUTELEMETRY_INTERFACE} ../src/cVirtualDroneCharTelemetry.cpp)
  SWIG_LINK_LIBRARIES(pyVirtualDroneCharTelemetry ${LIBS_PYTHON})
ELSE(PYTHONLIBS_FOUND)
  MESSAGE(STATUS "--- Cannot make Python bindings, Python dev libraries not found!")
ENDIF(PYTHONLIBS_FOUND)
