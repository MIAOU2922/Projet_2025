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
#include "cFileMappingDroneCharTelemetryClient.h"
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

//-----------------------------


cDial    *dialVal7, *dialVal8, *dialVal9;
cLabel   *labelVal0, *labelVal1, *labelVal2, *labelVal3, *labelVal4, *labelVal5, *labelVal6, *labelVal7, *labelVal8, *labelVal9, *labelVal10, *labelVal11, *labelVal12, *labelVal13, *labelVal14, *labelVal15;
cLevel   *levelVal0, *levelVal1, *levelVal2, *levelVal3, *levelVal4, *levelVal5, *levelVal6, *levelVal10, *levelVal11, *levelVal12, *levelVal13 ;

cFontPtr font;

//------------------------------
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

cFileMappingDroneCharTelemetryClient* monClientCppFMDCTelemetry = NULL;

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
void updateTelemetry(void);

int main(int argc, char* argv[]) {
    //--------------------------------------------------------------------------
    // OPEN GL - WINDOW DISPLAY
    //--------------------------------------------------------------------------

    if (!glfwInit()) {
        cout << "failed initialization" << endl;
        cSleepMs(1000);
        return 1;
    }
    glfwSetErrorCallback(errorCallback);

    // blocage du redimensionnement
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
    // version OpenGL
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

    // mode stéréo
    glfwWindowHint(GLFW_STEREO, (stereoMode == C_STEREO_ACTIVE) ? GL_TRUE : GL_FALSE);

    // création de la fenêtre en dimensions fixes
    window = glfwCreateWindow(IMAGE_WIDTH, IMAGE_HEIGHT, "drone", NULL, NULL);
    if (!window) {
        cout << "failed to create window" << endl;
        cSleepMs(1000);
        glfwTerminate();
        return 1;
    }

    // centrage de la fenêtre
    const GLFWvidmode* mode = glfwGetVideoMode(glfwGetPrimaryMonitor());
    int posX = (mode->width - IMAGE_WIDTH) / 2;
    int posY = (mode->height - IMAGE_HEIGHT) / 2;
    glfwSetWindowPos(window, posX, posY);

    // callbacks
    glfwSetKeyCallback(window, keyCallback);
    glfwSetWindowSizeCallback(window, windowSizeCallback);
    glfwMakeContextCurrent(window);
    glfwSwapInterval(swapInterval);

#ifdef GLEW_VERSION
    if (glewInit() != GLEW_OK) {
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
    backgroundImage->allocate(IMAGE_WIDTH*2, IMAGE_HEIGHT*2, GL_RGB, GL_UNSIGNED_INT);
    ContextImage->allocate(width*5, height*5, GL_RGB, GL_UNSIGNED_INT);
    ContextImageByteArray = new unsigned char[width * height * 3];

    glViewport(0, 0, width, height);



    light = new cDirectionalLight(world);
    world->addChild(light);
    light->setEnabled(true);
    light->setDir(-1.0, 0.0, 0.0);
    
    sphere = new cShapeSphere(0.05);
    sphere->m_material->setRed();
    sphere->setLocalPos(0.0, 0.0, 0.0);
    world->addChild(sphere);
    
    monClientCppFMPictureScreenShot = new cFileMappingPictureClient(false);
    monClientCppFMPictureScreenShot->setDebugMode(false);
    monClientCppFMPictureScreenShot->OpenClient("img_java_to_c");

    monServeurCppFMPictureScreenShot = new cFileMappingPictureServeur(false);
    monServeurCppFMPictureScreenShot->setDebugMode(false);
    monServeurCppFMPictureScreenShot->OpenServer("img_c_to_java");

    monClientCppFMDCTelemetry = new cFileMappingDroneCharTelemetryClient(false);
    monClientCppFMDCTelemetry->setDebugMode(false);
    monClientCppFMDCTelemetry->OpenClient("telemetrie_java_to_c");
    
    maVirtualPictureScreenShot = new cVirtualPicture();


	cSleepMs(100);

    //-------------------------------
    font = NEW_CFONTCALIBRI20();

        //------------------------------
    // dist
    levelVal0 = new cLevel();
    camera->m_frontLayer->addChild(levelVal0);
    levelVal0->setLocalPos(750, 45);
    levelVal0->setRange(-5.0, 10000.0);
    levelVal0->setWidth(40);
    levelVal0->setNumIncrements(46);
    levelVal0->setSingleIncrementDisplay(false);
    levelVal0->setTransparencyLevel(0.5);

    labelVal0 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal0); 
    labelVal0->setLocalPos(750, 10);
    labelVal0->setText("val0: 0.00");

    //-------------------------------
    // temp
    levelVal1 = new cLevel();
    camera->m_frontLayer->addChild(levelVal1);
    levelVal1->setLocalPos(250, 45);
    levelVal1->setRange(-25.0, 120.0);
    levelVal1->setWidth(40);
    levelVal1->setNumIncrements(46);
    levelVal1->setSingleIncrementDisplay(false);
    levelVal1->setTransparencyLevel(0.5);

    labelVal1 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal1);
    labelVal1->setLocalPos(  250,  10 );
    labelVal1->setText("val1: 0.00");

    //-------------------------------
    // alt
    levelVal2 = new cLevel();
    camera->m_frontLayer->addChild(levelVal2);
    levelVal2->setLocalPos(400, 45);
    levelVal2->setRange(-10.0, 500.0);
    levelVal2->setWidth(40);
    levelVal2->setNumIncrements(46);
    levelVal2->setSingleIncrementDisplay(false);
    levelVal2->setTransparencyLevel(0.5);

    labelVal2 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal2);
    labelVal2->setLocalPos(400, 10);
    labelVal2->setText("val2: 0.00");

    //-------------------------------
    // baro
    levelVal3 = new cLevel();
    camera->m_frontLayer->addChild(levelVal3);
    levelVal3->setLocalPos(300, 45);
    levelVal3->setRange(-100.0, 1500.0);
    levelVal3->setWidth(40);
    levelVal3->setNumIncrements(46);
    levelVal3->setSingleIncrementDisplay(false);
    levelVal3->setTransparencyLevel(0.5);

    labelVal3 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal3);
    labelVal3->setLocalPos(  300,  10 );
    labelVal3->setText("val3: 0.00");

    //-------------------------------
    //aceleration x
    levelVal4 = new cLevel();
    camera->m_frontLayer->addChild(levelVal4);
    levelVal4->setLocalPos(900, 45);
    levelVal4->setRange(-5.0, 10.0);
    levelVal4->setWidth(40);
    levelVal4->setNumIncrements(46);
    levelVal4->setSingleIncrementDisplay(false);
    levelVal4->setTransparencyLevel(0.5);


    labelVal4 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal4);
    labelVal4->setLocalPos(  900,  10 );
    labelVal4->setText("val4: 0.00");

    //-------------------------------
    //aceleration y
    levelVal5 = new cLevel();
    camera->m_frontLayer->addChild(levelVal5);
    levelVal5->setLocalPos(950, 45);
    levelVal5->setRange(-5.0, 10.0);
    levelVal5->setWidth(40);
    levelVal5->setNumIncrements(46);
    levelVal5->setSingleIncrementDisplay(false);
    levelVal5->setTransparencyLevel(0.5);

    labelVal5 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal5);
    labelVal5->setLocalPos(  950,  10);
    labelVal5->setText("val5: 0.00");

    //-------------------------------
    //aceleration z
    levelVal6 = new cLevel();
    camera->m_frontLayer->addChild(levelVal6);
    levelVal6->setLocalPos(1000, 45);
    levelVal6->setRange(-5.0, 10.0);
    levelVal6->setWidth(40);
    levelVal6->setNumIncrements(46);
    levelVal6->setSingleIncrementDisplay(false);
    levelVal6->setTransparencyLevel(0.5);


    labelVal6 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal6);
    labelVal6->setLocalPos(  1000,  10 );
    labelVal6->setText("val6: 0.00");
    
    //-------------------------------
    // gyro x
    dialVal7 = new cDial();
    camera->m_frontLayer->addChild(dialVal7);
    dialVal7->setLocalPos(  900+20, 260 );
    dialVal7->setRange(-180.0, 180.0);
    dialVal7->setSize(       40 );
    dialVal7->setSingleIncrementDisplay(true);

    labelVal7 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal7);
    labelVal7->setLocalPos(  900,  280 );
    labelVal7->setText("val7: 0.00");

    //-------------------------------
    // gyro y
    dialVal8 = new cDial();
    camera->m_frontLayer->addChild(dialVal8);
    dialVal8->setLocalPos(  950+20, 260 );
    dialVal8->setRange(-180.0, 180.0);
    dialVal8->setSize(       40 );
    dialVal8->setSingleIncrementDisplay(true);

    labelVal8 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal8);
    labelVal8->setLocalPos(  950,  280 );
    labelVal8->setText("val8: 0.00");
    
    //-------------------------------
    // gyro z
    dialVal9 = new cDial();
    camera->m_frontLayer->addChild(dialVal9);
    dialVal9->setLocalPos(  1000+20, 260 );
    dialVal9->setRange(-180.0, 180.0);
    dialVal9->setSize(       40 );
    dialVal9->setSingleIncrementDisplay(true);
    
    labelVal9 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal9);
    labelVal9->setLocalPos(  1000,  280 );
    labelVal9->setText("val9: 0.00");
    
    //-------------------------------
    // lat
    labelVal10 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal10);
    labelVal10->setLocalPos(  600,  700 );
    labelVal10->setText("val10: 0.00");
    
    //-------------------------------
    // lon
    labelVal11 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal11);
    labelVal11->setLocalPos(  500,  700 );
    labelVal11->setText("val11: 0.00");
    
    //-------------------------------
    // gnss_alt
    levelVal12 = new cLevel();
    camera->m_frontLayer->addChild(levelVal12);
    levelVal12->setLocalPos(450, 45);
    levelVal12->setRange(-10.0, 500.0);
    levelVal12->setWidth(40);
    levelVal12->setNumIncrements(46);
    levelVal12->setSingleIncrementDisplay(false);
    levelVal12->setTransparencyLevel(0.5);

    labelVal12 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal12);
    labelVal12->setLocalPos(  450,  10 );
    labelVal12->setText("val12: 0.00");
    
    //-------------------------------
    // speed
    levelVal13 = new cLevel();
    camera->m_frontLayer->addChild(levelVal13);
    levelVal13->setLocalPos(700, 45);
    levelVal13->setRange(-25.0, 100.0);
    levelVal13->setWidth(40);
    levelVal13->setNumIncrements(46);
    levelVal13->setSingleIncrementDisplay(false);
    levelVal13->setTransparencyLevel(0.5);

    labelVal13 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal13);
    labelVal13->setLocalPos(  700,  10 );
    labelVal13->setText("val13: 0.00");
    
    //-------------------------------
    // satel
    labelVal14 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal14);
    labelVal14->setLocalPos(  500,  680 );
    labelVal14->setText("val14: 0.00");
    
    //-------------------------------
    // time
    labelVal15 = new cLabel(font);
    camera->m_frontLayer->addChild(labelVal15);
    labelVal15->setLocalPos(  1000,  700 );
    labelVal15->setText("val15: 0.00");





    // call window size callback at initialization
    windowSizeCallback(window, width, height);

	// attendre 1 seconde avant de commencer
    cSleepMs(100);


    while (!glfwWindowShouldClose(window)) {
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

    updateTelemetry();




    //--------------------------------------

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
        //cout << "Image ecrite avec succes: " << maVirtualPictureScreenShot << endl;
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
void updateTelemetry()
{
    // Valeur 0
    {
        double v = monClientCppFMDCTelemetry->get_val_0();
        levelVal0->setValue(v);
        std::ostringstream oss; oss << "dist: \n" << std::fixed << std::setprecision(2) << v;
        labelVal0->setText(oss.str());
    }

    // Valeur 1
    {
        double v = monClientCppFMDCTelemetry->get_val_1();
        levelVal1->setValue(v);
        std::ostringstream oss; oss << "temp: \n" << std::fixed << std::setprecision(2) << v;
        labelVal1->setText(oss.str());
    }

    // Valeur 2
    {
        double v = monClientCppFMDCTelemetry->get_val_2();
        levelVal2->setValue(v);
        std::ostringstream oss; oss << "alt: \n" << std::fixed << std::setprecision(2) << v;
        labelVal2->setText(oss.str());
    }

    // Valeur 3
    {
        double v = monClientCppFMDCTelemetry->get_val_3();
        levelVal3->setValue(v);
        std::ostringstream oss; oss << "baro: \n" << std::fixed << std::setprecision(2) << v;
        labelVal3->setText(oss.str());
    }

    // Valeur 4
    {
        double v = monClientCppFMDCTelemetry->get_val_4();
        levelVal4->setValue(v);
        std::ostringstream oss; oss << "agx: \n" << std::fixed << std::setprecision(2) << v;
        labelVal4->setText(oss.str());
    }

    // Valeur 5
    {
        double v = monClientCppFMDCTelemetry->get_val_5();
        levelVal5->setValue(v);
        std::ostringstream oss; oss << "agy: \n" << std::fixed << std::setprecision(2) << v;
        labelVal5->setText(oss.str());
    }

    // Valeur 6
    {
        double v = monClientCppFMDCTelemetry->get_val_6();
        levelVal6->setValue(v);
        std::ostringstream oss; oss << "agz: \n" << std::fixed << std::setprecision(2) << v;
        labelVal6->setText(oss.str());
    }

    // Valeur 7
    {
        double v = monClientCppFMDCTelemetry->get_val_7();
        dialVal7->setValue(v);
        std::ostringstream oss; oss << "gyrox: \n" << std::fixed << std::setprecision(2) << v;
        labelVal7->setText(oss.str());
    }

    // Valeur 8
    {
        double v = monClientCppFMDCTelemetry->get_val_8();
        dialVal8->setValue(v);
        std::ostringstream oss; oss << "gyroy: \n" << std::fixed << std::setprecision(2) << v;
        labelVal8->setText(oss.str());
    }

    // Valeur 9
    {
        double v = monClientCppFMDCTelemetry->get_val_9();
        dialVal9->setValue(v);
        std::ostringstream oss; oss << "gyroz: \n" << std::fixed << std::setprecision(2) << v;
        labelVal9->setText(oss.str());
    }

    // Valeur 10
    {
        double v = monClientCppFMDCTelemetry->get_val_10();
        std::ostringstream oss; oss << "lat: " << std::fixed << std::setprecision(2) << v;
        labelVal10->setText(oss.str());
    }

    // Valeur 11
    {
        double v = monClientCppFMDCTelemetry->get_val_11();
        std::ostringstream oss; oss << "lon: " << std::fixed << std::setprecision(2) << v;
        labelVal11->setText(oss.str());
    }

    // Valeur 12
    {
        double v = monClientCppFMDCTelemetry->get_val_12();
        levelVal12->setValue(v);
        std::ostringstream oss; oss << "gnss_alt: \n" << std::fixed << std::setprecision(2) << v;
        labelVal12->setText(oss.str());
    }

    // Valeur 13
    {
        double v = monClientCppFMDCTelemetry->get_val_13();
        levelVal13->setValue(v);
        std::ostringstream oss; oss << "speed: \n" << std::fixed << std::setprecision(2) << v;
        labelVal13->setText(oss.str());
    }

    // Valeur 14
    {
        double v = monClientCppFMDCTelemetry->get_val_14();
        std::ostringstream oss; oss << "satel: " << std::fixed << std::setprecision(2) << v;
        labelVal14->setText(oss.str());
    }

    // Valeur 15
    {
        double v = monClientCppFMDCTelemetry->get_val_15();
        std::ostringstream oss; oss << "time: " << std::fixed << std::setprecision(2) << v;
        labelVal15->setText(oss.str());
    }

    sphere->setLocalPos(monClientCppFMDCTelemetry->get_val_4(), monClientCppFMDCTelemetry->get_val_5(), monClientCppFMDCTelemetry->get_val_6());
}
