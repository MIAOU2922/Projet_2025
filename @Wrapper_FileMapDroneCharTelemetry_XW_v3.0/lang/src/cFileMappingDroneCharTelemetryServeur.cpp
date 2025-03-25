/*************************************************************************************************************************************************
*
* @description Serveur d'informations de telemetrie sous forme de fichier de memoire (File mapping),
*
*
* @author: Wilfrid Grassi
* @version: 1.2
* @copyright: (c) 2024 Wilfrid Grassi
* @license: BSD License
* @Date : 09/02/2024 modifications : 20/02/2024
*   Cette version est prete pour la prise en charge Windows 64bits ou  Raspberry 32bits
*************************************************************************************************************************************************/
#include "cFileMappingDroneCharTelemetryServeur.h"

//===========================================================================
/*!
Constructor of cFileMappingDroneCharTelemetryServeur.
*/
//===========================================================================
cFileMappingDroneCharTelemetryServeur::cFileMappingDroneCharTelemetryServeur(bool _DebugFlag)
{
	char msg[60];
	this->DebugFlag = _DebugFlag;

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	sprintf_s(msg, "<FILEMAPPING DRONE TELLO EDU TELEMETRY SERVER v%s>", this->Version.c_str());
#else
	sprintf(msg, "<MAPPING DRONE TELLO EDU TELEMETRY SERVER v%s>", this->Version.c_str());
#endif
	std::printf("\n<FILEMAPPING DRONE TELLO EDU TELEMETRY SERVER v%s - %s>\n", this->Version.c_str(), this->VersionDate.c_str());
	std::printf("File Map Structure size : %d octets\n", this->FileMapDroneCharTelemetrySize);
}

//===========================================================================
/*!
Destructor of cFileMappingDroneCharTelemetryServeur.
*/
//===========================================================================
cFileMappingDroneCharTelemetryServeur::~cFileMappingDroneCharTelemetryServeur()
{
	this->CloseServer();
	free(this->pFileDataStruct);
	std::printf("Server close.\n");
}

//===========================================================================
/*!
This method opens a file shared in memory.

\return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cFileMappingDroneCharTelemetryServeur::OpenServer(char* NameSharedMem)
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	strcpy_s(this->NomSharedMem, NameSharedMem);
#else
	strcpy(this->NomSharedMem, NameSharedMem);
#endif

	std::printf("Shared memory name : < %s >  size %d octets\n", this->NomSharedMem, this->FileMapDroneCharTelemetrySize);
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	this->pFileDataStruct = nullptr;
	// CREATE FILE TO BE MAPPED:
	this->m_hMapFile = (LPHANDLE)CreateFileMapping(
		//(HANDLE)0xFFFFFFFF,			// compilation 32bits
		(LPHANDLE)-1,					// compilation 64bits     	// Current file handle.
		NULL,							// Default security.
		PAGE_READWRITE | SEC_COMMIT,    // Read/write permission.
		0,                              // Max. object size.
		this->FileMapDroneCharTelemetrySize,		// Partie Basse  
		this->NomSharedMem);			// Name of mapping object.

	if (this->m_hMapFile == NULL || this->m_hMapFile == INVALID_HANDLE_VALUE)
	{
		std::printf("Erreur CreateFileMapping: %s (%d).\n", this->MsgBoxError, GetLastError());
		return false;
	}
	else
	{
		this->dwLastError = GetLastError();

		// open connection to virtual Telemetry
		this->m_lpMapAddress = (LPHANDLE)MapViewOfFile(
			this->m_hMapFile,			// handle fichier mappe
			FILE_MAP_ALL_ACCESS,		// Lecture/ecriture
			0,							// Partie haute
			0,							// Partie basse (ici, debut du fichier mappe)
			this->FileMapDroneCharTelemetrySize);	// sur 148 octets

		if (this->m_lpMapAddress == NULL)
		{
			std::printf("%s (%d).\n", this->MsgBoxError, GetLastError());
			CloseHandle((LPHANDLE)this->m_hMapFile);
			return false;
		}
		// Ok, nous avons un handle, mais existait-il deja ?
		if (this->dwLastError != ERROR_ALREADY_EXISTS)
		{
			// Ce processus vient de creer le fichier mappe 
			std::printf("Map File not already exist, Address : @%8x\n", this->m_lpMapAddress);
		}
	}
#else
	this->pFileDataStruct = NULL;
	// search for map file form on Linux
	this->m_hMapFile = shm_open(this->NomSharedMem, O_CREAT | O_TRUNC | O_RDWR, 777);   //| O_TRUNC
	std::printf("FileMapping <%s> created.\n", this->NomSharedMem);

	// no map file form available on Linux
	if (this->m_hMapFile == -1)
	{
		std::printf("Opening File Map <%s>, Error!!!\n", this->NomSharedMem);
		//errExit("shm_open");
		return false;
	}
	else
		std::printf("File Map <%s> File number : %d\n", this->NomSharedMem, this->m_hMapFile);

	//Set the size of the shared memory
	this->hRegion = ftruncate(this->m_hMapFile, this->FileMapDroneCharTelemetrySize);
	if (this->hRegion != 0)
	{
		std::printf("Cannot troncate File Map to keep %d octets!!!\n", this->FileMapDroneCharTelemetrySize);
		return false;
	}
	else
		std::printf("File Map troncate to keep %d octets\n", this->FileMapDroneCharTelemetrySize);

	//now map the shared memory segment in the address space of the process 
	//0xFFFFFFFFFFFFFFFF en 64 bits ou -1
	//0xFFFFFFFF en 32 bits
	this->m_lpMapAddress = mmap((void *)-1, this->FileMapDroneCharTelemetrySize, PROT_READ | PROT_WRITE, MAP_SHARED, this->m_hMapFile, 0);
	::close(this->m_hMapFile);
	if (this->m_lpMapAddress == MAP_FAILED)
	{
		std::printf("Cannot take memory segment !!! Error\n");
		//errExit("mmap");
		return false;
	}
#endif

	// MAP MEMORY:
//	(cVirtualDroneCharTelemetry*)this->pFileDataStruct = (cVirtualDroneCharTelemetry*)this->m_lpMapAddress;
	this->pFileDataStruct = (cVirtualDroneCharTelemetry*)this->m_lpMapAddress;	

	std::printf("Map File address: @%8x\n", this->pFileDataStruct);
	(this->pFileDataStruct)->MutexBlocAccess = false;
	return true;
}

//===========================================================================
/*!
This method closes the file shared in memory.

*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::CloseServer()
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
//	UnmapViewOfFile((cVirtualDroneCharTelemetry*)this->pFileDataStruct);
	UnmapViewOfFile(this->pFileDataStruct);
	std::printf("<%s> free memory segment\n", this->NomSharedMem);
	pFileDataStruct = nullptr;
	CloseHandle((LPHANDLE)this->m_hMapFile);
	std::printf("<%s> All shared memory Handles closed\n", this->NomSharedMem);
#else
	this->m_hMapFile = munmap(this->m_lpMapAddress, this->FileMapDroneCharTelemetrySize);
	if (this->m_hMapFile != 0)
		std::printf("<%s> Error during memory segment release !!!\n", this->NomSharedMem);
	else
		std::printf("<%s> Memory segment released, and all shared memory handles closed\n", this->NomSharedMem);
	this->m_lpMapAddress = NULL;
	this->pFileDataStruct = NULL;
#endif
}

//===========================================================================
/*!
This method read the cVirtualDroneCharTelemetry structure from the map file.

\Parameters cVirtualDroneCharTelemetry  pointer
\return cVirtualDroneCharTelemetry pointer.
*/
//===========================================================================
cVirtualDroneCharTelemetry* cFileMappingDroneCharTelemetryServeur::ReadMapFileToVirtualDroneCharTelemetryStruct(cVirtualDroneCharTelemetry* Data)
{
	if (Data == nullptr)
		PrintDebug("ReadMapFile: Error!!! Data = NULL...", true);
	else
	{
		if (this->pFileDataStruct != nullptr)
		{
			while ((this->pFileDataStruct)->MutexBlocAccess == true)
				Sleep(1);

//			((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = true;
			(this->pFileDataStruct)->MutexBlocAccess = true;
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
//			CopyMemory((cVirtualDroneCharTelemetry*)Data, (cVirtualDroneCharTelemetry*)this->pFileDataStruct, this->FileMapDroneCharTelemetrySize);
			CopyMemory(Data, this->pFileDataStruct, this->FileMapDroneCharTelemetrySize);
#else
//			memcpy((cVirtualDroneCharTelemetry*)Data, (cVirtualDroneCharTelemetry*)this->pFileDataStruct, this->FileMapDroneCharTelemetrySize);
			memcpy(Data, this->pFileDataStruct, this->FileMapDroneCharTelemetrySize);
#endif
//			((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = false;
			(this->pFileDataStruct)->MutexBlocAccess = false;
			PrintDebug("ReadMapFile: Ok", true);
		}
		else
		{
			PrintDebug("ReadMapFile: Error!!! pFileDataStruct = NULL...", true);
			Data = nullptr;
		}
	}
	return Data;
}

//===========================================================================
/*!
This method write the cVirtualDroneCharTelemetry structure to the map file.

\Parameters cVirtualDroneCharTelemetry pointer
\return true or false
*/
//===========================================================================
bool cFileMappingDroneCharTelemetryServeur::WriteVirtualDroneCharTelemetryStructToMapFile(cVirtualDroneCharTelemetry* Data)
{
	if (Data != nullptr && this->pFileDataStruct != nullptr)
	{
		if ((this->pFileDataStruct)->MutexBlocAccess == false)
		{
//			((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = true;
			(this->pFileDataStruct)->MutexBlocAccess = true;
			//save pFileDataStruct->PictureDataPtr !!!
			Data->DroneCharTelemetryDataPtr = this->pFileDataStruct->DroneCharTelemetryDataPtr;
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
			//CopyMemory((PVOID)this->pFileDataStruct, Data, this->FileMapDroneCharTelemetrySize);
//			CopyMemory((cVirtualDroneCharTelemetry*)this->pFileDataStruct, (cVirtualDroneCharTelemetry*)Data, this->FileMapDroneCharTelemetrySize);
			CopyMemory(this->pFileDataStruct, Data, this->FileMapDroneCharTelemetrySize);
#else
			memcpy((cVirtualDroneCharTelemetry*)pFileDataStruct, (cVirtualDroneCharTelemetry*)Data, this->FileMapDroneCharTelemetrySize);
#endif
//			((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = false;
			(this->pFileDataStruct)->MutexBlocAccess = false;
			PrintDebug("Write MapFile: ok", true);
			return true;
		}
		PrintDebug("Write MapFile: Error!!! Mutex true...", true);
		return false;
	}
	PrintDebug("Write MapFile: Error!!! Data = nullptr or pFileDataStruct = nullptr\n", true);
	return false;
}

//===========================================================================
/*!
This methode Print Debug informations

\Parameters const char * msg to print
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::PrintDebug(const char *msg, bool _Return)
{
	if (this->DebugFlag)
		if (_Return)
			std::cout << msg << std::endl;
		else
			std::cout << msg;
}

//===========================================================================
/*!
This method print to the console members of a cVirtualDroneCharTelemetry structure .

\Parameters cVirtualDroneCharTelemetry pointer
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::PrintStruct(cVirtualDroneCharTelemetry* Data)
{
	if (Data != nullptr)
		printf("Struct(@%8x)   Picture Struct Size: %S  \r", this->pFileDataStruct, sizeof(Data));
}

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
//===========================================================================
/*!
This method convert a char array to LPWSTR.

\Parameters const char pointer
\Return wchar_t pointer (LPWSTR)
*/
//===========================================================================
LPWSTR cFileMappingDroneCharTelemetryServeur::ConvCharTabToLPWSTR(const char* msg)
{
	size_t nLen = strlen(msg) + 1; // Warning 4996
	wchar_t* pwmsg = (LPWSTR)malloc(sizeof(wchar_t)* nLen);
	size_t ConvertedChars = 0;
	mbstowcs_s(&ConvertedChars, pwmsg, nLen, msg, _TRUNCATE);
	return pwmsg;
}
#endif

//===========================================================================
//===========================================================================
// getter
//===========================================================================
cVirtualDroneCharTelemetry* cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryPtr()
{
	if (this->pFileDataStruct != nullptr)
		return (cVirtualDroneCharTelemetry*)this->pFileDataStruct;
	else
	{
		PrintDebug("getVirtualDroneCharTelemetryPtr: Error pFileDataStruct = nullptr", true);
		return nullptr;
	}
}
//===========================================================================
bool cFileMappingDroneCharTelemetryServeur::getDebugMode()
{
	return this->DebugFlag;
}
//===========================================================================
bool cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryMutexBlocAccess()
{
	if (pFileDataStruct != nullptr)
		return (this->pFileDataStruct)->MutexBlocAccess;
	else
	{
		PrintDebug("getVirtualDroneCharTelemetryMutexBlocAccess: Error, pFileDataStruct = nullptr", true);
		return false;
	}
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_0()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_0;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_1()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_1;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_2()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_2;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_3()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_3;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_4()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_4;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_5()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_5;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_6()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_6;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_7()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_7;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_8()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_8;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_9()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_9;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_10()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_10;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_11()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_11;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_12()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_12;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_13()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_13;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_14()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_14;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_15()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_15;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_16()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_16;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_17()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_17;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_18()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_18;
}
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::get_val_19()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->val_19;
}
//===========================================================================
//===========================================================================
// setter
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryPtr(cVirtualDroneCharTelemetry *VPStruct)
{
	if (this->pFileDataStruct != nullptr && VPStruct != nullptr)
//		(cVirtualDroneCharTelemetry*)this->pFileDataStruct = VPStruct;
		this->pFileDataStruct = VPStruct;
	else
		PrintDebug("setVirtualDroneCharTelemetryPtr: Error pFileDataStruct = nullptr or VPStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setDebugMode(bool _DebugMode)
{
	this->DebugFlag = _DebugMode;
}
//===========================================================================
void  cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryMutexBlocAccess(bool blocaccess)
{
	if (this->pFileDataStruct != nullptr)
//		(bool)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = blocaccess;
		(this->pFileDataStruct)->MutexBlocAccess = blocaccess;
	else
		PrintDebug("setVirtualDroneCharTelemetryMutexBlocAccess: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_0(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_0 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_1(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_1 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_2(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_2 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_3(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_3 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_4(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_4 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_5(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_5 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_6(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_6 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_7(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_7 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_8(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_8 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_9(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_9 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_10(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_10 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_11(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_11 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_12(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_12 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_13(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_13 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_14(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_14 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_15(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_15 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_16(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_16 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_17(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_17 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_18(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_18 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::set_val_19(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->val_19 = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}
