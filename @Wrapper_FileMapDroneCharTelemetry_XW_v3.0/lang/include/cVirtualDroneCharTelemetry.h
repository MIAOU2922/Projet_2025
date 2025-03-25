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
#endif // VIRTUALDRONECHARTELEMETRY_H