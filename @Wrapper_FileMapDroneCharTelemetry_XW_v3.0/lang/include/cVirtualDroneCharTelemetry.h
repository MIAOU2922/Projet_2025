/*************************************************************************************************************************************************
*
* @description Structure du Fichier de mapping ou est stocke les donnees de telemetrie du drone Char,
*
*
* @author: Wilfrid Grassi
* @version: 1.1
* @copyright: (c) 2024 Wilfrid Grassi
* @license: BSD License
* @Date : 09/02/2024 modifications : 11/02/2024
*   Cette version est prete pour la prise en charge Windows 64bits ou  Raspberry 32bits
*************************************************************************************************************************************************/
#pragma once

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	#include <windows.h>
	#include <stdlib.h> 
	#define _WINSOCKAPI_    // stops windows.h including winsock.h
#endif


#ifndef VIRTUALDRONECHARTELEMETRY_H
	#define VIRTUALDRONECHARTELEMETRY_H
	#define STRUCT_SIZE sizeof(cVirtualDroneCharTelemetry)

	typedef struct cVirtualDroneCharTelemetry cVirtualDroneCharTelemetry;
	struct cVirtualDroneCharTelemetry
	{
		public:
			bool			MutexBlocAccess;			// Mutex to protect ressouce access
			int				BatteryValue;				// Drone Battery Value
			char 			DriveTime[30];				// Drone Drive Time
			char 			TempC[30];					// Drone Temp Celsius
			char 			TempF[30];					// Drone Temp Farenheit
			char 			Altitude[30];				// Drone Altitude
			double			Ax;							// Drone acceleration X
			double			Ay;							// Drone acceleration Y
			double			Az;							// Drone acceleration Z
			double			FrontDistance;				// Drone FrontDistance
			double			BackDistance;				// Drone BackDistance
			double			Pressure;					// Drone Pressure
			unsigned char*	DroneCharTelemetryDataPtr;	// Pointer on DroneCharTelemetry data buffer
	};
#endif // VIRTUALDRONECHARTELEMETRY_H