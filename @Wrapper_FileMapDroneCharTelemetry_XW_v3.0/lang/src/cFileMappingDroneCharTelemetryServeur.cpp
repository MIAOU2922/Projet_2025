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
/*!
This method getVirtualDroneCharTelemetryPtr get the virtual map structure pointer.

\Parameters
\return true or false
*/
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
/*!
This methode get Debug mode to show additional informations

\Parameters
\return Debug mode state true or false
*/
//===========================================================================
bool cFileMappingDroneCharTelemetryServeur::getDebugMode()
{
	return this->DebugFlag;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryBatteryValue get the virtual Telemetry Battery Value.

\Parameters
\return integer Battery value
*/
//===========================================================================
int cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryBatteryValue()
{
	if (this->pFileDataStruct != nullptr)
		return (int)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->BatteryValue;
	else
	{
		PrintDebug("getVirtualDroneCharTelemetryBatteryValue: Error pFileDataStruct = nullptr", true);
		return -1;
	}
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryDriveTime get the virtual Telemetry DriveTime.

\Parameters
\return string Drive Time value
*/
//===========================================================================
char* cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryDriveTime()
{
	return (char*)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->DriveTime;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryTempC get the virtual Telemetry DriveTime.

\Parameters
\return string Celsius Temperature value
*/
//===========================================================================
char* cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryTempC()
{
	return (char*)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->TempC;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryTempF get the virtual Telemetry DriveTime.

\Parameters
\return string Farenheit Temperature value
*/
//===========================================================================
char* cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryTempF()
{
	return (char*)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->TempF;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryAltitude get the virtual Telemetry Altitude.

\Parameters
\return string Altitude value
*/
//===========================================================================
char* cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryAltitude()
{
	return (char*)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Altitude;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryAx get the virtual Telemetry X acceleration.

\Parameters
\return double X acceleration
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryAx()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Ax;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryAy get the virtual Telemetry Y acceleration.

\Parameters
\return double Y acceleration
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryAy()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Ay;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryAz get the virtual Telemetry Z acceleration.

\Parameters
\return double Z acceleration
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryAz()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Az;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryFrontDistance get the virtual Telemetry Front Distance.

\Parameters
\return double Front Distance
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryFrontDistance()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->FrontDistance;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryBackDistance get the virtual Telemetry Back Distance.

\Parameters
\return double Back Distance
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryBackDistance()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->BackDistance;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryPressure get the virtual Telemetry Pressure.

\Parameters
\return double Pressure
*/
//===========================================================================
double cFileMappingDroneCharTelemetryServeur::getVirtualDroneCharTelemetryPressure()
{
	return (double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure;
}

//===========================================================================
/*!
This method getVirtualDroneCharTelemetryMutexBlocAccess get the virtual Telemetry Bloc Access Mutex state.

\Parameters
\return datas number in octets
*/
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
/*!
This method setVirtualDroneCharTelemetryPtr set the virtual map structure pointer.

\Parameters virtual Picture structure pointer
*/
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
/*!
This methode set Debug mode to show additional informations

\Parameters boolean Debug mode set true or false
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setDebugMode(bool _DebugMode)
{
	this->DebugFlag = _DebugMode;
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryBatteryValue set the virtual Telemetry batterie value.

\Parameters int battery value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryBatteryValue(int value)
{
	if (this->pFileDataStruct != nullptr)
//		(int)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->BatteryValue = value;
		(this->pFileDataStruct)->BatteryValue = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryDriveTime set the virtual Telemetry Fly Time.

\Parameters char * DriveTime
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryDriveTime(char* DriveTime)
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
	strcpy_s(this->pFileDataStruct->DriveTime, DriveTime);
#else
	strcpy(this->pFileDataStruct->DriveTime, DriveTime);
#endif
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryTemp set the virtual Telemetry Celsius Temperature.

\Parameters char * TempC
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryTempC(char* TempC)
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
	strcpy_s(this->pFileDataStruct->TempC, TempC);
#else
	strcpy(this->pFileDataStruct->TempC, TempC);
#endif
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryTemp set the virtual Telemetry Farenheit Temperature.

\Parameters char * TempF
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryTempF(char* TempF)
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
	strcpy_s(this->pFileDataStruct->TempF, TempF);
#else
	strcpy(this->pFileDataStruct->TempF, TempF);
#endif
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAltitude set the virtual Telemetry Altitude.

\Parameters char * Altitude
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryAltitude(char* Altitude)
{
#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
	strcpy_s(this->pFileDataStruct->Altitude, Altitude);
#else
	strcpy(this->pFileDataStruct->Altitude, Altitude);
#endif
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAx set the virtual Telemetry X acceleration value.

\Parameters double Ax value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryAx(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Ax = value;
		(this->pFileDataStruct)->Ax = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAx set the virtual Telemetry Y acceleration value.

\Parameters double Ay value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryAy(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Ay = value;
		(this->pFileDataStruct)->Ay = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAx set the virtual Telemetry Z acceleration value.

\Parameters double Az value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryAz(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Az = value;
		(this->pFileDataStruct)->Az = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAx set the virtual Telemetry Front Distance value.

\Parameters double Front Distance value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryFrontDistance(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->FrontDistance = value;
		(this->pFileDataStruct)->FrontDistance = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryAx set the virtual Telemetry Back Distance value.

\Parameters double Back Distance value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryBackDistance(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->BackDistance = value;
		(this->pFileDataStruct)->BackDistance = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method setVirtualDroneCharTelemetryPressure set the virtual Telemetry Pressure value.

\Parameters double Pressure value
*/
//===========================================================================
void cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryPressure(double value)
{
	if (this->pFileDataStruct != nullptr)
//		(double)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->Pressure = value;
		(this->pFileDataStruct)->Pressure = value;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method set the virtual Telemetry Bloc Access Mutex state.

\Parameters char * path of the physical file
*/
//===========================================================================
void  cFileMappingDroneCharTelemetryServeur::setVirtualDroneCharTelemetryMutexBlocAccess(bool blocaccess)
{
	if (this->pFileDataStruct != nullptr)
//		(bool)((cVirtualDroneCharTelemetry*)this->pFileDataStruct)->MutexBlocAccess = blocaccess;
		(this->pFileDataStruct)->MutexBlocAccess = blocaccess;
	else
		PrintDebug("setVirtualDroneCharTelemetryMutexBlocAccess: Error pFileDataStruct = nullptr", true);
}
