######################################################################################
# CMake directives
######################################################################################

#Require 2.6 or higher. 
CMAKE_MINIMUM_REQUIRED(VERSION 2.6.0 FATAL_ERROR)

INCLUDE( ${CMAKE_SOURCE_DIR}/BuildSysCMakeLib.cmake )

#setting link directory policy

IF(COMMAND cmake_policy)
  CMAKE_POLICY(SET CMP0003 NEW)
ENDIF(COMMAND cmake_policy)

######################################################################################
# Project declaration and options
######################################################################################

#Project declaration

PROJECT(FILEMAPDRONECHARTELEMETRY)
INITIALIZE_BUILD()

#Common Options

OPTION_LIBRARY_BUILD_STATIC(ON)
#We can't build shared on MSVC yet
IF(NOT MSVC)
  OPTION_LIBRARY_BUILD_SHARED(ON)
ENDIF(NOT MSVC)
OPTION_GPROF(OFF)
#OPTION_CREATE_VERSION_FILE(ON "${CMAKE_CURRENT_SOURCE_DIR}/src/version.cc")

#Project specific options

OPTION(STATIC_LINK_SUFFIXES "Add a symbolic link with [library_name]_s on static libraries (for ease in building staticly linked binaries under gcc)" OFF)
OPTION(BUILD_SWIG_BINDINGS "Build Java/Python bindings for libnifalcon" ON)

IF(BUILD_SWIG_BINDINGS)
  ADD_SUBDIRECTORY(lang/swig)
ENDIF(BUILD_SWIG_BINDINGS)

