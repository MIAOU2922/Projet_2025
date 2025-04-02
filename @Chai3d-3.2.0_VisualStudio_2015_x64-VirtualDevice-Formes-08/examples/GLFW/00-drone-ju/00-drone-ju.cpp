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

using namespace chai3d;
using namespace std;

cStereoMode stereoMode = C_STEREO_DISABLED;
cWorld* world;
cCamera* camera;
cDirectionalLight* light;
cBackground* backgroundWidget;
cImagePtr backgroundImage;
cShapeSphere* sphere;
cLevel* levelVelocity;
cDial* dialPosX;
cDial* dialPosY;
cDial* dialPosZ;
cFrequencyCounter freqCounterGraphics;
GLFWwindow* window = NULL;
int width = 640;
int height = 360;
int swapInterval = 1;

const int IMAGE_WIDTH  = 1080;
const int IMAGE_HEIGHT = 720;
unsigned char backgroundImageData[IMAGE_WIDTH * IMAGE_HEIGHT * 3];

cFileMappingPictureServeur* monServeurCppFMPictureScreenShot = NULL;
cFileMappingPictureClient* monClientCppFMPictureScreenShot = NULL;

cVirtualPicture* maVirtualPictureScreenShot = NULL;

void updateGraphics(void);
void close(void);
void errorCallback(int error, const char* a_description);
void updateBackgroundImage(void);

int main(int argc, char* argv[]) {
    srand((unsigned)time(NULL));
    memset(backgroundImageData, 0, sizeof(backgroundImageData));
    
    if (!glfwInit()) {
        cout << "failed initialization" << endl;
        cSleepMs(1000);
        return 1;
    }
    
    window = glfwCreateWindow(width, height, "IHM drone", NULL, NULL);
    if (!window) {
        cout << "failed to create window" << endl;
        cSleepMs(1000);
        glfwTerminate();
        return 1;
    }
    glfwMakeContextCurrent(window);
    glfwSwapInterval(swapInterval);
    
    world = new cWorld();
    world->m_backgroundColor.setWhite();
    
    camera = new cCamera(world);
    world->addChild(camera);
    camera->setUseTexture(true);
    camera->setUseMultipassTransparency(true);
    camera->set(cVector3d(0.5, 0.0, 0.0), cVector3d(0.0, 0.0, 0.0), cVector3d(0.0, 0.0, 1.0));
    camera->setClippingPlanes(0.01, 100.0);
    camera->setStereoMode(stereoMode);
    camera->setStereoEyeSeparation(0.005);
    camera->setStereoFocalLength(0.5);
    
    backgroundWidget = new cBackground();
    backgroundImage = cImage::create();
    backgroundImage->allocate(IMAGE_WIDTH, IMAGE_HEIGHT, 24);
    backgroundWidget->loadFromImage(backgroundImage);
    camera->m_backLayer->addChild(backgroundWidget);
    
    light = new cDirectionalLight(world);
    world->addChild(light);
    light->setEnabled(true);
    light->setDir(-1.0, 0.0, 0.0);
    
    sphere = new cShapeSphere(0.01);
    sphere->m_material->setRed();
    sphere->setLocalPos(0.0, 0.0, 0.0);
    world->addChild(sphere);
    
    monClientCppFMPictureScreenShot = new cFileMappingPictureClient(false);
    monClientCppFMPictureScreenShot->OpenClient("img_java_to_c");

    monServeurCppFMPictureScreenShot = new cFileMappingPictureServeur(false);
    monServeurCppFMPictureScreenShot->OpenServer("img_c_to_java");
    maVirtualPictureScreenShot = new cVirtualPicture();
    
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

void errorCallback(int a_error, const char* a_description) {
    cout << "Error: " << a_description << endl;
}

void close(void) {
    delete world;
}

void updateGraphics(void) {
    updateBackgroundImage();
    camera->renderView(width, height);
    glFinish();
}

void updateBackgroundImage(void) {
    maVirtualPictureScreenShot->MutexBlocAccess = true;
    unsigned char* Buffer = (unsigned char*)malloc(500000 * sizeof(unsigned char));
    unsigned int size = 0;
    int ret = cSaveJPG(backgroundImage.get(), &Buffer, &size);
    maVirtualPictureScreenShot->MutexBlocAccess = false;

    CopyMemory((unsigned char*)maVirtualPictureScreenShot->PictureData, (unsigned char*)Buffer, size);
    monServeurCppFMPictureScreenShot->WriteVirtualPictureStructToMapFile(maVirtualPictureScreenShot);
    Sleep(1);
    free(Buffer);

    backgroundWidget->loadFromImage(backgroundImage);
}