//===========================================================================
/*
    This file is part of the CHAI 3D visualization and haptics libraries.
    Copyright (C) 2003-2009 by CHAI 3D. All rights reserved.

    This library is free software; you can redistribute it and/or modify
    it under the terms of the GNU General Public License("GPL") version 2
    as published by the Free Software Foundation.

    For using the CHAI 3D libraries with software that can not be combined
    with the GNU GPL, and for taking advantage of the additional benefits
    of our support services, please contact CHAI 3D about acquiring a
    Professional Edition License.

    \author    <http://www.chai3d.org>
    \author    Francois Conti
    \version   2.0.0 $Rev: 244 $

	\version modifie le 03/01/2018  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================

//---------------------------------------------------------------------------
#include "system/CGlobals.h"
#include "devices/CVirtualDevice.h"
//---------------------------------------------------------------------------
#if defined(C_ENABLE_VIRTUAL_DEVICE_SUPPORT)
//---------------------------------------------------------------------------

//------------------------------------------------------------------------------
namespace chai3d {
//------------------------------------------------------------------------------

//===========================================================================
/*!
    Constructor of cVirtualDevice.
*/
//===========================================================================
cVirtualDevice::cVirtualDevice(unsigned int a_deviceNumber)
{
    // the connection to your device has not yet been established.
    // device is not yet available or ready
    m_deviceAvailable   = false;
    m_deviceReady       = false;

    // settings:
    // haptic device model (see file "CGenericHapticDevice.h")
    m_specifications.m_model                         = C_HAPTIC_DEVICE_VIRTUAL;

    // name of the device manufacturer,
    m_specifications.m_manufacturerName              = "CHAI 3D-WG";

    // name of my device Wilfrid Grassi VirtualDevice
    m_specifications.m_modelName                     = "VirtualDevice-WG";


    //--------------------------------------------------------------------------
    // CHARACTERISTICS: (The following values must be positive or equal to zero)
    //--------------------------------------------------------------------------

    // the maximum force [N] the device can produce along the x,y,z axis.
    m_specifications.m_maxLinearForce                = 10.0;     //# [N]

	// the maximum closed loop linear stiffness in [N/m] along the x,y,z axis
	m_specifications.m_maxLinearStiffness			 = 2000.0; //# [N/m]

	// the maximum amount of torque your device can provide arround its
	// rotation degrees of freedom.
	m_specifications.m_maxAngularTorque				 = 0.0;     // [N*m]

	// the maximum amount of angular stiffness
	m_specifications.m_maxAngularStiffness			 = 0.0; // [N*m/Rad]

	// the maximum amount of torque which can be provided by your gripper
	m_specifications.m_maxGripperForce				 = 0.0;     // [N]

	// the maximum amount of stiffness supported by the gripper
	m_specifications.m_maxGripperLinearStiffness	 = 0.0;     //# [N*m]
	
    // the radius of the physical workspace of the device (x,y,z axis)
    m_specifications.m_workspaceRadius                = 0.15;     //# [m]

    // the maximum opening angle of the gripper
    m_specifications.m_gripperMaxAngleRad             = cDegToRad(30.0);

    //--------------------------------------------------------------------------
    // CHARACTERISTICS: (The following are of boolean type: (true or false)
    //--------------------------------------------------------------------------

    // does your device provide sensed position (x,y,z axis)?
    m_specifications.m_sensedPosition                = true;

    // does your device provide sensed rotations (i.e stylus)?
    m_specifications.m_sensedRotation                = false;

    // does your device provide a gripper which can be sensed?
    m_specifications.m_sensedGripper                 = false;

    // is you device actuated on the translation degrees of freedom?
    m_specifications.m_actuatedPosition              = true;

    // is your device actuated on the rotation degrees of freedom?
    m_specifications.m_actuatedRotation              = false;

    // is the gripper of your device actuated?
    m_specifications.m_actuatedGripper               = false;

    // can the device be used with the left hand?
    m_specifications.m_leftHand                      = true;

    // can the device be used with the right hand?
    m_specifications.m_rightHand                     = true;

    //------------------------ MAP FILE sous windows et linux ---------------------------------------------	
   #if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	// search for virtual device on Windows
	m_hMapFile = OpenFileMapping(
		FILE_MAP_ALL_ACCESS,
		FALSE,
		"dhdVirtual");

	// no virtual device available on Windows
	if (m_hMapFile == NULL)
	{
		printf("Ouverture de dhdVirtual impossible!!!\n");
		// device is not yet available or ready
		m_deviceAvailable   = false;
		m_deviceReady       = false;
		return;
	}
	else
	   printf("Ouverture de dhdVirtual numero fichier : %d...\n",m_hMapFile);
   
	// open connection to virtual device
	m_lpMapAddress = MapViewOfFile(
		m_hMapFile,
		FILE_MAP_ALL_ACCESS,
		0,
		0,
		0);

	// check whether connection succeeded
	if (m_lpMapAddress == NULL)
	{
		printf("Erreur lors de l obtention du segment memoire !!!\n");
		// device is not yet available or ready
		m_deviceAvailable   = false;
		m_deviceReady       = false;
		return;
	}
	else
	   printf("Obtention du segment memoire : 0x%8X...\n",m_lpMapAddress);
   #else	
	// search for virtual device on Linux
	m_hMapFile = shm_open(memname, O_RDWR, 777);

	// no virtual device available on Linux
	if (m_hMapFile == -1)
	{
		printf("Ouverture de dhdVirtual impossible!!!\n");
		// device is not yet available or ready
		m_deviceAvailable   = false;
		m_deviceReady       = false;
	return;
	}
	else
	  printf("Ouverture de dhdVirtual numero fichier : %d...\n",m_hMapFile);
  
	/* now map the shared memory segment in the address space of the process */
	//0xFFFFFFFFFFFFFFFF en 64 bits
	//0xFFFFFFFF en 32 bits
	m_lpMapAddress = mmap((void *)0xFFFFFFFFFFFFFFFF, 1024, PROT_READ | PROT_WRITE , MAP_SHARED, m_hMapFile, 0);

	if (m_lpMapAddress == MAP_FAILED)
	{
		printf("Erreur lors de l obtention du segment memoire !!!\n");
		// device is not yet available or ready
		m_deviceAvailable   = false;
		m_deviceReady       = false;
		return;
	}
	else
	  printf("Obtention du segment memoire : 0x%8X...\n",m_lpMapAddress);
	
	::close(m_hMapFile);
   #endif


	// map memory
	m_pDevice = (cVirtualDeviceData*)m_lpMapAddress;
	if (m_pDevice == NULL)
	{
		// device is not yet available or ready
		m_deviceAvailable   = false;
		m_deviceReady       = false;
		return;
	}
	
	printf("Adresse pDevice : 0x%8X...\n",m_pDevice);
	
	// virtual device is available
	m_deviceAvailable = true;
}

//===========================================================================
/*!
    Destructor of cVirtualDevice.
*/
//===========================================================================
cVirtualDevice::~cVirtualDevice()
{
    if (m_deviceReady)
    {
        close();
    }
	
    close();
}


//===========================================================================
/*!
    This method opens a connection to your device.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cVirtualDevice::open()
{
    // check if the system is available
    if (!m_deviceAvailable) return (C_ERROR);
	
    printf("Ouverture du VirtualDevice\n");
    // if system is already opened then return
    if (m_deviceReady) return (C_SUCCESS);

    m_deviceReady = true;

    return (C_SUCCESS);
}


//===========================================================================
/*!
    This method closes the connection to your device.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool  cVirtualDevice::close()
{
    // check if the system has been opened previously
    if (!m_deviceReady) return (C_ERROR);

    // update status
    m_deviceReady = false;

   #if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
	CloseHandle(m_hMapFile);
	UnmapViewOfFile(m_lpMapAddress);
	printf("Liberation du segment memoire, et fermeture des Handles de memoire partagee\n");
   #else
	m_hMapFile = munmap( m_lpMapAddress , 1024);
	if (m_hMapFile != 0)
	{
		printf("Erreur lors de la liberation du segment memoire !!!\n");
	}
	else
	  printf("Liberation du segment memoire, et fermeture des Handles de memoire partagee\n");
  
/*	m_hMapFile = shm_unlink(memname);
	if (m_hMapFile != 0)
	{
		printf("Erreur lors de la liberation du lien avec la memoire partagee !!!");
	}*/
   #endif

	m_lpMapAddress = NULL;
	m_pDevice = NULL;

    return (C_SUCCESS);
}


//==============================================================================
/*!
    This method calibrates your device.

    This method calibrates your device.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//==============================================================================
bool cVirtualDevice::calibrate(bool a_forceCalibration)
{
    // check if the device is read. See step 3.
    if (!m_deviceReady) return (C_ERROR);

    return (C_SUCCESS);
}



//==============================================================================
/*!
    This method returns the number of devices available from this class of device.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//==============================================================================
unsigned int cVirtualDevice::getNumDevices()
{
    int numberOfDevices = 0;  // At least set to 1 if a device is available.

	//if (cVirtualDevice::isDeviceAvailable()) numberOfDevices = 1;
	numberOfDevices = 1;

	return (numberOfDevices);
}



//===========================================================================
/*!
    This method returns the position of your device. Units are meters [m].

    \param   a_position  Return value.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cVirtualDevice::getPosition(cVector3d& a_position)
{
    // check if the device is read. See step 3.
    if (!m_deviceReady)
    {
        a_position.set(0.0, 0.0, 0.0);
        return (C_ERROR);
    }

    double x,y,z;

    x = 0.0;    // x = getMyDevicePositionX()
    y = 0.0;    // y = getMyDevicePositionY()
    z = 0.0;    // z = getMyDevicePositionZ()

    x = (double)(*m_pDevice).PosX;
    y = (double)(*m_pDevice).PosY;
    z = (double)(*m_pDevice).PosZ;

    // store new position values
    a_position.set(x, y, z);

    // estimate linear velocity
#if !defined(MACOSX) & !defined(LINUX)
       // estimateLinearVelocity(a_position);
#endif

    // exit
    return (C_SUCCESS);
}


//===========================================================================
/*!
    This method returns the orientation frame of your device end-effector

    \param   a_rotation  Return value.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cVirtualDevice::getRotation(cMatrix3d& a_rotation)
{  
    // check if the device is read.
	if (!m_deviceReady)
	{
		a_rotation.identity();
		return (C_ERROR);
	}

	a_rotation.identity();

    // exit
    return (C_SUCCESS);
}


//==============================================================================
/*!
    This method sends a force [N] and a torque [N*m] and gripper torque [N*m]
    to your haptic device.

    \param   a_force  Force command.
    \param   a_torque  Torque command.
    \param   a_gripperForce  Gripper force command.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//==============================================================================
bool cVirtualDevice::setForceAndTorqueAndGripperForce(const cVector3d& a_force,
                                                       const cVector3d& a_torque,
                                                       const double a_gripperForce)
{
    // check if the device is read. See step 3.
    if (!m_deviceReady) return (C_ERROR);

    // store new force value.
    m_prevForce = a_force;
    m_prevTorque = a_torque;
    m_prevGripperForce = a_gripperForce;

    // retrieve force, torque, and gripper force components in individual variables
    ((*m_pDevice).ForceX) = a_force(0);
    ((*m_pDevice).ForceY) = a_force(1);
    ((*m_pDevice).ForceZ) = a_force(2);

    ((*m_pDevice).TorqueA) = a_torque(0);
    ((*m_pDevice).TorqueB) = a_torque(1);
    ((*m_pDevice).TorqueG) = a_torque(2);

    // exit
    return (C_SUCCESS);
}


//===========================================================================
/*!
    This method return the last force [N] sent to the device.

    \param   a_force  Force command.
    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//===========================================================================
bool cVirtualDevice::getForce(cVector3d& a_force)
{
    // check if the device is read. See step 3.
    if (!m_deviceReady)
    {
        a_force.set(0,0,0);
        return (C_ERROR);
    }

    a_force(0) = ((*m_pDevice).ForceX);
    a_force(1) = ((*m_pDevice).ForceY);
    a_force(2) = ((*m_pDevice).ForceZ);

    return (C_SUCCESS);
}

//==============================================================================
/*!
    This method returns status of all user switches
    [__true__ = __ON__ / __false__ = __OFF__].

    \param  a_userSwitches  Return the 32-bit binary mask of the device buttons.

    \return __true__ if the operation succeeds, __false__ otherwise.
*/
//==============================================================================
bool cVirtualDevice::getUserSwitches(unsigned int& a_userSwitches)
{
    // check if the device is read. See step 3.
    if (!m_deviceReady) return (C_ERROR);

    ////////////////////////////////////////////////////////////////////////////
    /*
        STEP 11:

        Here you shall implement code that reads the status all user switches
        on your device. For each user switch, set the associated bit on variable
        a_userSwitches. If your device only has one user switch, then set
        a_userSwitches to 1, when the user switch is engaged, and 0 otherwise.
    */
    ////////////////////////////////////////////////////////////////////////////

    // *** INSERT YOUR CODE HERE ***
    a_userSwitches = 0;

    return (C_SUCCESS);
}
//------------------------------------------------------------------------------
}       // namespace chai3d
//------------------------------------------------------------------------------
#endif  // C_ENABLE_VIRTUAL_DEVICE_SUPPORT
//------------------------------------------------------------------------------


