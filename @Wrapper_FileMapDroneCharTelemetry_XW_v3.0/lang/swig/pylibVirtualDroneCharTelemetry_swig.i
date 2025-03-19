%include "cVirtualDroneCharTelemetry.h"
%include "carrays.i"

%{
#include "cVirtualDroneCharTelemetry.h"
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	#include <windows.h>
	#include <stdlib.h>
	#define _WINSOCKAPI_ 
#else
#endif
%}



%include "cVirtualDroneCharTelemetry.h"

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