/**
 * -------------------------------------------------------------------
 * Nom du fichier : 00-drone-ju.cpp
 * Auteur         : Votre Nom
 * Version        : 1.0
 * Date           : 10/03/2025
 * Description    : IHM chai3d
 * -------------------------------------------------------------------
 */

#include "chai3d.h"
#include <GLFW/glfw3.h>
#include <iostream>

using namespace chai3d;
using namespace std;

//--------------------------------------------------------------//
// variables globales

cStereoMode stereoMode = C_STEREO_DISABLED;

cWorld* world;
cCamera* camera;
cDirectionalLight* light;
cMesh* imagedrone;

cShapeSphere* sphere;

cLevel* levelVelocity;

cDial* dialPosX;
cDial* dialPosY;
cDial* dialPosZ;

cFrequencyCounter freqCounterGraphics;

GLFWwindow* window = NULL;
int width = 1280;
int height = 720;

int swapInterval = 1;

//--------------------------------------------------------------//
// fonctions

void updateGraphics(void);

void close(void);

// callback when an error GLFW occurs
void errorCallback(int error, const char* a_description);

//--------------------------------------------------------------//
//initialisation

int main(int argc, char* argv[]){
    if (!glfwInit()){
        cout << "failed initialization" << endl;
        cSleepMs(1000);
        return 1;
    }

    // set active stereo mode
    if (stereoMode == C_STEREO_ACTIVE)
    {
        glfwWindowHint(GLFW_STEREO, GL_TRUE);
    }
    else
    {
        glfwWindowHint(GLFW_STEREO, GL_FALSE);
    }

    // set error callback
    glfwSetErrorCallback(errorCallback);

    // set OpenGL version
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);

    // create display context
    window = glfwCreateWindow(width, height, "IHM drone", NULL, NULL);
    if (!window){
        cout << "failed to create window" << endl;
        cSleepMs(1000);
        glfwTerminate();
        return 1;
    }

    glfwMakeContextCurrent(window);

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

     // create a new world.
    world = new cWorld();

    // set the background color of the environment
    world->m_backgroundColor.setBlack();

    // create a camera and insert it into the virtual world
    camera = new cCamera(world);
    world->addChild(camera);

    // position and orient the camera
    camera->set(cVector3d(0.5, 0.0, 0.0),    // camera position (eye)
                cVector3d(0.0, 0.0, 0.0),    // look at position (target)
                cVector3d(0.0, 0.0, 1.0));   // direction of the (up) vector

    // set the near and far clipping planes of the camera
    camera->setClippingPlanes(0.01, 10.0);

    // set stereo mode
    camera->setStereoMode(stereoMode);

    // set stereo eye separation and focal length (applies only if stereo is enabled)
    camera->setStereoEyeSeparation(0.005);
    camera->setStereoFocalLength(0.5);

    // create a directional light source
    light = new cDirectionalLight(world);

    // insert light source inside world
    world->addChild(light);

    // enable light source
    light->setEnabled(true);

    // define direction of light beam
    light->setDir(-1.0, 0.0, 0.0);

    // create a sphere
    sphere = new cShapeSphere(0.01);
    sphere->m_material->setRed();
    sphere->setLocalPos(0.0, 0.0, 0.0);

    // insert sphere inside world
    world->addChild(sphere);

    while (true)
    {
        // get width and height of window
        glfwGetWindowSize(window, &width, &height);

        // swap buffers
        glfwSwapBuffers(window);

        // process events
        glfwPollEvents();

        // signal frequency counter
        freqCounterGraphics.signal(1);
    }
}

//--------------------------------------------------------------//
// callback when an error GLFW occurs
void errorCallback(int a_error, const char* a_description)
{
    cout << "Error: " << a_description << endl;
}
void close(void)
{
    // delete resources
    delete world;
}