//===========================================================================
/*
/* Client de formes virtuelles transmises,
/* par le serveur python de reconnaissance de formes
/*
/* version 1.3 le 30/10/2019  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================/

#include "CFileMappingStructureClientBridge.h"
//------------------------------------------------------------------------------
using namespace std;
//------------------------------------------------------------------------------

//------------------------------------------------------------------------------
namespace chai3d {
	//------------------------------------------------------------------------------

	//===========================================================================
	/*!
	Constructor of CFileMappingStructureClientBridge.
	*/
	//===========================================================================
	cFileMappingStructureClientBridge::cFileMappingStructureClientBridge()
	{
		TailleStruct = STRUCT_SIZE;

		printf("\n<CLIENT FILEMAPPING v1.3>\n\n");
		printf("Taille de la structure : %d octets\n", TailleStruct);
		if (flagShowMessageBox)
			MessageBox(NULL, ConvCharTabToLPCSTR("<CLIENT FILEMAPPING v1.3>"), ConvCharTabToLPCSTR("Information"), MB_OK);
	}

	//===========================================================================
	/*!
	Constructor of CFileMappingStructureClientBridge.
	*/
	//===========================================================================
	cFileMappingStructureClientBridge::cFileMappingStructureClientBridge(bool _flagPrintInfosToConsole, bool _flagShowMessageBox)
	{
		TailleStruct = STRUCT_SIZE;

		setflagPrintInfosToConsole(_flagPrintInfosToConsole);
		setflagShowMessageBox(_flagShowMessageBox);

		printf("\n<CLIENT FILEMAPPING v1.3>\n\n");
		printf("Taille de la structure : %d octets\n", TailleStruct);
		if (flagShowMessageBox)
			MessageBox(NULL, ConvCharTabToLPCSTR("<CLIENT FILEMAPPING v1.3>"), ConvCharTabToLPCSTR("Information"), MB_OK);
	}

	//===========================================================================
	/*!
	Destructor of CFileMappingStructureClientBridge.
	*/
	//===========================================================================
	cFileMappingStructureClientBridge::~cFileMappingStructureClientBridge()
	{
		CloseClient();
		printf("Fermeture du Client.");
		if(flagShowMessageBox)
			MessageBox(NULL, ConvCharTabToLPCSTR("Fermeture du client."), ConvCharTabToLPCSTR("Information"), MB_OK);
	}

	//===========================================================================
	/*!
	This method opens a file shared in memory.

	\return __true__ if the operation succeeds, __false__ otherwise.
	*/
	//===========================================================================
	bool cFileMappingStructureClientBridge::OpenClient()
	{
		// CREATE FILE TO BE MAPPED:
		hMapFile = (LPHANDLE)CreateFileMapping(
			//(HANDLE)0xFFFFFFFF,          // compilation 32bits
			(LPHANDLE)0xFFFFFFFFFFFFFFFF,        // compilation 64bits     	// Current file handle.
			NULL,                               // Default security.
			PAGE_READWRITE | SEC_COMMIT,        // Read/write permission.
			0,                                  // Max. object size.
			TailleStruct,            // Partie Basse  
			ConvCharTabToLPCSTR(NomSharedMem));                      // Name of mapping object.

		if (hMapFile == NULL || hMapFile == INVALID_HANDLE_VALUE)
		{
			printf("%s (%d).\n", MsgBoxError, GetLastError());
			if (flagShowMessageBox)
				MessageBox(NULL, ConvCharTabToLPCSTR(MsgBoxError), ConvCharTabToLPCSTR("Erreur"), MB_OK);
			return false;
		}
		else
		{
			dwLastError = GetLastError();

			pFileDataStruct = (LPTSTR)MapViewOfFile(hMapFile,   // handle fichier mappe
				FILE_MAP_ALL_ACCESS, // Lecture/ecriture
				0,                   // Partie haute
				0,                   // Partie basse (ici, debut du fichier mappe)
				TailleStruct);       // sur 148 octets

			if (pFileDataStruct == NULL)
			{
				MessageBox(NULL, ConvCharTabToLPCSTR(MsgBoxError), ConvCharTabToLPCSTR("Erreur"), MB_OK);
				CloseHandle(hMapFile);
				return false;
			}
			// Ok, nous avons un handle, mais existait-il deja ?
			if (dwLastError != ERROR_ALREADY_EXISTS)
			{
				printf("Serveur video non lance, impossible d'utiliser le fichier mappe (%d).\n", GetLastError());
				if (flagShowMessageBox)
					MessageBox(NULL, ConvCharTabToLPCSTR("Serveur video non lance, impossible d'utiliser le fichier mappe."), ConvCharTabToLPCSTR("Erreur"), MB_OK);
				return false;
			}
			else
			{
				// Le fichier mappe existait deja, on doit donc lire les informations.
				printf("Adresse Map File : @%8x\n", pFileDataStruct);
				if (flagShowMessageBox)
					MessageBox(NULL, ConvCharTabToLPCSTR("Client :Creation du fichier mappe termine, j'attends \"OK\" pour lancer la boucle client..."), ConvCharTabToLPCSTR("Information"), MB_OK);
				return true;
			}
		}
	}

	//===========================================================================
	/*!
	This method closes the file shared in memory.

	*/
	//===========================================================================
	void cFileMappingStructureClientBridge::CloseClient()
	{
		UnmapViewOfFile(pFileDataStruct);
		CloseHandle(hMapFile);
	}

	//===========================================================================
	/*!
	This method set a cVirtualFormData structure.

	\Parameters all structure members
	\return cVirtualFormData pointer.
	*/
	//===========================================================================
	cVirtualFormData * cFileMappingStructureClientBridge::setStruct(int _FormClass,
		char _FormName[15],
		char _X[50],
		char _Y[50],
		float _Ray,
		int _ColorR,
		int _ColorG,
		int _ColorB,
		int _Style,
		int _Tickness)
	{
		cVirtualFormData *Data = new cVirtualFormData();

		Data->FormClass = _FormClass;
		strcpy_s(Data->FormName, _FormName);
		strcpy_s(Data->X, _X);
		strcpy_s(Data->Y, _Y);
		Data->Ray = _Ray;
		Data->ColorR = _ColorR;
		Data->ColorG = _ColorG;
		Data->ColorB = _ColorB;
		Data->Style = _Style;
		Data->Thickness = _Tickness;

		return Data;
	}

	//===========================================================================
	/*!
	This method read the cVirtualFormData structure from the map file.

	\return cVirtualFormData pointer.
	*/
	//===========================================================================
	cVirtualFormData  *cFileMappingStructureClientBridge::ReadStructFromMapFile()
	{
		cVirtualFormData *Data  = new cVirtualFormData;
		while (((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess && ((cVirtualFormData*)pFileDataStruct)->FormClass < 1)
		{
			Sleep(1);
		}

		((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = true;
		Data->MutexBlocAccess = (bool)((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess;
		Data->FormClass =(int) ((cVirtualFormData*)pFileDataStruct)->FormClass;
		strcpy(Data->FormName,(char *)((cVirtualFormData*)pFileDataStruct)->FormName);
		strcpy(Data->X, (char *)((cVirtualFormData*)pFileDataStruct)->X);
		strcpy(Data->Y, (char *)((cVirtualFormData*)pFileDataStruct)->Y);
		Data->Ray = (int)((cVirtualFormData*)pFileDataStruct)->Ray;
		Data->ColorR = (int)((cVirtualFormData*)pFileDataStruct)->ColorR;
		Data->ColorG = (int)((cVirtualFormData*)pFileDataStruct)->ColorG;
		Data->ColorB = (int)((cVirtualFormData*)pFileDataStruct)->ColorB;
		Data->Style = (int)((cVirtualFormData*)pFileDataStruct)->Style;
		Data->Thickness = (int)((cVirtualFormData*)pFileDataStruct)->Thickness;

		((cVirtualFormData*)pFileDataStruct)->FormClass = 0;
		((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = false;

		return Data;
	}

	//===========================================================================
	/*!
	This method write the cVirtualFormData structure to the map file.

	\Parameters cVirtualFormData pointer
	\Return bool
	*/
	//===========================================================================
	bool cFileMappingStructureClientBridge::WriteStructToMapFile(cVirtualFormData *Data)
	{
		if (((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess == false)
		{
			//printf("<WRITE>: MutexBlocAccess true\n");
			((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = true;
			CopyMemory((PVOID)pFileDataStruct, Data, TailleStruct);
			//printf("<WRITE>: MutexBlocAccess false\n");
			((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = false;
			//printf("<WRITE>: Return true\n");
			return true;
		}
		//printf("<WRITE>: Return false\n");
		return false;
	}

	//===========================================================================
	/*!
	This method print to the console members of a cVirtualFormData structure .

	\Parameters cVirtualFormData pointer
	*/
	//===========================================================================
	void cFileMappingStructureClientBridge::PrintStruct(cVirtualFormData *Data)
	{
		if(flagPrintInfosToConsole)
			printf("Struct(@%8x) -> (%d) %s SommetsX:%s  SommetsY:%s  Rayon:%f     R:%d G:%d B:%d  Style:%d  Epaisseur:%d\r",
				pFileDataStruct,
				Data->FormClass,
				Data->FormName,
				Data->X, Data->Y,
				Data->Ray,
				Data->ColorR,
				Data->ColorG,
				Data->ColorB,
				Data->Style,
				Data->Thickness);
	}

	//===========================================================================
	/*!
	This method Split string with char delimiter return strings vector

	\Parameters string, char
	\Return Strings Vector
	*/
	//===========================================================================
	vector<string> cFileMappingStructureClientBridge::split(const string &s, char delim)
	{
		vector<string> result;
		stringstream ss(s);
		string item;

		while (getline(ss, item, delim))
		{
			result.push_back(item);
		}

		return result;
	}

	//===========================================================================
	/*!
	This method Draw the video form from VirtualFormData

	\Parameters cWorld pointer, cVirtualFormData pointer
	\Return Vector of cGenericObject pointers 
	*/
	//===========================================================================
	vector<cGenericObject*> cFileMappingStructureClientBridge::DrawVideoForm(cWorld * world, cVirtualFormData * Data)
	{
		int x, y;
		float fx, fy,frayon;
		float X[10];
		float Y[10];
		char* pEnd;
		cColorf colorfRGB;

		// vector to stock all lines objects
		vector<cGenericObject *>  GraphObjectsListe;

		// pointer to a line shape
		cMultiSegment* line;

		// pointer to a mesh for circle
		cMesh* circle;

		// vectors 3D for first coord and next coord to draw a line
		cVector3d Vect3D_X1Y1(0.0,0.0,0.0);
		cVector3d Vect3D_X2Y2(0.0,0.0,0.0);

		// Split X string from Data structure to vectorX
		vector<string> VectorX = split(Data->X, ';');

		// Split Y string from Data structure to vectorX
		vector<string> VectorY = split(Data->Y, ';');

		if (VectorX.size() == VectorY.size())
		{
			// Store all the coord values to float tab X
			int index = 0;
			for (auto vect : VectorX)
			{
				X[index] = strtof(vect.c_str(), &pEnd);
				index++;
			} 

			// Store all the coord values to float tab X
			index = 0;
			for (auto vect : VectorY)
			{
				Y[index] = strtof(vect.c_str(), &pEnd);
				index++;
			}

			// all forms except circle
			if (Data->FormClass > 1)
			{
				/////////////////////////////////////////////////////////////////////////
				// POLYGON: 
				/////////////////////////////////////////////////////////////////////////
				// draw form contour
				for (int i = 0; i < Data->FormClass - 1; i++)
				{
					// first point
					x = (int)X[i];
					y = (int)Y[i];
					fx = map(x, 550, 0, 0.40f, -0.90f);
					fy = map(y, 480, 0, -0.20f, 0.90f);
					Vect3D_X1Y1.set(0.5, fx, fy);

					// second point
					x = (int)X[i + 1];
					y = (int)Y[i + 1];
					fx = map(x, 550, 0, 0.40f, -0.90f);
					fy = map(y, 480, 0, -0.20f, 0.90f);
					Vect3D_X2Y2.set(0.5, fx, fy);

					// create a line
					line = new cMultiSegment();
					line->newSegment(Vect3D_X1Y1, Vect3D_X2Y2);

					// add line to Vector list for remove all lines after
					GraphObjectsListe.push_back(line);

					//add line  to world
					world->addChild(line);

					// set color at each point
					//colorfRGB.set(Data->ColorR, Data->ColorG, Data->ColorB);
					colorfRGB.set(0.0, 0.0, 0.0);
					line->setLineColor(colorfRGB);
					line->setLineWidth(2);

					// create haptic effect and set haptic properties
					line->createEffectMagnetic();
					line->m_material->setMagnetMaxDistance(0.05);
					line->m_material->setMagnetMaxForce(5.0);
					line->m_material->setStiffness(500);
				}

				// close the contour of the form
				// first point
				x = (int)X[0];
				y = (int)Y[0];
				fx = map(x, 550, 0, 0.40f, -0.90f);
				fy = map(y, 480, 0, -0.20f, 0.90f);
				Vect3D_X1Y1.set(0.5, fx, fy);

				// second point
				x = (int)X[Data->FormClass - 1];
				y = (int)Y[Data->FormClass - 1];
				fx = map(x, 550, 0, 0.40f, -0.90f);
				fy = map(y, 480, 0, -0.20f, 0.90f);
				Vect3D_X2Y2.set(0.5, fx, fy);

				// create a line
				line = new cMultiSegment();
				line->newSegment(Vect3D_X1Y1, Vect3D_X2Y2);

				// add line to Vector list for remove all lines after
				GraphObjectsListe.push_back(line);

				world->addChild(line);
				// set color at each point
				//colorfRGB.getColorb().set(Data->ColorR, Data->ColorG, Data->ColorB);
				colorfRGB.set(0.0, 0.0, 0.0);
				line->setLineColor(colorfRGB);
				line->setLineWidth(2);
				// create haptic effect and set haptic properties
				line->createEffectMagnetic();
				line->m_material->setMagnetMaxDistance(0.05);
				line->m_material->setMagnetMaxForce(5.0);
				line->m_material->setStiffness(500);;
			}
			else // for circle only
			{
				/////////////////////////////////////////////////////////////////////////
				// CIRCLE: 
				/////////////////////////////////////////////////////////////////////////
				x = (int)X[0];
				y = (int)Y[0];

				fx = map(x, 550, 0, 0.40f, -0.50f);
				fy = map(y, 480, 0, -0.20f, 0.90f);
				frayon = map((int)Data->Ray, 500, 0, -0.2f, 0.2f);
				frayon = abs(frayon);
				Vect3D_X1Y1.set(0.5, fx, fy);

				circle = new cMesh();

				// create a cylinder
				cCreatePipe(circle, 0.01,frayon, frayon, 200 * frayon);

				// create collision detector
				//object0->createAABBCollisionDetector(toolRadius);

				// add circle to Vector list for remove the circle after
				GraphObjectsListe.push_back(circle);

				// add object to world
				world->addChild(circle);

				// set the position of the object
				circle->setLocalPos(0.5,fx, fy);
				circle->rotateAboutGlobalAxisDeg(cVector3d(0, 1, 0), 90);

				// set material color
				//circle->m_material->setColor(Data->ColorR, Data->ColorG, Data->ColorB);
				colorfRGB.set(0.0, 0.0, 0.0);
				circle->m_material->setColor(colorfRGB);

				// set haptic properties
				circle->m_material->setStiffness(0.05 * 0.1);
				circle->m_material->setStaticFriction(0.0);
				circle->m_material->setDynamicFriction(0.0);
				circle->m_material->setUseHapticShading(true);
			}
		}
		else
			printf("Erreur de coordonnees dans la forme Vx:%d Vy:%d  Vx:%s    Vy:%s!!!\n", VectorX.size(),VectorY.size(), Data->X, Data->Y);

		return GraphObjectsListe;
	}

	//===========================================================================
	/*!
	This method to show or hide infos in the console

	\Parameters bool flagPrintInfosToConsole
	*/
	//===========================================================================
	void cFileMappingStructureClientBridge::setflagPrintInfosToConsole(bool flag)
	{
		flagPrintInfosToConsole = flag;
	}

	//===========================================================================
	/*!
	This method to show or hide MessageBox infos

	\Parameters bool flagShowMessageBox
	*/
	//===========================================================================
	void cFileMappingStructureClientBridge::setflagShowMessageBox(bool flag)
	{
		flagShowMessageBox = flag;
	}

	//===========================================================================
	/*!
	This method convert a char array to LPCSTR.

	\Parameters const char pointer
	\Return wchar_t pointer (LPCSTR)
	*/
	//===========================================================================
	LPCSTR cFileMappingStructureClientBridge::ConvCharTabToLPCSTR(const TCHAR *msg)
	{
		/*size_t nLen = strlen(msg) + 1; // Warning 4996
		LPCSTR pmsg = (LPCSTR) malloc(sizeof(LPCSTR) * nLen);
		//size_t ConvertedChars = 0;
		//mbstocs_s(&ConvertedChars, pwmsg, nLen, msg, nLen);
		*/
		return msg;
	}

	//===========================================================================
	/*!
	This method convert a char array to LPWSTR.

	\Parameters const char pointer
	\Return wchar_t pointer (LPWSTR)
	*/
	//===========================================================================
	LPWSTR cFileMappingStructureClientBridge::ConvCharTabToLPWSTR(const char *msg)
	{
		size_t nLen = strlen(msg) + 1; // Warning 4996
		wchar_t* pwmsg = (LPWSTR)malloc(sizeof(wchar_t)* nLen);
		size_t ConvertedChars = 0;
		mbstowcs_s(&ConvertedChars, pwmsg, nLen, msg, _TRUNCATE);
		return pwmsg;
	}

	//===========================================================================
	/*!
	Colletion of 3 methods to adapt the coordinates from world to an other world

	\Parameters x, min x, max x, out min x, out max x 
	\Return new coordinate in the new space
	*/
	//===========================================================================
	int cFileMappingStructureClientBridge::map(double x, double in_min, double in_max, int out_min, int out_max)
	{
		return (int)((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
	}

	float cFileMappingStructureClientBridge::map(int x, int in_min, int in_max, float out_min, float out_max)
	{
		return (float)((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
	}

	double cFileMappingStructureClientBridge::map(double x, double in_min, double in_max, double out_min, double out_max)
	{
		return (double)((x - in_min) * (out_max - out_min) / (in_max - in_min) + out_min);
	}

	//------------------------------------------------------------------------------
}	// namespace chai3d
	//------------------------------------------------------------------------------


