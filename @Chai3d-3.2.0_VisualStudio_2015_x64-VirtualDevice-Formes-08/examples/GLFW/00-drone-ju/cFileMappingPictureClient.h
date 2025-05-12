/*************************************************************************************************************************************************
*
* @description Client d images sous forme de fichier de memoire (File maping),
*
*
* @author: Wilfrid Grassi
* @version: 2.6
* @copyright: (c) 2021 Wilfrid Grassi
* @license: BSD License
* @Date : 13/11/2021 modifications : 08/01/2025
*   Cette version est prete pour la prise en charge Windows 64bits ou  Raspberry 32bits
*************************************************************************************************************************************************/
#pragma once

#ifndef FILEMAPPINGPICTURECLIENT
	#define FILEMAPPINGPICTURECLIENT
	
	#include "cVirtualPicture.h"
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
	\file       cFileMappingPictureClient.h

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
	class cFileMappingPictureClient;
	
	//===========================================================================
	/*!
	\class      cFileMappingPictureClient
	\ingroup    FileMappingStructure

	\brief
	Class which interfaces with the virtual Picture
	*/
	//===========================================================================
	class cFileMappingPictureClient
	{
		//--------------------------------------------------------------------------
		// PRIVATE MEMBERS:
		//--------------------------------------------------------------------------
		private:
			const string Version = "2.6   (taille image max 10Mo)";
			const string VersionDate = "08/01/2025";
			
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
			cVirtualPicture* pFileDataStruct;
			
			//! size of VirtualPicture structure
			int FileMapPictureSize = STRUCT_SIZE;
			
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
			//! Constructor of cFileMappingPictureClient.
			cFileMappingPictureClient(bool _DebugFlag);

			//! Dectructor of cFileMappingPictureClient.
			virtual ~cFileMappingPictureClient();

			//--------------------------------------------------------------------------
			// PUBLIC METHODS:
			//--------------------------------------------------------------------------
		public:
			//! This method opens a connection to the memory file.
			bool OpenClient(char* NameSharedMem);
			
			//! This method closes the connection to memory file.
			void CloseClient();

			//! This method Write File to Map File
			bool FileToMapFile(char* Name, cVirtualPicture* Data);

			//! This method MapFileToFile read the cVirtualPicture structure from the map file and write the picture to disk.
			int MapFileToFile(char* Name, cVirtualPicture* Data);

			//! This method Read a binary file on physical disk and store it in a cVirtualPicture structure in a map file.
			int ReadFileToVirtualPictureStruct(char* Name, cVirtualPicture* Data);

			//! This method Read the picture Structure cVirtualPicture from the map file
			cVirtualPicture* ReadMapFileToVirtualPictureStruct(cVirtualPicture* Data);

			//! This method Write the data picture from cVirtualPicture structure to physical file on disk. 
			int WriteVirtualPictureStructToFile(char* Name, cVirtualPicture* Data);
			
			//! Write the content of a structure to the map file
			bool WriteVirtualPictureStructToMapFile(cVirtualPicture* Data);

			//--------------------------------------------------------------------------
			// PUBLIC OPTIONAL METHODES:
			//--------------------------------------------------------------------------
		public:
			//! This methode Print Debug informations
			void PrintDebug(const char* msg, bool _Return);
			
			//! This method Print data structure in the shell console
			void PrintStruct(cVirtualPicture* Data);

			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
				//! Convert a char array to LPWSTR
				LPWSTR ConvCharTabToLPWSTR(const char* msg);
			#endif
			
			//--------------------------------------------------------------------------
			// PUBLIC GETTERS:
			//--------------------------------------------------------------------------
		public:
			//! This method getVirtualPicturePtr get the virtual map structure pointer.
			cVirtualPicture* getVirtualPicturePtr();
			
			//! This methode get Debug mode to show additional informations
			bool getDebugMode();
			
			//! This method getVirtualPictureDataSize get the virtual picture data size.
			int getVirtualPictureDataSize();
			
			//! This method getVirtualPicturePath get the virtual picture path wich point to a physical file.
			char* getVirtualPicturePath();
			
			//! This method getVirtualPictureMutexBlocAccess get the virtual picture Bloc Access Mutex state.
			bool getVirtualPictureMutexBlocAccess();

			//! This method get 1 unsigned char data in buffer PictureData at pos location
			unsigned char getMapFileOneByOneUnsignedChar(int pos);

			//! This method get pointer on unsigned char PictureData buffer
			unsigned char* getMapFileBufferData();
			
			//! This method get File Size in octets
			int getFileSize(char* Name);
			
			//--------------------------------------------------------------------------
			// PUBLIC SETTERS:
			//--------------------------------------------------------------------------
		public:
			//! This method setVirtualPicturePtr set the virtual map structure pointer.
			void setVirtualPicturePtr(cVirtualPicture* VPStruct);

			//! This methode set Debug mode to show additional informations
			void setDebugMode(bool _DebugMode);
			
			//! This method setVirtualPictureDataSize set the virtual picture data size.
			void setVirtualPictureDataSize(int size);
			
			//! This method setVirtualPicturePath set the virtual picture path wich point to a physical file.
			void setVirtualPicturePath(char* path);
			
			//! This method set the virtual picture Bloc Access Mutex state.
			void setVirtualPictureMutexBlocAccess(bool blocaccess);
			
			//! This method get 1 unsigned char data in buffer PictureData at pos location
			bool setMapFileOneByOneUnsignedChar(int pos, unsigned char value);
			
			//! This method set pointer on unsigned char PictureData buffer
			void setMapFileBufferData(unsigned char* Buf);
	};
#endif // !FILEMAPPINGPICTURECLIENT