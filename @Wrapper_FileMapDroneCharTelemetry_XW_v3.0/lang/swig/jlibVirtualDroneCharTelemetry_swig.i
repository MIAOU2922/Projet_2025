%include "cVirtualDroneCharTelemetry.h"
%include "arrays_java.i"

%typemap(jni) (unsigned char* SHORT) "jshortArray"
%typemap(jtype) (unsigned char* SHORT) "short[]"
%typemap(jstype) (unsigned char* SHORT) "short[]"
%typemap(in) unsigned char* SHORT { $1 = (unsigned char*) JCALL2
(GetShortArrayElements, jenv, $input, 0);
}
%typemap(argout) unsigned char* SHORT {
JCALL3(ReleaseShortArrayElements, jenv, $input, (jshort *) $1, 0);
}
%typemap(javain) (unsigned char* SHORT) "$javainput"
/* Prevent default freearg typemap from being used */
%typemap(freearg) (unsigned char* SHORT) ""

%typemap(javaout) (unsigned char* SHORT) {
return $jnicall;
}

%apply unsigned char* SHORT { unsigned char* DroneCharTelemetryDataPtr };

%{
#include "cVirtualDroneCharTelemetry.h"
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	#include <windows.h>
	#include <stdlib.h>
	#define _WINSOCKAPI_ 
#else
#endif
%}

#ifndef VIRTUALDRONECHARTELEMETRY_H
	#define VIRTUALDRONECHARTELEMETRY_H
	#define STRUCT_SIZE sizeof(cVirtualDroneCharTelemetry)

	typedef struct cVirtualDroneCharTelemetry cVirtualDroneCharTelemetry;
	struct cVirtualDroneCharTelemetry
	{
		public:
			bool			MutexBlocAccess;
			int				BatteryValue;
			char 			DriveTime[30];
			char 			TempC[30];
			char 			TempF[30];
			char 			Altitude[30];
			double			Ax;
			double			Ay;
			double			Az;
			double			FrontDistance;
			double			BackDistance;
			double			Pressure;
			unsigned char*	DroneCharTelemetryDataPtr;
	};
#endif