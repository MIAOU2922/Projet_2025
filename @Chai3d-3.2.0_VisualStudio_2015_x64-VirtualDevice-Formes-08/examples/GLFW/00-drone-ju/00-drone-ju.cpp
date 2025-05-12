/**
 * -------------------------------------------------------------------
 * Nom du fichier : 00-drone-ju.cpp
 * Auteur         : BEAL julien
 * Version        : 1.0
 * Date           : 10/03/2025
 * Description    : IHM chai3d avec background mis à jour aléatoirement
 *                via un cBackground et un cImage (allocation 24 bits)
 * -------------------------------------------------------------------
 */
#include "chai3d.h"
#include <GLFW/glfw3.h>
#include <iostream>
#include <cstdlib>
#include <ctime>
#include <GL/gl.h>
#include "cFileMappingPictureServeur.h"
#include "cFileMappingPictureClient.h"
#include <iomanip>


using namespace chai3d;
using namespace std;
cStereoMode stereoMode = C_STEREO_DISABLED;

// fullscreen mode
bool fullscreen = false;

// mirrored display
bool mirroredDisplay = false;

cWorld* world;
cCamera* camera;
cDirectionalLight* light;
cImagePtr backgroundImage = cImage::create();
cShapeSphere* sphere;
cLevel* levelVelocity;
cDial* dialPosX;
cDial* dialPosY;
cDial* dialPosZ;
cFrequencyCounter freqCounterGraphics;
GLFWwindow* window = NULL;
cBitmap *BitmapBackgroundMovie = new cBitmap();

int width = 1080;
int height = 720;
int swapInterval = 1;

const int IMAGE_WIDTH  = 1080;
const int IMAGE_HEIGHT = 720;
unsigned char* Buffer = nullptr;
unsigned char* BackgroundImageByteArray;
unsigned char* ContextImageByteArray;

cImagePtr ContextImage = cImage::create();

cVirtualPicture* maVirtualPicture = NULL;


cFileMappingPictureServeur* monServeurCppFMPictureScreenShot = NULL;
cFileMappingPictureClient* monClientCppFMPictureScreenShot = NULL;

cVirtualPicture* maVirtualPictureScreenShot = NULL;

// callback when the window display is resized
void windowSizeCallback(GLFWwindow* a_window, int a_width, int a_height);

// callback when an error GLFW occurs
void errorCallback(int error, const char* a_description);

// callback when a key is pressed
void keyCallback(GLFWwindow* a_window, int a_key, int a_scancode, int a_action, int a_mods);

void updateGraphics(void);
void close(void);
void errorCallback(int error, const char* a_description);
void updateBackgroundImage(void);
void ecrireEnMap(void);
double getRandom01_2Decimals();

int main(int argc, char* argv[]) {


    //--------------------------------------------------------------------------
    // OPEN GL - WINDOW DISPLAY
    //--------------------------------------------------------------------------

    // initialize GLFW library
    if (!glfwInit())
    {
        cout << "failed initialization" << endl;
        cSleepMs(1000);
        return 1;
    }

    // set error callback
    glfwSetErrorCallback(errorCallback);

    // compute desired size of window
    const GLFWvidmode* mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    int w = 0.8 * mode->height;
    int h = 0.5 * mode->height;
    int x = 0.5 * (mode->width - w);
    int y = 0.5 * (mode->height - h);

    // set OpenGL version
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

    // set active stereo mode
    if (stereoMode == C_STEREO_ACTIVE)
    {
        glfwWindowHint(GLFW_STEREO, GL_TRUE);
    }
    else
    {
        glfwWindowHint(GLFW_STEREO, GL_FALSE);
    }

    // create display context
    window = glfwCreateWindow(w, h, "drone", NULL, NULL);
    if (!window)
    {
        cout << "failed to create window" << endl;
        cSleepMs(1000);
        glfwTerminate();
        return 1;
    }

    // get width and height of window
    glfwGetWindowSize(window, &width, &height);

    // set position of window
    glfwSetWindowPos(window, x, y);

    // set key callback
    glfwSetKeyCallback(window, keyCallback);

    // set resize callback
    glfwSetWindowSizeCallback(window, windowSizeCallback);

    // set current display context
    glfwMakeContextCurrent(window);

    // sets the swap interval for the current display context
    glfwSwapInterval(swapInterval);

#ifdef GLEW_VERSION
    // initialize GLEW library
    if (glewInit() != GLEW_OK)
    {
        cout << "failed to initialize GLEW library" << endl;
        glfwTerminate();
        return 1;
    }
#endif


    //--------------------------------------------------------------------------
    // WORLD - CAMERA - LIGHTING
    //--------------------------------------------------------------------------

    // create a new world.
    world = new cWorld();

    // set the background color of the environment
    world->m_backgroundColor.setBlack();

    // create a camera and insert it into the virtual world
    camera = new cCamera(world);
    world->addChild(camera);

    // position and orient the camera
    camera->set(cVector3d(3.0, 0.0, 0.6),    // camera position (eye)
        cVector3d(0.0, 0.0, 0.0),    // lookat position (target)
        cVector3d(0.0, 0.0, 1.0));   // direction of the (up) vector

    // set the near and far clipping planes of the camera
    // anything in front or behind these clipping planes will not be rendered
    camera->setClippingPlanes(0.01, 10.0);

    // set stereo mode
    camera->setStereoMode(stereoMode);

    // set stereo eye separation and focal length (applies only if stereo is enabled)
    camera->setStereoEyeSeparation(0.03);
    camera->setStereoFocalLength(3.0);

    // set vertical mirrored display mode
    camera->setMirrorVertical(mirroredDisplay);

    // create a light source
    light = new cDirectionalLight(world);

    // attach light to camera
    camera->addChild(light);

    // enable light source
    light->setEnabled(true);

    // define the direction of the light beam
    light->setDir(-3.0, -0.5, 0.0);

    // set lighting conditions
    light->m_ambient.set(0.4f, 0.4f, 0.4f);
    light->m_diffuse.set(0.8f, 0.8f, 0.8f);
    light->m_specular.set(1.0f, 1.0f, 1.0f);
    

    camera->m_backLayer->addChild(BitmapBackgroundMovie);
    camera->m_backLayer->setShowEnabled(true);
    BitmapBackgroundMovie->setShowEnabled(true);




    //backgroundImage = cImage::create();
    backgroundImage->allocate(IMAGE_WIDTH, IMAGE_HEIGHT, GL_RGB, GL_UNSIGNED_INT);
    ContextImage->allocate(width*5, height*5, GL_RGB, GL_UNSIGNED_INT);
    ContextImageByteArray = new unsigned char[width * height * 3];

    glViewport(0, 0, width, height);



    light = new cDirectionalLight(world);
    world->addChild(light);
    light->setEnabled(true);
    light->setDir(-1.0, 0.0, 0.0);
    
    sphere = new cShapeSphere(0.1);
    sphere->m_material->setRed();
    sphere->setLocalPos(0.0, 0.0, 0.0);
    world->addChild(sphere);
    
    monClientCppFMPictureScreenShot = new cFileMappingPictureClient(false);
    monClientCppFMPictureScreenShot->setDebugMode(false);
    monClientCppFMPictureScreenShot->OpenClient("img_java_to_c");

    monServeurCppFMPictureScreenShot = new cFileMappingPictureServeur(false);
    monServeurCppFMPictureScreenShot->setDebugMode(false);
    monServeurCppFMPictureScreenShot->OpenServer("img_c_to_java");
    maVirtualPictureScreenShot = new cVirtualPicture();

    // call window size callback at initialization
    windowSizeCallback(window, width, height);

	// attendre 1 seconde avant de commencer
    cSleepMs(1000);


    while (!glfwWindowShouldClose(window)) {
        glfwGetWindowSize(window, &width, &height);
        updateGraphics();
        glfwSwapBuffers(window);
        glfwPollEvents();
        freqCounterGraphics.signal(1);
    }
    
    close();
    return 0;
}

void close(void) {
    monClientCppFMPictureScreenShot->CloseClient();
    delete world;
}

//------------------------------------------------------------------------------

void errorCallback(int a_error, const char* a_description)
{
    cout << "Error: " << a_description << endl;
}

//------------------------------------------------------------------------------

void keyCallback(GLFWwindow* a_window, int a_key, int a_scancode, int a_action, int a_mods)
{
    // filter calls that only include a key press
    if ((a_action != GLFW_PRESS) && (a_action != GLFW_REPEAT))
    {
        return;
    }

    // option - exit
    else if ((a_key == GLFW_KEY_ESCAPE) || (a_key == GLFW_KEY_Q))
    {
        glfwSetWindowShouldClose(a_window, GLFW_TRUE);
    }

   
    // option - toggle fullscreen
    else if (a_key == GLFW_KEY_F)
    {
        // toggle state variable
        fullscreen = !fullscreen;

        // get handle to monitor
        GLFWmonitor* monitor = glfwGetPrimaryMonitor();

        // get information about monitor
        const GLFWvidmode* mode = glfwGetVideoMode(monitor);

        // set fullscreen or window mode
        if (fullscreen)
        {
            glfwSetWindowMonitor(window, monitor, 0, 0, mode->width, mode->height, mode->refreshRate);
            glfwSwapInterval(swapInterval);
        }
        else
        {
            int w = 0.8 * mode->height;
            int h = 0.5 * mode->height;
            int x = 0.5 * (mode->width - w);
            int y = 0.5 * (mode->height - h);
            glfwSetWindowMonitor(window, NULL, x, y, w, h, mode->refreshRate);
            glfwSwapInterval(swapInterval);
        }
    }

    // option - toggle vertical mirroring
    else if (a_key == GLFW_KEY_M)
    {
        mirroredDisplay = !mirroredDisplay;
        camera->setMirrorVertical(mirroredDisplay);
    }
}


void updateGraphics(void) 
{
    updateBackgroundImage();

    world->updateShadowMaps(false, false);

    sphere->setLocalPos(getRandom01_2Decimals(), getRandom01_2Decimals(), getRandom01_2Decimals());



    camera->renderView(width, height);

    glFinish();
    // check for any OpenGL errors
    GLenum err = glGetError();
    if (err != GL_NO_ERROR) cout << "Error: " << gluErrorString(err) << endl;

    ContextImage->setSize(width, height);
    camera->copyImageBuffer(ContextImage);

	ecrireEnMap();
}

//------------------------------------------------------------------------------

void windowSizeCallback(GLFWwindow* a_window, int a_width, int a_height)
{
    // update window size
    width = a_width;
    height = a_height;
}

void updateBackgroundImage() {
    // Attendre que le mutex soit libre
    while (monClientCppFMPictureScreenShot->getVirtualPictureMutexBlocAccess()) 
    {
        cSleepMs(1);
    }
    
    // Verrouiller le mutex
    monClientCppFMPictureScreenShot->setVirtualPictureMutexBlocAccess(true);
    //BackgroundImageByteArray = monClientCppFMPictureScreenShot->getMapFileBufferData();
    int TailleImage = monClientCppFMPictureScreenShot->getVirtualPictureDataSize();
    
    BackgroundImageByteArray = new unsigned char[TailleImage];
    for (int i = 0; i < TailleImage; i++)
    {
        BackgroundImageByteArray[i] = monClientCppFMPictureScreenShot->getMapFileOneByOneUnsignedChar(i);
    }
    
    // Deverrouiller le mutex
    monClientCppFMPictureScreenShot->setVirtualPictureMutexBlocAccess(false);

    //On recupere l image depuis le byte array
    
    if (cLoadJPG(backgroundImage->getImage(), BackgroundImageByteArray, TailleImage))
    {
        //cout << "Image recuperee avec succes" << endl;

        BitmapBackgroundMovie->loadFromImage(backgroundImage);
        BitmapBackgroundMovie->setSize(width, height);

        BackgroundImageByteArray = NULL;

    }

    


    //cSaveFileJPG(backgroundImage->getImage(), "Image_Test.jpg");
    
    // Mettre à jour le widget de fond

    
}

void ecrireEnMap()
{
	// Attendre que le mutex soit libre
	while (monServeurCppFMPictureScreenShot->getVirtualPictureMutexBlocAccess())
	{
		cSleepMs(1);
	}
	monServeurCppFMPictureScreenShot->setVirtualPictureMutexBlocAccess(true);

    unsigned int size = 0;
    //unsigned char** Buffer = (unsigned char**)malloc(900000 * sizeof(unsigned char));
    
    bool ret = cSaveJPG(ContextImage->getImage(), &Buffer, &size);
	if (ret)
	{
		
        CopyMemory((unsigned char*)maVirtualPictureScreenShot->PictureData,Buffer, size);
        cout << "Image ecrite avec succes: " << maVirtualPictureScreenShot << endl;
        maVirtualPictureScreenShot->DataPictureSize = (int) size;
		monServeurCppFMPictureScreenShot->setVirtualPictureDataSize((int)size);  
        monServeurCppFMPictureScreenShot->WriteVirtualPictureStructToMapFile(maVirtualPictureScreenShot);
		//cout << "size : " << size << endl;
        cSleepMs(1);
	}
	else
	{
		cout << "Erreur d ecriture de l image" << endl;
	}

    if (Buffer)
    {
        free(Buffer);
        Buffer = nullptr;
    }

	// Deverrouiller le mutex
	monServeurCppFMPictureScreenShot->setVirtualPictureMutexBlocAccess(false);


}
double getRandom01_2Decimals() {
    int r = std::rand() % 101; // Valeur entière entre 0 et 100
    return r / 100.0;
}