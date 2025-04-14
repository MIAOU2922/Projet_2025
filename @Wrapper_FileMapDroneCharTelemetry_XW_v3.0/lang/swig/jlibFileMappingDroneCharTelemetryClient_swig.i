%pragma(java) jniclasspackage="FileMappingTelemetry"

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

%apply unsigned char SHORT { unsigned char value };

%{
#include "cFileMappingDroneCharTelemetryClient.h"	
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
#endif