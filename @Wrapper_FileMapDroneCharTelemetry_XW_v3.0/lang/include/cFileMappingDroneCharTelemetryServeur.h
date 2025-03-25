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
			

		public:
			bool getDebugMode();
			cVirtualDroneCharTelemetry* getVirtualDroneCharTelemetryPtr();
			bool getVirtualDroneCharTelemetryMutexBlocAccess();

			double get_val_0();
			double get_val_1();
			double get_val_2();
			double get_val_3();
			double get_val_4();
			double get_val_5();
			double get_val_6();
			double get_val_7();
			double get_val_8();
			double get_val_9();
			double get_val_10();
			double get_val_11();
			double get_val_12();
			double get_val_13();
			double get_val_14();
			double get_val_15();
			double get_val_16();
			double get_val_17();
			double get_val_18();
			double get_val_19();


			void setDebugMode(bool _DebugMode);
			void setVirtualDroneCharTelemetryPtr(cVirtualDroneCharTelemetry* VTStruct);
			void setVirtualDroneCharTelemetryMutexBlocAccess(bool blocaccess);

			void set_val_0(double _val_0);
			void set_val_1(double _val_1);
			void set_val_2(double _val_2);
			void set_val_3(double _val_3);
			void set_val_4(double _val_4);
			void set_val_5(double _val_5);
			void set_val_6(double _val_6);
			void set_val_7(double _val_7);
			void set_val_8(double _val_8);
			void set_val_9(double _val_9);
			void set_val_10(double _val_10);
			void set_val_11(double _val_11);
			void set_val_12(double _val_12);
			void set_val_13(double _val_13);
			void set_val_14(double _val_14);
			void set_val_15(double _val_15);
			void set_val_16(double _val_16);
			void set_val_17(double _val_17);
			void set_val_18(double _val_18);
			void set_val_19(double _val_19);



	};
#endif // !FILEMAPPINGDRONECHARTELEMETRYSERVEUR_H
