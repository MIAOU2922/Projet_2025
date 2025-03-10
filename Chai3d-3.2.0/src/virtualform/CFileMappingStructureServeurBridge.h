//===========================================================================
/*
/* Serveur de formes virtuelles transmises,
/* par le serveur python de reconnaissance de formes
/*
/* version 1.3 le 30/10/2019  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================/
#pragma once

#ifndef FILEMAPPINGSERVEUR_H
	#define FILEMAPPINGSERVEUR_H

	#include <windows.h>
	#include <stdio.h>
	#include <conio.h>
	#include <stdlib.h>
	
	#define _WINSOCKAPI_    // stops windows.h including winsock.h

	//------------------------------------------------------------------------------

	//===========================================================================
	/*!
	\file       CFileMappingStructureServeurBridge.h

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
	class CFileMappingStructureServeurBridge;

	#ifndef STRUCT_H
	#define STRUCT_H
	struct cVirtualFormData
	{
		bool		MutexBlocAccess;
		int 		FormClass;		// Numero de forme
		char 		FormName[15];	// Nom de la forme
		char 		X[50];	   		// Tableau des valeurs de X des sommets de la forme.]
		char 		Y[50];			// Tableau des valeurs de Y des sommets de la forme.
		float		Ray;			// Rayon pour les cercles
		int			ColorR;			// Couleur composante Rouge
		int 		ColorG;			// Couleur composante Verte
		int 		ColorB;			// Couleur composante bleue
		int 		Style;			// Style de trait
		int			Thickness;		// Epaisseur trace
	};

	#define STRUCT_SIZE sizeof(cVirtualFormData);
	#endif // STRUCT_H

	//===========================================================================
	/*!
	\class      CFileMappingStructureServeur
	\ingroup    FileMappingStructure

	\brief
	Class which interfaces with the virtual form
	*/
	//===========================================================================
	class cFileMappingStructureServeurBridge
	{
		//--------------------------------------------------------------------------
		// PRIVATE MEMBERS:
		//--------------------------------------------------------------------------
		private:
			DWORD dwLastError;
			//! Shared memory connection to virtual form.
		#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
			HANDLE hMapFile;
		#else
			int hMapFile;
			const char *memname = "FormesVirtual";
			void * m_lpMapAddress;
		#endif

			//! flag to print infos in the console
			bool flagPrintInfosToConsole = true;

			//! flag to open messagebox infos
			bool flagShowMessageBox = true;

			//! Name of the shared memory segment									
			char NomSharedMem[14] = "FormesVirtual";
			//! Predefined error message				   
			char MsgBoxError[42] = "Impossible de mappé le fichier en mémoire";
			//! size of VirtualFormeData structure							 
			int TailleStruct = sizeof(cVirtualFormData);

			//--------------------------------------------------------------------------
			// PUBLIC MEMBERS:
			//--------------------------------------------------------------------------
		public:
			//! Pointer to shared memory data structure.							   
			//LPCTSTR pFileDataStruct;
			LPVOID pFileDataStruct;

			//-----------------------------------------------------------------------
			// CONSTRUCTOR & DESTRUCTOR:
			//-----------------------------------------------------------------------
		public:
			//! Constructor of CFileMappingStructureServeur.
			cFileMappingStructureServeurBridge();
			
			cFileMappingStructureServeurBridge(bool _flagPrintInfosToConsole, bool _flagShowMessageBox);

			//! Dectructor of CFileMappingStructureServeur
			virtual ~cFileMappingStructureServeurBridge();

			//--------------------------------------------------------------------------
			// PUBLIC METHODS:
			//--------------------------------------------------------------------------
		public:
			//! This method opens a connection to the memory file.											 
			bool OpenServer();

			//! This method closes the connection to memory file.											
			void CloseServer();

			//! set the members of a cVirtualFormData structure										  
			cVirtualFormData * setStruct(int _FormClass,
				char _FormName[15],
				char _X[50],
				char _Y[50],
				float _Ray,
				int _ColorR,
				int _ColorG,
				int _ColorB,
				int _Style,
				int _Tickness);

			//! Read the content Structure from the map file									   
			cVirtualFormData * ReadStructFromMapFile();

			//! Write the content of a structure to the map file										   
			bool WriteStructToMapFile(cVirtualFormData *Data);

			//! Print data structure in the shell console									
			void PrintStruct(cVirtualFormData *Data);

			//! set the flagPrintInfosToConsole
			void setflagPrintInfosToConsole(bool flag);

			//! set the flagShowMessageBox
			void setflagShowMessageBox(bool flag);

			//! Convert a char array to LPCSTR	
			LPCSTR ConvCharTabToLPCSTR(const TCHAR *msg);

			//! Convert a char array to LPWSTR						 
			LPWSTR ConvCharTabToLPWSTR(const char *msg);
	};
#endif // !FILEMAPPINGSERVEUR