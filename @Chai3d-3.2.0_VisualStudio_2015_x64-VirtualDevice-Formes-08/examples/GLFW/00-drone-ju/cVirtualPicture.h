/*************************************************************************************************************************************************
*
* @description Structure du Fichier de mapping ou est stocke l image,
*
*
* @author: Wilfrid Grassi
* @version: 3.2
* @copyright: (c) 2021 Wilfrid Grassi
* @license: BSD License
* @Date : 13/11/2021 modifications : 08/01/2025
*   Cette version est prete pour la prise en charge Windows 64bits ou  Raspberry 32bits
*************************************************************************************************************************************************/
#pragma once

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	#include <windows.h>
	#include <stdlib.h> 
	#define _WINSOCKAPI_    // stops windows.h including winsock.h
#endif


#ifndef VIRTUALPICTURE_H
	#define VIRTUALPICTURE_H
	#define STRUCT_SIZE sizeof(cVirtualPicture)
	#define STRUCT_DATAPICTURE_BUFFER_SIZE 10000000 // 10Mo

	typedef struct cVirtualPicture cVirtualPicture;
	struct cVirtualPicture
	{
		public:
			bool			MutexBlocAccess;		// Mutex to protect ressouce access
			int				DataPictureSize;		// Number of data picture
			char 			PicturePath[255];		// Path of the picture on disk
			unsigned char	PictureData[STRUCT_DATAPICTURE_BUFFER_SIZE];	// Data array of the picture
			unsigned char*	PictureDataPtr;		// Pointer on Picture data buffer
	};
#endif // VIRTUALPICTURE_H