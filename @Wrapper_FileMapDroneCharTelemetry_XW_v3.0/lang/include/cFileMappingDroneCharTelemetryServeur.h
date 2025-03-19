/*************************************************************************************************************************************************
*
* @description Serveur d'informations de telemetrie sous forme de fichier de memoire (File mapping),
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

#ifndef FILEMAPPINGDRONECHARTELEMETRYSERVEUR_H
	#define FILEMAPPINGDRONECHARTELEMETRYSERVEUR_H

	#include "cVirtualDroneCharTelemetry.h"
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
		#include <windows.h>
		#include <winnt.h>
		#include <conio.h>
		#define _WINSOCKAPI_    // stops windows.h including winsock.h
	#else
		#include <sys/file.h>
		#include <sys/shm.h>
		#include <sys/mman.h>
		#include <sys/stat.h> /* Pour les constantes « mode » */
		#include <sys/wait.h>
		#include <sys/types.h>
		#include <cstdint>
		#include <unistd.h>
		#define Sleep(X) usleep(X * 1000)
	#endif
	
	#include <stdio.h>
	#include <string.h>
	#include <iostream>
	#include <sstream>
	#include <stdlib.h>
	#include <fcntl.h> /* Pour les constantes O_* */
	#include <fstream>

	#define POINTER_64 __ptr64
	#define POINTER_32 __ptr32
//------------------------------------------------------------------------------
using namespace std;
//------------------------------------------------------------------------------

	//===========================================================================
	/*!
	\file       cFileMappingDroneCharTelemetryServeur.h

	\brief
	<b> Formes </b> \n
	Virtual Formes.
	*/
	//===========================================================================

	//------------------------------------------------------------------------------
	#if defined(WIN32) | defined(WIN64)
	//------------------------------------------------------------------------------
	#ifndef DOXYGEN_SHOULD_SKIP_THIS
	//------------------------------------------------------------------------------

	//------------------------------------------------------------------------------
	#endif  // DOXYGEN_SHOULD_SKIP_THIS
	//------------------------------------------------------------------------------
	#endif  // WIN32
	//------------------------------------------------------------------------------

	//------------------------------------------------------------------------------
	class cFileMappingDroneCharTelemetryServeur;

	//===========================================================================
	/*!
	\class      cFileMappingDroneCharTelemetryServeur
	\ingroup    ShareTelemetryBetweenProcess

	\brief
	Class which interfaces with the virtual DroneCharTelemetry
	*/
	//===========================================================================
	class cFileMappingDroneCharTelemetryServeur
	{
		//--------------------------------------------------------------------------
		// PRIVATE MEMBERS:
		//--------------------------------------------------------------------------
		private:
			const string Version = "1.2";
			const string VersionDate = "20/02/2024";
			
			//! Shared memory connection to virtual Picture.
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
				DWORD dwLastError;
				LPHANDLE m_hMapFile;
				LPVOID m_lpMapAddress;
			#else
				int hRegion;
				int m_hMapFile;
				void* m_lpMapAddress;
			#endif
			
			//! Name of the shared memory segment
			char NomSharedMem[50];
			
			//! Pointer to shared memory data structure.
			cVirtualDroneCharTelemetry* pFileDataStruct;
			
			//! size of VirtualDroneCharTelemetry structure
			int FileMapDroneCharTelemetrySize = STRUCT_SIZE;
			
			//! Predefined error message
			char* MsgBoxError = "Cannot Map memory file!!!";
			
			//! Print debug informations
			bool DebugFlag = true;
			
			//--------------------------------------------------------------------------
			// PUBLIC MEMBERS:
			//--------------------------------------------------------------------------
			
			//-----------------------------------------------------------------------
			// CONSTRUCTOR & DESTRUCTOR:
			//-----------------------------------------------------------------------
		public:
			//! Constructor of CFileMappingStructureServeur.
			cFileMappingDroneCharTelemetryServeur(bool _DebugFlag);
			
			//! Dectructor of CFileMappingStructureServeur
			virtual ~cFileMappingDroneCharTelemetryServeur();

			//--------------------------------------------------------------------------
			// PUBLIC METHODS:
			//--------------------------------------------------------------------------
		public:
			//! This method opens a connection to the memory file.
			bool OpenServer(char* NameSharedMem);
			
			//! This method closes the connection to memory file.
			void CloseServer();

			//! This method Read the Telemetry data Structure cVirtualDroneCharTelemetry from the map file
			cVirtualDroneCharTelemetry* ReadMapFileToVirtualDroneCharTelemetryStruct(cVirtualDroneCharTelemetry* Data);

			//! Write the content of a structure to the map file
			bool WriteVirtualDroneCharTelemetryStructToMapFile(cVirtualDroneCharTelemetry* Data);
			
			//--------------------------------------------------------------------------
			// PUBLIC OPTIONAL METHODES:
			//--------------------------------------------------------------------------
		public:
			//! This methode Print Debug informations
			void PrintDebug(const char* msg, bool _Return);
			
			//! This method Print data structure in the shell console
			void PrintStruct(cVirtualDroneCharTelemetry* Data);
			
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
				//! Convert a char array to LPWSTR
				LPWSTR ConvCharTabToLPWSTR(const char* msg);
			#endif
			
			//--------------------------------------------------------------------------
			// PUBLIC GETTERS:
			//--------------------------------------------------------------------------
		public:
			//! This method getVirtualDroneCharTelemetryPtr get the virtual map structure pointer.
			cVirtualDroneCharTelemetry* getVirtualDroneCharTelemetryPtr();

			//! This methode get Debug mode to show additional informations
			bool getDebugMode();
			
			//! This method getVirtualDroneCharTelemetryBatteryValue get the virtual Telemetry battery value.
			int getVirtualDroneCharTelemetryBatteryValue();
			
			//! This method getVirtualDroneCharTelemetryDriveTime get the virtual Telemetry DriveTime.
			char* getVirtualDroneCharTelemetryDriveTime();
			
			//! This method getVirtualDroneCharTelemetryTempC get the virtual Telemetry Celsius Temperature.
			char* getVirtualDroneCharTelemetryTempC();
			
			//! This method getVirtualDroneCharTelemetryTempF get the virtual Telemetry Farenheit Temperature.
			char* getVirtualDroneCharTelemetryTempF();
			
			//! This method getVirtualDroneCharTelemetryAltitude get the virtual Telemetry Altitude.
			char* getVirtualDroneCharTelemetryAltitude();
			
			//! This method getVirtualDroneCharTelemetryAx get the virtual Telemetry X acceleration.
			double getVirtualDroneCharTelemetryAx();
			
			//! This method getVirtualDroneCharTelemetryAy get the virtual Telemetry Y acceleration.
			double getVirtualDroneCharTelemetryAy();
			
			//! This method getVirtualDroneCharTelemetryAz get the virtual Telemetry Z acceleration.
			double getVirtualDroneCharTelemetryAz();
			
			//! This method getVirtualDroneCharTelemetryFrontDistance get the virtual Telemetry Front Distance.
			double getVirtualDroneCharTelemetryFrontDistance();
			
			//! This method getVirtualDroneCharTelemetryBackDistance get the virtual Telemetry Back Distance.
			double getVirtualDroneCharTelemetryBackDistance();
			
			//! This method getVirtualDroneCharTelemetryPressure get the virtual Telemetry Pressure.
			double getVirtualDroneCharTelemetryPressure();
			
			//! This method getVirtualDroneCharTelemetryMutexBlocAccess get the virtual Telemetry Bloc Access Mutex state.
			bool getVirtualDroneCharTelemetryMutexBlocAccess();

			//--------------------------------------------------------------------------
			// PUBLIC SETTERS:
			//--------------------------------------------------------------------------
		public:
			//! This method setVirtualDroneCharTelemetryPtr set the virtual map structure pointer.
			void setVirtualDroneCharTelemetryPtr(cVirtualDroneCharTelemetry* VTStruct);

			//! This methode set Debug mode to show additional informations
			void setDebugMode(bool _DebugMode);
			
			//! This method setVirtualDroneCharTelemetryBatteryValue set the virtual Telemetry battery value.
			void setVirtualDroneCharTelemetryBatteryValue(int value);
			
			//! This method setVirtualDroneCharTelemetryDriveTime set the virtual Telemetry Fly Time.
			void setVirtualDroneCharTelemetryDriveTime(char* DriveTime);
			
			//! This method setVirtualDroneCharTelemetryTempC set the virtual Telemetry Celsius Temperature.
			void setVirtualDroneCharTelemetryTempC(char* TempC);
			
			//! This method setVirtualDroneCharTelemetryTempF set the virtual Telemetry Farenheit Temperature.
			void setVirtualDroneCharTelemetryTempF(char* TempF);
			
			//! This method setVirtualDroneCharTelemetryAltitude set the virtual Telemetry Altitude.
			void setVirtualDroneCharTelemetryAltitude(char* Altitude);
			
			//! This method setVirtualDroneCharTelemetryAx set the virtual Telemetry X acceleration.
			void setVirtualDroneCharTelemetryAx(double Ax);
			
			//! This method setVirtualDroneCharTelemetryAy set the virtual Telemetry y acceleration.
			void setVirtualDroneCharTelemetryAy(double Ay);
			
			//! This method setVirtualDroneCharTelemetryAz set the virtual Telemetry z acceleration.
			void setVirtualDroneCharTelemetryAz(double Az);
			
			//! This method setVirtualDroneCharTelemetryFrontDistance set the virtual Telemetry Front Distance.
			void setVirtualDroneCharTelemetryFrontDistance(double FrontDistance);

			//! This method setVirtualDroneCharTelemetryBackDistance set the virtual Telemetry Back Distance.
			void setVirtualDroneCharTelemetryBackDistance(double BackDistance);
			
			//! This method setVirtualDroneCharTelemetryPressure set the virtual Telemetry Pressure.
			void setVirtualDroneCharTelemetryPressure(double Pressure);
			
			//! This method set the virtual Telemetry Bloc Access Mutex state.
			void setVirtualDroneCharTelemetryMutexBlocAccess(bool blocaccess);
	};
#endif // !FILEMAPPINGDRONECHARTELEMETRYSERVEUR_H
