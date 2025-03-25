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
			bool MutexBlocAccess;
			unsigned char* DroneCharTelemetryDataPtr;
			float val_0;
			float val_1;
			float val_2;
			float val_3;
			float val_4;
			float val_5;
			float val_6;
			float val_7;
			float val_8;
			float val_9;
			float val_10;
			float val_11;
			float val_12;
			float val_13;
			float val_14;
			float val_15;
			float val_16;
			float val_17;
			float val_18;
			float val_19;



	};
#endif