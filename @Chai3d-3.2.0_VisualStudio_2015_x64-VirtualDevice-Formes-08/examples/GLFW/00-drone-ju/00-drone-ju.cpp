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
#include <GL/gl.h> // pour les appels OpenGL

using namespace chai3d;
using namespace std;

//--------------------------------------------------------------//
// Variables globales

cStereoMode stereoMode = C_STEREO_DISABLED;

cWorld* world;
cCamera* camera;
cDirectionalLight* light;

cBackground* backgroundWidget;  // Widget de fond
cImagePtr backgroundImage;      // Objet cImage utilisé par le widget

// Autres objets de la scène
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

// Dimensions de l'image de fond (24 bits = 3 octets par pixel)
const int IMAGE_WIDTH  = 1080;
const int IMAGE_HEIGHT = 720;
unsigned char backgroundImageData[IMAGE_WIDTH * IMAGE_HEIGHT * 3];

//--------------------------------------------------------------//
// Prototypes des fonctions

void updateGraphics(void);
void close(void);
void errorCallback(int error, const char* a_description);
void updateBackgroundImage(void);

//--------------------------------------------------------------//
// Fonction principale

int main(int argc, char* argv[])
{
    srand((unsigned)time(NULL));
    memset(backgroundImageData, 0, sizeof(backgroundImageData));
    
    if (!glfwInit())
    {
        cout << "failed initialization" << endl;
        cSleepMs(1000);
        return 1;
    }
    
    glfwWindowHint(GLFW_STEREO, (stereoMode == C_STEREO_ACTIVE) ? GL_TRUE : GL_FALSE);
    glfwSetErrorCallback(errorCallback);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2);
    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1);
    glfwWindowHint(GLFW_RESIZABLE, GL_FALSE);
    
    window = glfwCreateWindow(width, height, "IHM drone", NULL, NULL);
    if (!window)
    {
        cout << "failed to create window" << endl;
        cSleepMs(1000);
        glfwTerminate();
        return 1;
    }
    glfwMakeContextCurrent(window);
    glfwSwapInterval(swapInterval);
    
    #ifdef GLEW_VERSION
    if (glewInit() != GLEW_OK)
    {
        cout << "failed to initialize GLEW library" << endl;
        glfwTerminate();
        return 1;
    }
    #endif
    
    // Création du monde (fond blanc)
    world = new cWorld();
    world->m_backgroundColor.setWhite();
    
    // Création de la caméra et ajout dans le monde
    camera = new cCamera(world);
    world->addChild(camera);
    camera->setUseTexture(true);
    camera->setUseMultipassTransparency(true);
    camera->set(cVector3d(0.5, 0.0, 0.0),    // position (eye)
                cVector3d(0.0, 0.0, 0.0),    // point visé (target)
                cVector3d(0.0, 0.0, 1.0));   // vecteur "up"
    camera->setClippingPlanes(0.01, 100.0);
    camera->setStereoMode(stereoMode);
    camera->setStereoEyeSeparation(0.005);
    camera->setStereoFocalLength(0.5);
    
    // Création du widget de fond et allocation de l'image en 24 bits
    backgroundWidget = new cBackground();
    backgroundImage = cImage::create();
    if (!backgroundImage->allocate(IMAGE_WIDTH, IMAGE_HEIGHT, 24))
    {
        cout << "Erreur d'allocation de backgroundImage en 24 bits" << endl;
        
        backgroundImage = cImage::create();
        if (!backgroundImage->allocate(IMAGE_WIDTH, IMAGE_HEIGHT, 32))
        {
        cout << "Erreur d'allocation de backgroundImage en 32 bits" << endl;
        }else{
        cout << "Allocation reussi de backgroundImage en 32 bits" << endl;
        }
    }else{
       cout << "Allocation reussi de backgroundImage en 24 bits" << endl;
    }
    // Chargement initial dans le widget (une seule fois)
    backgroundWidget->loadFromImage(backgroundImage);
    camera->m_backLayer->addChild(backgroundWidget);
    
    // Création d'une lumière directionnelle
    light = new cDirectionalLight(world);
    world->addChild(light);
    light->setEnabled(true);
    light->setDir(-1.0, 0.0, 0.0);
    
    // Création d'une sphère rouge
    sphere = new cShapeSphere(0.01);
    sphere->m_material->setRed();
    sphere->setLocalPos(0.0, 0.0, 0.0);
    world->addChild(sphere);
    
    while (!glfwWindowShouldClose(window))
    {
        glfwGetWindowSize(window, &width, &height);
        updateGraphics();
        glfwSwapBuffers(window);
        glfwPollEvents();
        freqCounterGraphics.signal(1);
    }
    
    close();
    return 0;
}

//--------------------------------------------------------------//
// Callback d'erreur GLFW

void errorCallback(int a_error, const char* a_description)
{
    cout << "Error: " << a_description << endl;
}

//--------------------------------------------------------------//
// Fonction de fermeture

void close(void)
{
    delete world;
}

//--------------------------------------------------------------//
// Fonction de rendu de la scène

void updateGraphics(void)
{
    // Mise à jour de l'image de fond avec des valeurs aléatoires
    updateBackgroundImage();
    
    // Rendu de la scène (le widget de fond est dans le back layer)
    camera->renderView(width, height);
    glFinish();
}

//--------------------------------------------------------------//
// Fonction de mise à jour de l'image de fond
// Supposons que backgroundImageData[] a déjà été rempli via filemapping
// (les dimensions et le format doivent correspondre à IMAGE_WIDTH x IMAGE_HEIGHT en 24 bits)

// Copie des données dans le cImage
void updateBackgroundImage(void)
{
    // Remplir backgroundImageData avec des valeurs aléatoires
    for (int i = 0; i < IMAGE_WIDTH * IMAGE_HEIGHT * 3; i++) {
        backgroundImageData[i] = rand() % 256;
    }

    // Copie des données dans le cImage
    if (backgroundImage->getData() != nullptr)
    {
        memcpy(backgroundImage->getData(), backgroundImageData, IMAGE_WIDTH * IMAGE_HEIGHT * 3 * sizeof(unsigned char));
    }
    else
    {
        cout << "Erreur : backgroundImage->getData() retourne NULL" << endl;
    }

    // Recharge le widget avec la nouvelle image
    backgroundWidget->loadFromImage(backgroundImage);
}