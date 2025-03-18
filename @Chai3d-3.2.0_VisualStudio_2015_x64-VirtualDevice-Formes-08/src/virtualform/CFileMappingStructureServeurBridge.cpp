//===========================================================================
/*
/* Serveur de formes virtuelles transmises,
/* par le serveur python de reconnaissance de formes
/*
/* version 1.3 le 30/10/2019  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================/

#include "CFileMappingStructureServeurBridge.h"

//===========================================================================
/*!
Constructor of CFileMappingStructureServeurBridge.
*/
//===========================================================================
cFileMappingStructureServeurBridge::cFileMappingStructureServeurBridge()
{
	TailleStruct = STRUCT_SIZE;

	printf("\n<SERVEUR FILEMAPPING v1.3b>\n\n");
	printf("\n<SERVEUR FILEMAPPING>\n\n");
	printf("Taille de la structure : %d octets\n", TailleStruct);
	if (flagShowMessageBox)
		MessageBox(NULL, ConvCharTabToLPCSTR("\n<SERVEUR FILEMAPPING v1.3>"), ConvCharTabToLPCSTR("Information"), MB_OK);
}

//===========================================================================
/*!
Constructor of CFileMappingStructureServeurBridge.
*/
//===========================================================================
cFileMappingStructureServeurBridge::cFileMappingStructureServeurBridge(bool _flagPrintInfosToConsole, bool _flagShowMessageBox)
{
	TailleStruct = STRUCT_SIZE;

	setflagPrintInfosToConsole(_flagPrintInfosToConsole);
	setflagShowMessageBox(_flagShowMessageBox);

	printf("\n<SERVEUR FILEMAPPING v1.3>\n\n");
	printf("Taille de la structure : %d octets\n", TailleStruct);
	if (flagShowMessageBox)
		MessageBox(NULL, ConvCharTabToLPCSTR("\n<SERVEUR FILEMAPPING v1.3>"), ConvCharTabToLPCSTR("Information"), MB_OK);
}

//===========================================================================
/*!
Destructor of CFileMappingStructureServeurBridge.
*/
//===========================================================================
cFileMappingStructureServeurBridge::~cFileMappingStructureServeurBridge()
{
	CloseServer();
	printf("Fermeture du serveur.");
	if(flagShowMessageBox)
		MessageBox(NULL, ConvCharTabToLPCSTR("Fermeture du serveur."), ConvCharTabToLPCSTR("Erreur"), MB_OK);
}

//===========================================================================
/*!
This method opens a file shared in memory.

\return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cFileMappingStructureServeurBridge::OpenServer()
{
	printf("Nom de la memoire partagee : %s\n", ConvCharTabToLPCSTR(NomSharedMem));
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
	
		// open connection to virtual device
		pFileDataStruct = MapViewOfFile(
			hMapFile,
			FILE_MAP_ALL_ACCESS,
			0,
			0,
			TailleStruct);

		if (pFileDataStruct == NULL)
		{
			printf("%s (%d).\n", MsgBoxError, GetLastError());
			if (flagShowMessageBox)
				MessageBox(NULL, ConvCharTabToLPCSTR(MsgBoxError), ConvCharTabToLPCSTR("Erreur"), MB_OK);
			CloseHandle(hMapFile);
			return false;
		}
		// Ok, nous avons un handle, mais existait-il deja ?
		if (dwLastError != ERROR_ALREADY_EXISTS)
		{
			// Ce processus vient de creer le fichier mappe 
			printf("Adresse Map File : @%8x\n", pFileDataStruct);
			if (flagShowMessageBox)
				MessageBox(NULL, ConvCharTabToLPCSTR("Serveur :Creation du fichier mappe termine, j'attends \"OK\" pour lancer la boucle serveur..."), ConvCharTabToLPCSTR("Information"), MB_OK);

		}
		/*else
		{
			printf("Serveur deja lance!!!");
			if (flagShowMessageBox)
				MessageBox(NULL, ConvCharTabToLPCSTR("Serveur deja lance,ou client toujours actif!!!\nFermez le serveur ou deconnectez le client."), ConvCharTabToLPCSTR("Erreur"), MB_OK);
			return false;
		}*/
		((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = false;

		return true;
	}
}

//===========================================================================
/*!
This method closes the file shared in memory.

*/
//===========================================================================
void cFileMappingStructureServeurBridge::CloseServer()
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
cVirtualFormData * cFileMappingStructureServeurBridge::setStruct(int _FormClass,
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
cVirtualFormData * cFileMappingStructureServeurBridge::ReadStructFromMapFile()
{
	cVirtualFormData *Data = new cVirtualFormData;
	if (!((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess)
	{
		((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = true;
		Data->MutexBlocAccess = (bool)((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess;
		Data->FormClass = (int)((cVirtualFormData*)pFileDataStruct)->FormClass;
		strcpy(Data->FormName, (char *)((cVirtualFormData*)pFileDataStruct)->FormName);
		strcpy(Data->X, (char *)((cVirtualFormData*)pFileDataStruct)->X);
		strcpy(Data->Y, (char *)((cVirtualFormData*)pFileDataStruct)->Y);
		Data->Ray = (int)((cVirtualFormData*)pFileDataStruct)->Ray;
		Data->ColorR = (int)((cVirtualFormData*)pFileDataStruct)->ColorR;
		Data->ColorG = (int)((cVirtualFormData*)pFileDataStruct)->ColorG;
		Data->ColorB = (int)((cVirtualFormData*)pFileDataStruct)->ColorB;
		Data->Style = (int)((cVirtualFormData*)pFileDataStruct)->Style;
		Data->Thickness = (int)((cVirtualFormData*)pFileDataStruct)->Thickness;
	}
	else
	{
		((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = true;
		(int)((cVirtualFormData*)pFileDataStruct)->FormClass = 0;
	}
	((cVirtualFormData*)pFileDataStruct)->MutexBlocAccess = false;

	return Data;		 
}

//===========================================================================
/*!
This method write the cVirtualFormData structure to the map file.

\Parameters cVirtualFormData pointer
\return true or false					 
*/
//===========================================================================
bool cFileMappingStructureServeurBridge::WriteStructToMapFile(cVirtualFormData *Data)
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
void cFileMappingStructureServeurBridge::PrintStruct(cVirtualFormData *Data)
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
This method to show or hide infos in the console

\Parameters bool flagPrintInfosToConsole
*/
//===========================================================================
void cFileMappingStructureServeurBridge::setflagPrintInfosToConsole(bool flag)
{
	flagPrintInfosToConsole = flag;
}

//===========================================================================
/*!
This method to show or hide MessageBox infos

\Parameters bool flagShowMessageBox
*/
//===========================================================================
void cFileMappingStructureServeurBridge::setflagShowMessageBox(bool flag)
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
LPCSTR cFileMappingStructureServeurBridge::ConvCharTabToLPCSTR(const TCHAR *msg)
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
LPWSTR cFileMappingStructureServeurBridge::ConvCharTabToLPWSTR(const char *msg)
{
	size_t nLen = strlen(msg) + 1; // Warning 4996
	wchar_t* pwmsg = (LPWSTR)malloc(sizeof(wchar_t)* nLen);
	size_t ConvertedChars = 0;
	mbstowcs_s(&ConvertedChars, pwmsg, nLen, msg, _TRUNCATE);
	return pwmsg;
}

