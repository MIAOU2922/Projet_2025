//===========================================================================
/*
/* Client de formes virtuelles transmises,
/* par le serveur python de reconnaissance de formes
/*
/* version 1.3 le 30/10/2019  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================/
#pragma once

#ifndef FILEMAPPINGCLIENT
	#define FILEMAPPINGCLIENT

	#include <windows.h>
	#include <stdio.h>
	#include <conio.h>

	#include <iostream>
	#include <sstream>
	#include <vector>
	#include <fcntl.h> /* Pour les constantes O_* */

	#include "world\CWorld.h"
	#include "world\CMultiSegment.h"
	#include "world\CShapeCylinder.h"
	
	//------------------------------------------------------------------------------
	#include "chai3d.h"
	//------------------------------------------------------------------------------
	#include <..\extras\GLFW\include\GLFW\glfw3.h>
	//------------------------------------------------------------------------------
	//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
using namespace std;
//------------------------------------------------------------------------------

namespace chai3d {
	//------------------------------------------------------------------------------

	//===========================================================================
	/*!
	\file       CFileMappingStructureClientBridge.h

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
	class cFileMappingStructureClientBridge;

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
	\class      cFileMappingStructureClientBridge
	\ingroup    FileMappingStructure

	\brief
	Class which interfaces with the virtual form
	*/
	//===========================================================================
	class cFileMappingStructureClientBridge
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
			char MsgBoxError[42] = "Impossible de mappe le fichier en memoire";
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
		public :
			//! Constructor of CFileMappingStructureClientBridge.
			cFileMappingStructureClientBridge();	

			cFileMappingStructureClientBridge(bool _flagPrintInfosToConsole, bool _flagShowMessageBox);

			//! Dectructor of CFileMappingStructureClientBridge.
			virtual ~cFileMappingStructureClientBridge();	

		//--------------------------------------------------------------------------
		// PUBLIC METHODS:
		//--------------------------------------------------------------------------
		public:
			//! This method opens a connection to the memory file.
			bool OpenClient();

			//! This method closes the connection to memory file.
			void CloseClient();

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

			//! Split string with char delimiter return strings vector
			vector<string> split(const string &s, char delim);

			//! Draw the video form from VirtualFormData
			vector<cGenericObject*> DrawVideoForm(cWorld * world, cVirtualFormData * Data);

			//! Convert a char array to LPCSTR	
			LPCSTR ConvCharTabToLPCSTR(const TCHAR *msg);

			//! Convert a char array to LPWSTR
			LPWSTR ConvCharTabToLPWSTR(const char *msg);

			//! set the flagPrintInfosToConsole
			void setflagPrintInfosToConsole(bool flag);

			//! set the flagShowMessageBox
			void setflagShowMessageBox(bool flag);

			//! Mapp coordinates
			int map(double x, double in_min, double in_max, int out_min, int out_max);

			float map(int x, int in_min, int in_max, float out_min, float out_max);

			double map(double x, double in_min, double in_max, double out_min, double out_max);
	};

	//------------------------------------------------------------------------------
}	// namespace chai3d
	//------------------------------------------------------------------------------

#endif // !FILEMAPPINGCLIENT