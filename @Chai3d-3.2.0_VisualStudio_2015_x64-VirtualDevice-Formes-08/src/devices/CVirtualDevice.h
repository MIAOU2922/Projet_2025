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
    \version   2.0.0 $Rev: 251 $

    \version modifie le 03/01/2018  Wilfrid Grassi pour fonctionner avec chai3d 3.2.0
*/
//===========================================================================

//---------------------------------------------------------------------------
#ifndef CVirtualDeviceH
#define CVirtualDeviceH
//------------------------------------------------------------------------------
#if defined(C_ENABLE_VIRTUAL_DEVICE_SUPPORT)
//------------------------------------------------------------------------------
//---------------------------------------------------------------------------
#include "devices/CGenericHapticDevice.h"

#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <fcntl.h> /* Pour les constantes O_* */

#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 

#else
    #include <sys/file.h>
	#include <sys/shm.h>
	#include <sys/mman.h>
	#include <sys/stat.h> /* Pour les constantes « mode » */
	#include <fcntl.h> /* Pour les constantes O_* */
	#include <sys/wait.h>
	#include <sys/types.h>
	#include <cstdint>
	#include <unistd.h>
#endif

//---------------------------------------------------------------------------

//---------------------------------------------------------------------------
namespace chai3d {
//------------------------------------------------------------------------------

//===========================================================================
/*!
    \file       CVirtualDevice.h

    \brief
    <b> Devices </b> \n 
    Virtual Haptic Device.
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
class cVirtualDevice;
typedef std::shared_ptr<cVirtualDevice> cVirtualDevicePtr;
//------------------------------------------------------------------------------

//==============================================================================

struct cVirtualDeviceData
{
    double       ForceX;   // Force component X.
    double       ForceY;   // Force component Y.
    double       ForceZ;   // Force component Z.
    double       TorqueA;  // Torque alpha.
    double       TorqueB;  // Torque beta.
    double       TorqueG;  // Torque gamma.
    double       PosX;     // Position X.
    double       PosY;     // Position Y.
    double       PosZ;     // Position Z.
    double       AngleA;   // Angle alpha.
    double       AngleB;   // Angle beta.
    double       AngleG;   // Angle gamma.
    bool         Button0;  // Button 0 status.
    bool         AckMsg;   // Acknowledge Message
    bool         CmdReset; // Command Reset
};


//===========================================================================
/*!
    \class      cVirtualDevice
    \ingroup    devices  

    \brief      
    Class which interfaces with the virtual device
*/
//===========================================================================
class cVirtualDevice : public cGenericHapticDevice
{
    //-----------------------------------------------------------------------
    // CONSTRUCTOR & DESTRUCTOR:
    //-----------------------------------------------------------------------

  public:
    
    //! Constructor of cVirtualDevice.
    cVirtualDevice(unsigned int a_deviceNumber = 0);

    //! Destructor of cVirtualDevice.
    virtual ~cVirtualDevice();

    //! Shared cVirtualDevice allocator.
    static cVirtualDevicePtr create(unsigned int a_deviceNumber = 0) { return (std::make_shared<cVirtualDevice>(a_deviceNumber)); }


    //--------------------------------------------------------------------------
    // PUBLIC METHODS:
    //--------------------------------------------------------------------------

public:

    //! This method opens a connection to the haptic device.
    virtual bool open();

    //! This method closes the connection to the haptic device.
    virtual bool close();

    //! This method calibrates the haptic device.
    virtual bool calibrate(bool a_forceCalibration = false);

    //! This method returns the position of the device.
    virtual bool getPosition(cVector3d& a_position);

    //! This method returns the orientation frame of the device end-effector.
    virtual bool getRotation(cMatrix3d& a_rotation);

    //! This method returns the status of all user switches [__true__ = __ON__ / __false__ = __OFF__].
    virtual bool getUserSwitches(unsigned int& a_userSwitches);

    //! This method sends a force [N] and a torque [N*m] and gripper force [N] to the haptic device.
    virtual bool setForceAndTorqueAndGripperForce(const cVector3d& a_force, const cVector3d& a_torque, double a_gripperForce);

	//! Read a force [N] from the haptic device.
	virtual bool  getForce(cVector3d& a_force);

    //--------------------------------------------------------------------------
    // PUBLIC STATIC METHODS:
    //--------------------------------------------------------------------------

public:

    //! This method returns the number of devices available from this class of device.
    static unsigned int getNumDevices();


    //--------------------------------------------------------------------------
    // PROTECTED MEMBERS:
    //--------------------------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////
    /*
        INTERNAL VARIABLES:

        If you need to declare any local variables or methods for your device,
        you may do it here below.
    */
    ////////////////////////////////////////////////////////////////////////////

  private:
    //! Shared memory connection to virtual haptic device.
	#if defined(__WIN32__) || defined(_WIN32) || defined(WIN32) || defined(__WINDOWS__) || defined(__TOS_WIN__) 
		HANDLE m_hMapFile;
		//Pointer to shared memory.
		LPVOID m_lpMapAddress;
	#else
		int m_hMapFile;
		const char *memname = "dhdVirtual";
		void * m_lpMapAddress;
	#endif

    //! Pointer to shared memory data structure.
    cVirtualDeviceData* m_pDevice;
};

//------------------------------------------------------------------------------
}       // namespace chai3d
//------------------------------------------------------------------------------
#endif // C_ENABLE_VIRTUAL_DEVICE_SUPPORT
//---------------------------------------------------------------------------
#endif
//------------------------------------------------------------------------------
