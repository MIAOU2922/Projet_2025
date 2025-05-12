/*************************************************************************************************************************************************
*
* @description Client d images sous forme de fichier de memoire (File maping),
*
*
* @author: Wilfrid Grassi
* @version: 2.5
* @copyright: (c) 2021 Wilfrid Grassi
* @license: BSD License
* @Date : 13/11/2021 modifications : 20/12/2021
*   Cette version est prete pour la prise en charge Windows 64bits ou  Raspberry 32bits
*************************************************************************************************************************************************/
#include "cFileMappingPictureClient.h"
//------------------------------------------------------------------------------
using namespace std;
//------------------------------------------------------------------------------

//===========================================================================
/*!
Constructor of cFileMappingPictureClient.
*/
//===========================================================================
cFileMappingPictureClient::cFileMappingPictureClient(bool _DebugFlag)
{
	char msg[60];
	this->DebugFlag = _DebugFlag;

	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
		sprintf_s(msg, "<FILEMAPPING PICTURE CLIENT v%s>", this->Version.c_str());
	#else
		sprintf(msg, "<FILEMAPPING PICTURE CLIENT v%s>", this->Version.c_str());
	#endif
		std::printf("\n<FILEMAPPING PICTURE CLIENT v%s - %s>\n", this->Version.c_str(), this->VersionDate.c_str());
		std::printf("File Map Structure size : %d octets\n", this->FileMapPictureSize);
}

//===========================================================================
/*!
Destructor of cFileMappingPictureClient.
*/
//===========================================================================
cFileMappingPictureClient::~cFileMappingPictureClient()
{
	this->CloseClient();
	free(this->pFileDataStruct);
	std::printf("Client Close.\n");
}

//===========================================================================
/*!
This method opens a file shared in memory.

\return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cFileMappingPictureClient::OpenClient(char* NameSharedMem)
{
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
		strcpy_s(this->NomSharedMem, NameSharedMem);
	#else
		strcpy(this->NNomSharedMem, NameSharedMem);
	#endif

	std::printf("Shared memory name : < %s >  size %d octets\n", this->NomSharedMem, this->FileMapPictureSize);
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
		// CREATE FILE TO BE MAPPED:
		this->m_hMapFile = (LPHANDLE)CreateFileMapping(
			//(HANDLE)0xFFFFFFFF,			// compilation 32bits
			(LPHANDLE)-1,					// compilation 64bits     	// Current file handle.
			NULL,							// Default security.
			PAGE_READWRITE | SEC_COMMIT,    // Read/write permission.
			0,                              // Max. object size.
			this->FileMapPictureSize,		// Partie Basse  
			this->NomSharedMem);			// Name of mapping object.

		if (this->m_hMapFile == NULL || this->m_hMapFile == INVALID_HANDLE_VALUE)
		{
			std::printf("Erreur CreateFileMapping: %s (%d).\n", this->MsgBoxError, GetLastError());
			return false;
		}
		else
		{
			this->dwLastError = GetLastError();

			// open connection to virtual picture
			this->m_lpMapAddress = (LPHANDLE)MapViewOfFile(
				this->m_hMapFile,			// handle fichier mappe
				FILE_MAP_ALL_ACCESS,		// Lecture/ecriture
				0,							// Partie haute
				0,							// Partie basse (ici, debut du fichier mappe)
				this->FileMapPictureSize);	// sur 148 octets

			if (this->m_lpMapAddress == NULL)
			{
				std::printf("%s (%d).\n", this->MsgBoxError, GetLastError());
				CloseHandle((LPHANDLE)this->m_hMapFile);
				return false;
			}
			// Ok, nous avons un handle, mais existait-il deja ?
			if (this->dwLastError != ERROR_ALREADY_EXISTS)
			{
				std::printf("File Map access not possible (%d).\n", GetLastError());
				return false;
			}
		}
	#else
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
		this->hRegion = ftruncate(this->m_hMapFile, this->FileMapPictureSize);
		if (this->hRegion != 0)
		{
			std::printf("Cannot troncate File Map to keep %d octets!!!\n", this->FileMapPictureSize);
			return false;
		}
		else
			std::printf("File Map troncate to keep %d octets\n", this->thisFileMapPictureSize);

		//now map the shared memory segment in the address space of the process 
		//0xFFFFFFFFFFFFFFFF en 64 bits ou -1
		//0xFFFFFFFF en 32 bits
		this->m_lpMapAddress = mmap((void *)-1, this->FileMapPictureSize, PROT_READ | PROT_WRITE, MAP_SHARED, this->m_hMapFile, 0);
		::close((LPHANDLE)this->m_hMapFile);
		if (this->m_lpMapAddress == MAP_FAILED)
		{
			std::printf("Cannot take memory segment !!! Error\n");
			//errExit("mmap");
			return false;
		}
	#endif

	// MAP MEMORY:
	(cVirtualPicture*)this->pFileDataStruct = (cVirtualPicture*)this->m_lpMapAddress;
	std::printf("Map File address: @%8x\n", this->pFileDataStruct);
	(bool)((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = false;																	  
	return true;
}

//===========================================================================
/*!
This method closes the file shared in memory.
*/
//===========================================================================
void cFileMappingPictureClient::CloseClient()
{
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
		UnmapViewOfFile((cVirtualPicture*)this->pFileDataStruct);
		std::printf("<%s> free memory segment\n", this->NomSharedMem);
		pFileDataStruct = nullptr;
		CloseHandle((LPHANDLE)this->m_hMapFile);
		std::printf("<%s> All shared memory Handles closed\n", this->NomSharedMem);
	#else
		this->m_hMapFile = munmap((LPHANDLE)this->m_lpMapAddress, this->FileMapPictureSize);
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
This method FileToMapFile read file and write it to the map file.

\Parameters char *Name, cVirtualPicture  pointer
\return true or false
*/
//===========================================================================
bool cFileMappingPictureClient::FileToMapFile(char* Name, cVirtualPicture* Data)
{
	int size = -1;

	if (Data != nullptr)
	{
		if (this->ReadFileToVirtualPictureStruct(Name, Data) != -1)
			return this->WriteVirtualPictureStructToMapFile(Data);
	}
	return size;
}

//===========================================================================
/*!
This method MapFileToFile read the cVirtualPicture structure from the map file and write the picture to disk.

\Parameters char *Name, cVirtualPicture  pointer
\return number of file byte written or -1 if cannot write file
*/
//===========================================================================
int cFileMappingPictureClient::MapFileToFile(char* Name, cVirtualPicture* Data)
{
	int size = -1;
	
	if (Data != nullptr)
	{
		if (this->ReadMapFileToVirtualPictureStruct(Data) != NULL)
			return this->WriteVirtualPictureStructToFile(Name, Data);
	}
	return size;
}

//===========================================================================
/*!
This method read a binary file on physical disk
and store it in a cVirtualPicture structure in a map file.

\Parameters File Name char pointer, cVirtualPicture pointer
\return number of bytes read (file size) or -1 if error
*/
//===========================================================================
int cFileMappingPictureClient::ReadFileToVirtualPictureStruct(char* Name, cVirtualPicture* Data)
{
	streampos size = -1;
	
	if (Data != nullptr)
	{
		ifstream FileInput(Name, ios::in | ios::binary | ios::ate);
		if (FileInput.is_open())
		{
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
				strcpy_s(Data->PicturePath, Name);
			#else
				strcpy(Data->PicturePath, Name);
			#endif
			size = FileInput.tellg();
			Data->DataPictureSize = size;
			FileInput.seekg(0, ios::beg);
			FileInput.read((char*)(Data->PictureData), Data->DataPictureSize);
			FileInput.close();
			PrintDebug("File Readed: the entire file content is in memory ",true);
		}
		else
		{
			PrintDebug("ReadFileToMapFile: Error!!! File not accessible...",true);
			size = -1;
		}
	}
	return size;
}

//===========================================================================
/*!
This method read the cVirtualPicture structure from the map file.

\Parameters cVirtualPicture  pointer
\return cVirtualPicture pointer.
*/
//===========================================================================
cVirtualPicture* cFileMappingPictureClient::ReadMapFileToVirtualPictureStruct(cVirtualPicture* Data)
{
	if (Data == nullptr)
		PrintDebug("ReadMapFile: Error!!! Data = NULL...",true);
	else
	{
		if (this->pFileDataStruct != nullptr)
		{
			while (((bool)((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess) == true)
				Sleep(1);

			((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = true;
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
				//CopyMemory(Data, (PVOID)this->pFileDataStruct, this->FileMapPictureSize);
				CopyMemory((cVirtualPicture*)Data, (cVirtualPicture*)this->pFileDataStruct, this->FileMapPictureSize);
			#else
				memcpy((cVirtualPicture*)Data, (cVirtualPicture*)this->pFileDataStruct, this->FileMapPictureSize);
			#endif
			((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = false;
			PrintDebug("ReadMapFile: Ok",true);
		}
		else
		{
			PrintDebug("ReadMapFile: Error!!! pFileDataStruct = NULL...",true);
			Data = nullptr;
		}
	}
	return Data;
}

//===========================================================================
/*!
This method write picture Buffer to physical file on disk.

\Parameters File Name char pointer, cVirtualPicture  pointer
\return number of bytes write (file size) or -1 if error
*/
//===========================================================================
int cFileMappingPictureClient::WriteVirtualPictureStructToFile(char* Name, cVirtualPicture* Data)
{
	int size = -1;
	
	if (Data != nullptr)
	{
		ofstream FileOutput(Name, ios::binary | ios::ate);
		if (!FileOutput)
		{
			PrintDebug("WriteMapPictureToFile: Error!!! Cannot open output file...\n<", false);
			PrintDebug(Name,false);
			PrintDebug(">",true);
			size = -1;
		}
		else
		{
			FileOutput.write((char *)Data->PictureData, Data->DataPictureSize);
			FileOutput.close();
			// Size verification, if all bytes where writing
			if ((size = getFileSize(Name)) != Data->DataPictureSize)
			{
				PrintDebug("WriteMapPictureToFile: Error!!! Map picture size = ",false);
				PrintDebug(std::to_string(Data->DataPictureSize).c_str(),false);
				PrintDebug(" / File size = ",false);
				PrintDebug(std::to_string(size).c_str(),true);
				size = -1;
			}
			else
			{
				PrintDebug("WriteMapPictureToFile: Ok  Map picture size = ",false);
				PrintDebug(std::to_string(Data->DataPictureSize).c_str(),true);
				size = Data->DataPictureSize;
			}
		}
	}
	return size;
}

//===========================================================================
/*!
This method write the cVirtualPicture structure to the map file.

\Parameters cVirtualPicture pointer
\return true or false
*/
//===========================================================================
bool cFileMappingPictureClient::WriteVirtualPictureStructToMapFile(cVirtualPicture* Data)
{
	if (Data != nullptr && this->pFileDataStruct != nullptr)
	{
		if (((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess == false)
		{
			((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = true;
			//save pFileDataStruct->PictureDataPtr !!!
			Data->PictureDataPtr = this->pFileDataStruct->PictureDataPtr;
			#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__)
				//CopyMemory((PVOID)this->pFileDataStruct, Data, this->FileMapPictureSize);
				CopyMemory((cVirtualPicture*)this->pFileDataStruct, (cVirtualPicture*)Data, this->FileMapPictureSize);
			#else
				memcpy((cVirtualPicture*)pFileDataStruct, (cVirtualPicture*)Data, PictureSize);
			#endif
			((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = false;
			PrintDebug("Write MapFile: ok",true);
			return true;
		}
		PrintDebug("Write MapFile: Error!!! Mutex true...",true);
		return false;
	}
	PrintDebug("Write MapFile: Error!!! Data = nullptr or pFileDataStruct = nullptr\n",true);
	return false;
}

//===========================================================================
/*!
This methode Print Debug informations

\Parameters const char * msg to print
*/
//===========================================================================
void cFileMappingPictureClient::PrintDebug(const char *msg, bool _Return)
{
	if (this->DebugFlag)
		if(_Return)
			std::cout << msg << std::endl;
		else
			std::cout << msg;
}

//===========================================================================
/*!
This method print to the console members of a cVirtualPicture structure .

\Parameters cVirtualPicture pointer
*/
//===========================================================================
void cFileMappingPictureClient::PrintStruct(cVirtualPicture* Data)
{
	if (Data != nullptr)
		printf("Struct(@%8x) -> Path: (%s) Picture Data Size: %d   Picture Struct Size: %S  \r", this->pFileDataStruct, Data->PicturePath, Data->DataPictureSize, sizeof(Data));
}

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	//===========================================================================
	/*!
	This method convert a char array to LPWSTR.

	\Parameters const char pointer
	\Return wchar_t pointer (LPWSTR)
	*/
	//===========================================================================
	LPWSTR cFileMappingPictureClient::ConvCharTabToLPWSTR(const char* msg)
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
This method getVirtualPicturePtr get the virtual map structure pointer.

\Parameters
\return true or false
*/
//===========================================================================
cVirtualPicture* cFileMappingPictureClient::getVirtualPicturePtr()
{
	if (this->pFileDataStruct != nullptr)
		return (cVirtualPicture*)this->pFileDataStruct;
	else
	{
		PrintDebug("getVirtualPicturePtr: Error pFileDataStruct = nullptr",true);
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
bool cFileMappingPictureClient::getDebugMode()
{
	return this->DebugFlag;
}

//===========================================================================
/*!
This method getVirtualPictureDataSize get the virtual picture data size.

\Parameters
\return datas number in octets
*/
//===========================================================================
int cFileMappingPictureClient::getVirtualPictureDataSize()
{
	if (this->pFileDataStruct != nullptr)
		return (int)((cVirtualPicture*)this->pFileDataStruct)->DataPictureSize;
	else
	{
		PrintDebug("getVirtualPictureDataSize: Error pFileDataStruct = nullptr", true);
		return -1;
	}
}

//===========================================================================
/*!
This method getVirtualPicturePath get the virtual picture path wich point to a physical file.

\Parameters
\return datas number in octets
*/
//===========================================================================
char* cFileMappingPictureClient::getVirtualPicturePath()
{
	return (char*)((cVirtualPicture*)this->pFileDataStruct)->PicturePath;
}

//===========================================================================
/*!
This method getVirtualPictureMutexBlocAccess get the virtual picture Bloc Access Mutex state.

\Parameters
\return datas number in octets
*/
//===========================================================================
bool cFileMappingPictureClient::getVirtualPictureMutexBlocAccess()
{
	if (pFileDataStruct != nullptr)
		return (bool)((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess;
	else
	{
		PrintDebug("getVirtualPictureMutexBlocAccess: Error, pFileDataStruct = nullptr",true);
		return false;
	}
}

//===========================================================================
/*!
This method get 1 unsigned char data in buffer PictureData at pos location

\Parameters pos location of the unsigned char in buffer PictureData
\return unsigned char data wich is readed
*/
//===========================================================================
unsigned char cFileMappingPictureClient::getMapFileOneByOneUnsignedChar(int pos)
{
	if (this->pFileDataStruct != nullptr)
		return (unsigned char)(((cVirtualPicture*)this->pFileDataStruct)->PictureData[pos]);
	else
	{
		PrintDebug("getMapFileOneByOneUnsignedChar: Error pFileDataStruct = nullptr",true);
		return 0;
	}
}

//===========================================================================
/*!
This method get pointer on unsigned unsigned char PictureData buffer

\Parameters
\return pointer unsigned unsigned char data buffer
*/
//===========================================================================
unsigned char* cFileMappingPictureClient::getMapFileBufferData()
{
	unsigned char *Buffer = (unsigned char*) malloc( (int)(((cVirtualPicture*)this->pFileDataStruct)->DataPictureSize) * sizeof(unsigned char));
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
		CopyMemory((unsigned char*)Buffer, (unsigned char*)(((cVirtualPicture*)this->pFileDataStruct)->PictureData) , (int)(((cVirtualPicture*)this->pFileDataStruct)->DataPictureSize) * sizeof(unsigned char));
	#else
		memcpy((unsigned char*)Buffer, (unsigned char*)(((cVirtualPicture*)this->pFileDataStruct)->PictureData), (int)(((cVirtualPicture*)this->pFileDataStruct)->DataPictureSize) * sizeof(unsigned char));
	#endif
	printf("DLL: Buffer address: %x      PictureData address: %x \n", Buffer, (unsigned char*)(((cVirtualPicture*)this->pFileDataStruct)->PictureData));
	for (int i = 0; i < 20; i++)
		printf("DLL: Buffer[%d]: %x\n", i, Buffer[i]);
	return (Buffer);
}

//===========================================================================
/*!
This method get file size in octets

\Parameters File Name char pointer
\return bytes file size or -1 if error
*/
//===========================================================================
int cFileMappingPictureClient::getFileSize(char* Name)
{
	streampos size;
	ifstream FileInput(Name, ios::in | ios::binary | ios::ate);

	if (FileInput.is_open())
	{
		PrintDebug("getFileSize: Ok. Open File <", false);
		PrintDebug(Name, false);
		PrintDebug(">.", true);
		size = FileInput.tellg();
		FileInput.close();
		return size;
	}
	else
	{
		PrintDebug("getFileSize: Error!!! File <", false);
		PrintDebug(Name, false);
		PrintDebug("> not accessible...",true);
		return -1;
	}
}

//===========================================================================
/*!
This method setVirtualPicturePtr set the virtual map structure pointer.

\Parameters virtual Picture structure pointer
*/
//===========================================================================
void cFileMappingPictureClient::setVirtualPicturePtr(cVirtualPicture *VPStruct)
{
	if (this->pFileDataStruct != nullptr && VPStruct != nullptr)
		(cVirtualPicture*)this->pFileDataStruct = VPStruct;
	else
		PrintDebug("setVirtualPicturePtr: Error pFileDataStruct = nullptr or VPStruct = nullptr",true);
}

//===========================================================================
/*!
This methode set Debug mode to show additional informations

\Parameters boolean Debug mode set true or false
*/
//===========================================================================
void cFileMappingPictureClient::setDebugMode(bool _DebugMode)
{
	this->DebugFlag = _DebugMode;
}

//===========================================================================
/*!
This method setVirtualPictureDataSize set the virtual picture data size.

\Parameters number of picture data
*/
//===========================================================================
void cFileMappingPictureClient::setVirtualPictureDataSize(int size)
{
	if (this->pFileDataStruct != nullptr)
		(int)((cVirtualPicture*)this->pFileDataStruct)->DataPictureSize = size;
	else
		PrintDebug("setVirtualPictureDataSize: Error pFileDataStruct = nullptr",true);
}

//===========================================================================
/*!
This method setVirtualPicturePath set the virtual picture path wich point to a physical file.

\Parameters char * path of the physical file
*/
//===========================================================================
void cFileMappingPictureClient::setVirtualPicturePath(char* path)
{
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 																							 
		strcpy_s(this->pFileDataStruct->PicturePath, path);
	#else
		strcpy(this->pFileDataStruct->PicturePath, path);
	#endif
}

//===========================================================================
/*!
This method set the virtual picture Bloc Access Mutex state.

\Parameters char * path of the physical file
*/
//===========================================================================
void  cFileMappingPictureClient::setVirtualPictureMutexBlocAccess(bool blocaccess)
{
	if (this->pFileDataStruct != nullptr)
		(bool)((cVirtualPicture*)this->pFileDataStruct)->MutexBlocAccess = blocaccess;
	else
		PrintDebug("setVirtualPictureMutexBlocAccess: Error pFileDataStruct = nullptr", true);
}

//===========================================================================
/*!
This method set 1 unsigned char data in buffer PictureData at pos location

\Parameters int pos location of the unsigned char in buffer PictureData
unsigned char set the value in the buffer PictureData at this position
*/
//===========================================================================
//!
bool cFileMappingPictureClient::setMapFileOneByOneUnsignedChar(int pos, unsigned char value)
{
	if (this->pFileDataStruct != nullptr)
	{
		(unsigned char)(((cVirtualPicture*)this->pFileDataStruct)->PictureData[pos]) = value;
		return true;
	}
	else
	{
		PrintDebug("setMapFileOneByOneUnsignedChar: Error pFileDataStruct = nullptr", true);
		return false;
	}
}

//===========================================================================
/*!
This method set the  unsigned char pointeur data buffer

\Parameters 

*/
//===========================================================================
//! This method set pointer on unsigned char PictureData buffer
void cFileMappingPictureClient::setMapFileBufferData(unsigned char* Buf)
{
	if (Buf != nullptr && this->pFileDataStruct != nullptr)
	{
		#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
				CopyMemory((unsigned char*)(((cVirtualPicture*)this->pFileDataStruct)->PictureData), (unsigned char*)Buf, sizeof(Buf));
		#else
				memcpy((unsigned char*)(((cVirtualPicture*)this->pFileDataStruct)->PictureData), (unsigned char*)Buf, sizeof(Buf));
		#endif
				PrintDebug("setMapFileBufferData: data Buffer or pFileDataStruct = nullptr", true);
	}
	else
		PrintDebug("setMapFileBufferData: Ok.", true);
}