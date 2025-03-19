%include "cVirtualDroneCharTelemetry.h"
%include "carrays.i"

%{
#include "cFileMappingDroneCharTelemetryClient.h"	
#include "cVirtualDroneCharTelemetry.h"
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	#include <windows.h>
	#include <winnt.h>
	#include <conio.h>
	
	#define _WINSOCKAPI_
#else
	#include <sys/file.h>
	#include <sys/shm.h>
	#include <sys/mman.h>
	#include <sys/stat.h>
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
#include <fcntl.h>
#include <fstream>

#define POINTER_64 __ptr64
#define POINTER_32 __ptr32
%}

#ifndef FILEMAPPINGDRONECHARTELEMETRYCLIENT_H
	#define FILEMAPPINGDRONECHARTELEMETRYCLIENT_H
	class cFileMappingDroneCharTelemetryClient
	{
		private:
			const string Version = "1.2";
			const string VersionDate = "20/02/2024";
			
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
				DWORD dwLastError;
				LPHANDLE m_hMapFile;
				LPVOID m_lpMapAddress;
			#else
				int hRegion;
				int m_hMapFile;
				void* m_lpMapAddress;
			#endif
			
			char NomSharedMem[50];
			
			cVirtualDroneCharTelemetry* pFileDataStruct;
			int FileMapDroneCharTelemetrySize = STRUCT_SIZE;
			
			char* MsgBoxError = "Cannot Map memory file!!!";
			bool DebugFlag = true;
			
		public:
			cFileMappingDroneCharTelemetryClient(bool _DebugFlag);
			virtual ~cFileMappingDroneCharTelemetryClient();

		public:
			bool OpenClient(char* NameSharedMem);
			void CloseClient();
			
			cVirtualDroneCharTelemetry* ReadMapFileToVirtualDroneCharTelemetryStruct(cVirtualDroneCharTelemetry* Data);
			bool WriteVirtualDroneCharTelemetryStructToMapFile(cVirtualDroneCharTelemetry* Data);

			void PrintDebug(const char* msg, bool _Return);
			void PrintStruct(cVirtualDroneCharTelemetry* Data);
			
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
				LPWSTR ConvCharTabToLPWSTR(const char* msg);
			#endif
			
			cVirtualDroneCharTelemetry* getVirtualDroneCharTelemetryPtr();
			bool getDebugMode();
			int getVirtualDroneCharTelemetryBatteryValue();
			char* getVirtualDroneCharTelemetryDriveTime();
			char* getVirtualDroneCharTelemetryTempC();
			char* getVirtualDroneCharTelemetryTempF();
			char* getVirtualDroneCharTelemetryAltitude();
			double getVirtualDroneCharTelemetryAx();
			double getVirtualDroneCharTelemetryAy();
			double getVirtualDroneCharTelemetryAz();
			double getVirtualDroneCharTelemetryFrontDistance();
			double getVirtualDroneCharTelemetryBackDistance();
			double getVirtualDroneCharTelemetryPressure();
			bool getVirtualDroneCharTelemetryMutexBlocAccess();

			void setVirtualDroneCharTelemetryPtr(cVirtualDroneCharTelemetry* VTStruct);
			void setDebugMode(bool _DebugMode);
			void setVirtualDroneCharTelemetryBatteryValue(int value);
			void setVirtualDroneCharTelemetryDriveTime(char* DriveTime);
			void setVirtualDroneCharTelemetryTempC(char* TempC);
			void setVirtualDroneCharTelemetryTempF(char* TempF);
			void setVirtualDroneCharTelemetryAltitude(char* Altitude);
			void setVirtualDroneCharTelemetryAx(double Ax);
			void setVirtualDroneCharTelemetryAy(double Ay);
			void setVirtualDroneCharTelemetryAz(double Az);
			void setVirtualDroneCharTelemetryFrontDistance(double FrontDistance);
			void setVirtualDroneCharTelemetryBackDistance(double BackDistance);
			void setVirtualDroneCharTelemetryPressure(double Pressure);
			void setVirtualDroneCharTelemetryMutexBlocAccess(bool blocaccess);
	};
#endif