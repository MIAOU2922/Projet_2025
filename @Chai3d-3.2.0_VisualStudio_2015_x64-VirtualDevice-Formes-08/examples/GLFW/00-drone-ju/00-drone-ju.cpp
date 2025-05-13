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


cDial    *dialVal0, *dialVal1, *dialVal2, *dialVal3, *dialVal4, *dialVal5, *dialVal6, *dialVal7, *dialVal8, *dialVal9, *dialVal10, *dialVal11, *dialVal12, *dialVal13, *dialVal14, *dialVal15, *dialVal16, *dialVal17, *dialVal18, *dialVal19;
cLabel   *labelVal0, *labelVal1, *labelVal2, *labelVal3, *labelVal4, *labelVal5, *labelVal6, *labelVal7, *labelVal8, *labelVal9, *labelVal10, *labelVal11, *labelVal12, *labelVal13, *labelVal14, *labelVal15, *labelVal16, *labelVal17, *labelVal18, *labelVal19, *labelValX;

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

    monClientCppFMDCTelemetry = new cFileMappingDroneCharTelemetryClient(false);
    monClientCppFMDCTelemetry->setDebugMode(false);
    monClientCppFMDCTelemetry->OpenClient("telemetrie_java_to_c");
    
    maVirtualPictureScreenShot = new cVirtualPicture();


	cSleepMs(100);

    //-------------------------------
    font = NEW_CFONTCALIBRI20();
    // dial

    dialVal0 = new cDial(); 
    camera->m_frontLayer->addChild(dialVal0);
    dialVal0->setLocalPos(50, 650);
    dialVal0->setRange(-0.1, 0.1);
    dialVal0->setSize(40);
    dialVal0->setSingleIncrementDisplay(true);

    labelVal0 = new cLabel(font);
    dialVal0->addChild(labelVal0); 
    labelVal0->setLocalPos(0, 50);
    labelVal0->setText("val0: 0.00");

    //-------------------------------
    dialVal1 = new cDial();
    camera->m_frontLayer->addChild(dialVal1);
    dialVal1->setLocalPos(  150, 650 );      // ajustez X,Y
    dialVal1->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal1->setSize(       40 );
    dialVal1->setSingleIncrementDisplay(true);

    labelVal1 = new cLabel(font);
    dialVal1->addChild(labelVal1);
    labelVal1->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal1->setText("val1: 0.00");

    //-------------------------------
    dialVal2 = new cDial();
    camera->m_frontLayer->addChild(dialVal2);
    dialVal2->setLocalPos(  250, 650 );      // ajustez X,Y
    dialVal2->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal2->setSize(       40 );
    dialVal2->setSingleIncrementDisplay(true);

    labelVal2 = new cLabel(font);
    dialVal2->addChild(labelVal2);
    labelVal2->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal2->setText("val2: 0.00");

    //-------------------------------
    dialVal3 = new cDial();
    camera->m_frontLayer->addChild(dialVal3);
    dialVal3->setLocalPos(  350, 650 );      // ajustez X,Y
    dialVal3->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal3->setSize(       40 );
    dialVal3->setSingleIncrementDisplay(true);

    labelVal3 = new cLabel(font);
    dialVal3->addChild(labelVal3);
    labelVal3->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal3->setText("val3: 0.00");

    //-------------------------------
    dialVal4 = new cDial();
    camera->m_frontLayer->addChild(dialVal4);
    dialVal4->setLocalPos(  50, 550 );      // ajustez X,Y
    dialVal4->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal4->setSize(       40 );
    dialVal4->setSingleIncrementDisplay(true);

    labelVal4 = new cLabel(font);
    dialVal4->addChild(labelVal4);
    labelVal4->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal4->setText("val4: 0.00");

    //-------------------------------
    dialVal5 = new cDial();
    camera->m_frontLayer->addChild(dialVal5);
    dialVal5->setLocalPos(  150, 550 );      // ajustez X,Y
    dialVal5->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal5->setSize(       40 );
    dialVal5->setSingleIncrementDisplay(true);

    labelVal5 = new cLabel(font);
    dialVal5->addChild(labelVal5);
    labelVal5->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal5->setText("val5: 0.00");

    //-------------------------------
    dialVal6 = new cDial();
    camera->m_frontLayer->addChild(dialVal6);
    dialVal6->setLocalPos(  250, 550 );      // ajustez X,Y
    dialVal6->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal6->setSize(       40 );
    dialVal6->setSingleIncrementDisplay(true);

    labelVal6 = new cLabel(font);
    dialVal6->addChild(labelVal6);
    labelVal6->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal6->setText("val6: 0.00");
    
    //-------------------------------
    dialVal7 = new cDial();
    camera->m_frontLayer->addChild(dialVal7);
    dialVal7->setLocalPos(  350, 550 );      // ajustez X,Y
    dialVal7->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal7->setSize(       40 );
    dialVal7->setSingleIncrementDisplay(true);

    labelVal7 = new cLabel(font);
    dialVal7->addChild(labelVal7);
    labelVal7->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal7->setText("val7: 0.00");

    //-------------------------------
    dialVal8 = new cDial();
    camera->m_frontLayer->addChild(dialVal8);
    dialVal8->setLocalPos(  50, 450 );      // ajustez X,Y
    dialVal8->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal8->setSize(       40 );
    dialVal8->setSingleIncrementDisplay(true);

    labelVal8 = new cLabel(font);
    dialVal8->addChild(labelVal8);
    labelVal8->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal8->setText("val8: 0.00");
    
    //-------------------------------
    dialVal9 = new cDial();
    camera->m_frontLayer->addChild(dialVal9);
    dialVal9->setLocalPos(  150, 450 );      // ajustez X,Y
    dialVal9->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal9->setSize(       40 );
    dialVal9->setSingleIncrementDisplay(true);
    
    labelVal9 = new cLabel(font);
    dialVal9->addChild(labelVal9);
    labelVal9->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal9->setText("val9: 0.00");
    
    //-------------------------------
    dialVal10 = new cDial();
    camera->m_frontLayer->addChild(dialVal10);
    dialVal10->setLocalPos(  250, 450 );      // ajustez X,Y
    dialVal10->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal10->setSize(       40 );
    dialVal10->setSingleIncrementDisplay(true);
    
    labelVal10 = new cLabel(font);
    dialVal10->addChild(labelVal10);
    labelVal10->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal10->setText("val10: 0.00");
    
    //-------------------------------
    dialVal11 = new cDial();
    camera->m_frontLayer->addChild(dialVal11);
    dialVal11->setLocalPos(  350, 450 );      // ajustez X,Y
    dialVal11->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal11->setSize(       40 );
    dialVal11->setSingleIncrementDisplay(true);
    
    labelVal11 = new cLabel(font);
    dialVal11->addChild(labelVal11);
    labelVal11->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal11->setText("val11: 0.00");
    
    //-------------------------------
    dialVal12 = new cDial();
    camera->m_frontLayer->addChild(dialVal12);
    dialVal12->setLocalPos(  50, 350 );      // ajustez X,Y
    dialVal12->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal12->setSize(       40 );
    dialVal12->setSingleIncrementDisplay(true);
    
    labelVal12 = new cLabel(font);
    dialVal12->addChild(labelVal12);
    labelVal12->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal12->setText("val12: 0.00");
    
    //-------------------------------
    dialVal13 = new cDial();
    camera->m_frontLayer->addChild(dialVal13);
    dialVal13->setLocalPos(  150, 350 );      // ajustez X,Y
    dialVal13->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal13->setSize(       40 );
    dialVal13->setSingleIncrementDisplay(true);
    
    labelVal13 = new cLabel(font);
    dialVal13->addChild(labelVal13);
    labelVal13->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal13->setText("val13: 0.00");
    
    //-------------------------------
    dialVal14 = new cDial();
    camera->m_frontLayer->addChild(dialVal14);
    dialVal14->setLocalPos(  250, 350 );      // ajustez X,Y
    dialVal14->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal14->setSize(       40 );
    dialVal14->setSingleIncrementDisplay(true);
    
    labelVal14 = new cLabel(font);
    dialVal14->addChild(labelVal14);
    labelVal14->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
    labelVal14->setText("val14: 0.00");
    
    //-------------------------------
    dialVal15 = new cDial();
    camera->m_frontLayer->addChild(dialVal15);
    dialVal15->setLocalPos(  350, 350 );      // ajustez X,Y
    dialVal15->setRange(   -0.1, 0.1 );      // ajustez plage si besoin
    dialVal15->setSize(       40 );
    dialVal15->setSingleIncrementDisplay(true);
    
    labelVal15 = new cLabel(font);
    dialVal15->addChild(labelVal15);
    labelVal15->setLocalPos(  0,  50 );           // position du label au-dessus du cadran
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
        dialVal0->setValue(v);
        std::ostringstream oss; oss << "val0: " << std::fixed << std::setprecision(2) << v;
        labelVal0->setText(oss.str());
    }

    // Valeur 1
    {
        double v = monClientCppFMDCTelemetry->get_val_1();
        dialVal1->setValue(v);
        std::ostringstream oss; oss << "val1: " << std::fixed << std::setprecision(2) << v;
        labelVal1->setText(oss.str());
    }

    // Valeur 2
    {
        double v = monClientCppFMDCTelemetry->get_val_2();
        dialVal2->setValue(v);
        std::ostringstream oss; oss << "val2: " << std::fixed << std::setprecision(2) << v;
        labelVal2->setText(oss.str());
    }

    // Valeur 3
    {
        double v = monClientCppFMDCTelemetry->get_val_3();
        dialVal3->setValue(v);
        std::ostringstream oss; oss << "val3: " << std::fixed << std::setprecision(2) << v;
        labelVal3->setText(oss.str());
    }

    // Valeur 4
    {
        double v = monClientCppFMDCTelemetry->get_val_4();
        dialVal4->setValue(v);
        std::ostringstream oss; oss << "val4: " << std::fixed << std::setprecision(2) << v;
        labelVal4->setText(oss.str());
    }

    // Valeur 5
    {
        double v = monClientCppFMDCTelemetry->get_val_5();
        dialVal5->setValue(v);
        std::ostringstream oss; oss << "val5: " << std::fixed << std::setprecision(2) << v;
        labelVal5->setText(oss.str());
    }

    // Valeur 6
    {
        double v = monClientCppFMDCTelemetry->get_val_6();
        dialVal6->setValue(v);
        std::ostringstream oss; oss << "val6: " << std::fixed << std::setprecision(2) << v;
        labelVal6->setText(oss.str());
    }

    // Valeur 7
    {
        double v = monClientCppFMDCTelemetry->get_val_7();
        dialVal7->setValue(v);
        std::ostringstream oss; oss << "val7: " << std::fixed << std::setprecision(2) << v;
        labelVal7->setText(oss.str());
    }

    // Valeur 8
    {
        double v = monClientCppFMDCTelemetry->get_val_8();
        dialVal8->setValue(v);
        std::ostringstream oss; oss << "val8: " << std::fixed << std::setprecision(2) << v;
        labelVal8->setText(oss.str());
    }

    // Valeur 9
    {
        double v = monClientCppFMDCTelemetry->get_val_9();
        dialVal9->setValue(v);
        std::ostringstream oss; oss << "val9: " << std::fixed << std::setprecision(2) << v;
        labelVal9->setText(oss.str());
    }

    // Valeur 10
    {
        double v = monClientCppFMDCTelemetry->get_val_10();
        dialVal10->setValue(v);
        std::ostringstream oss; oss << "val10: " << std::fixed << std::setprecision(2) << v;
        labelVal10->setText(oss.str());
    }

    // Valeur 11
    {
        double v = monClientCppFMDCTelemetry->get_val_11();
        dialVal11->setValue(v);
        std::ostringstream oss; oss << "val11: " << std::fixed << std::setprecision(2) << v;
        labelVal11->setText(oss.str());
    }

    // Valeur 12
    {
        double v = monClientCppFMDCTelemetry->get_val_12();
        dialVal12->setValue(v);
        std::ostringstream oss; oss << "val12: " << std::fixed << std::setprecision(2) << v;
        labelVal12->setText(oss.str());
    }

    // Valeur 13
    {
        double v = monClientCppFMDCTelemetry->get_val_13();
        dialVal13->setValue(v);
        std::ostringstream oss; oss << "val13: " << std::fixed << std::setprecision(2) << v;
        labelVal13->setText(oss.str());
    }

    // Valeur 14
    {
        double v = monClientCppFMDCTelemetry->get_val_14();
        dialVal14->setValue(v);
        std::ostringstream oss; oss << "val14: " << std::fixed << std::setprecision(2) << v;
        labelVal14->setText(oss.str());
    }

    // Valeur 15
    {
        double v = monClientCppFMDCTelemetry->get_val_15();
        dialVal15->setValue(v);
        std::ostringstream oss; oss << "val15: " << std::fixed << std::setprecision(2) << v;
        labelVal15->setText(oss.str());
    }
}
