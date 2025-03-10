/********************************************************************************
** Form generated from reading UI file 'Interface.ui'
**
** Created by: Qt User Interface Compiler version 5.9.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_INTERFACE_H
#define UI_INTERFACE_H

#include <QtCore/QVariant>
#include <QtWidgets/QAction>
#include <QtWidgets/QApplication>
#include <QtWidgets/QButtonGroup>
#include <QtWidgets/QGridLayout>
#include <QtWidgets/QGroupBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QSlider>
#include <QtWidgets/QSpacerItem>
#include <QtWidgets/QStatusBar>
#include <QtWidgets/QToolBar>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_InterfaceClass
{
public:
    QAction *actionShow_Settings;
    QAction *actionFull_Screen;
    QWidget *centralWidget;
    QHBoxLayout *horizontalLayout;
    QWidget *Settings;
    QVBoxLayout *verticalLayout;
    QGroupBox *groupHapticLin;
    QGridLayout *gridLayoutHaptic;
    QLabel *titleZoom;
    QSlider *sliderZoom;
    QLabel *labelZoom;
    QSpacerItem *verticalSpacer;
    QToolBar *toolBar;
    QStatusBar *statusBar;

    void setupUi(QMainWindow *InterfaceClass)
    {
        if (InterfaceClass->objectName().isEmpty())
            InterfaceClass->setObjectName(QStringLiteral("InterfaceClass"));
        InterfaceClass->resize(800, 600);
        QIcon icon;
        icon.addFile(QStringLiteral(":/chai3d.ico"), QSize(), QIcon::Normal, QIcon::Off);
        InterfaceClass->setWindowIcon(icon);
        actionShow_Settings = new QAction(InterfaceClass);
        actionShow_Settings->setObjectName(QStringLiteral("actionShow_Settings"));
        actionShow_Settings->setCheckable(true);
        actionFull_Screen = new QAction(InterfaceClass);
        actionFull_Screen->setObjectName(QStringLiteral("actionFull_Screen"));
        actionFull_Screen->setCheckable(true);
        centralWidget = new QWidget(InterfaceClass);
        centralWidget->setObjectName(QStringLiteral("centralWidget"));
        horizontalLayout = new QHBoxLayout(centralWidget);
        horizontalLayout->setSpacing(0);
        horizontalLayout->setContentsMargins(11, 11, 11, 11);
        horizontalLayout->setObjectName(QStringLiteral("horizontalLayout"));
        horizontalLayout->setContentsMargins(0, 0, 0, 0);
        Settings = new QWidget(centralWidget);
        Settings->setObjectName(QStringLiteral("Settings"));
        QSizePolicy sizePolicy(QSizePolicy::Fixed, QSizePolicy::Preferred);
        sizePolicy.setHorizontalStretch(0);
        sizePolicy.setVerticalStretch(0);
        sizePolicy.setHeightForWidth(Settings->sizePolicy().hasHeightForWidth());
        Settings->setSizePolicy(sizePolicy);
        Settings->setMinimumSize(QSize(300, 0));
        Settings->setMaximumSize(QSize(300, 16777215));
        Settings->setAutoFillBackground(true);
        verticalLayout = new QVBoxLayout(Settings);
        verticalLayout->setSpacing(6);
        verticalLayout->setContentsMargins(11, 11, 11, 11);
        verticalLayout->setObjectName(QStringLiteral("verticalLayout"));
        groupHapticLin = new QGroupBox(Settings);
        groupHapticLin->setObjectName(QStringLiteral("groupHapticLin"));
        groupHapticLin->setEnabled(true);
        gridLayoutHaptic = new QGridLayout(groupHapticLin);
        gridLayoutHaptic->setSpacing(6);
        gridLayoutHaptic->setContentsMargins(11, 11, 11, 11);
        gridLayoutHaptic->setObjectName(QStringLiteral("gridLayoutHaptic"));
        titleZoom = new QLabel(groupHapticLin);
        titleZoom->setObjectName(QStringLiteral("titleZoom"));
        titleZoom->setEnabled(true);

        gridLayoutHaptic->addWidget(titleZoom, 0, 0, 1, 1);

        sliderZoom = new QSlider(groupHapticLin);
        sliderZoom->setObjectName(QStringLiteral("sliderZoom"));
        sliderZoom->setEnabled(true);
        sliderZoom->setMaximum(100);
        sliderZoom->setOrientation(Qt::Horizontal);

        gridLayoutHaptic->addWidget(sliderZoom, 0, 2, 1, 1);

        labelZoom = new QLabel(groupHapticLin);
        labelZoom->setObjectName(QStringLiteral("labelZoom"));
        labelZoom->setEnabled(true);
        labelZoom->setAlignment(Qt::AlignRight|Qt::AlignTrailing|Qt::AlignVCenter);

        gridLayoutHaptic->addWidget(labelZoom, 0, 3, 1, 1);


        verticalLayout->addWidget(groupHapticLin);

        verticalSpacer = new QSpacerItem(20, 40, QSizePolicy::Minimum, QSizePolicy::Expanding);

        verticalLayout->addItem(verticalSpacer);


        horizontalLayout->addWidget(Settings);

        InterfaceClass->setCentralWidget(centralWidget);
        toolBar = new QToolBar(InterfaceClass);
        toolBar->setObjectName(QStringLiteral("toolBar"));
        toolBar->setAutoFillBackground(true);
        toolBar->setMovable(false);
        toolBar->setToolButtonStyle(Qt::ToolButtonTextUnderIcon);
        toolBar->setFloatable(false);
        InterfaceClass->addToolBar(Qt::TopToolBarArea, toolBar);
        statusBar = new QStatusBar(InterfaceClass);
        statusBar->setObjectName(QStringLiteral("statusBar"));
        InterfaceClass->setStatusBar(statusBar);

        toolBar->addAction(actionShow_Settings);
        toolBar->addAction(actionFull_Screen);

        retranslateUi(InterfaceClass);
        QObject::connect(actionShow_Settings, SIGNAL(triggered(bool)), Settings, SLOT(setVisible(bool)));

        QMetaObject::connectSlotsByName(InterfaceClass);
    } // setupUi

    void retranslateUi(QMainWindow *InterfaceClass)
    {
        InterfaceClass->setWindowTitle(QApplication::translate("InterfaceClass", "CHAI3D", Q_NULLPTR));
#ifndef QT_NO_TOOLTIP
        InterfaceClass->setToolTip(QString());
#endif // QT_NO_TOOLTIP
        actionShow_Settings->setText(QApplication::translate("InterfaceClass", "Settings", Q_NULLPTR));
        actionFull_Screen->setText(QApplication::translate("InterfaceClass", "Fullscreen", Q_NULLPTR));
        groupHapticLin->setTitle(QApplication::translate("InterfaceClass", "Application Parameters", Q_NULLPTR));
#ifndef QT_NO_TOOLTIP
        titleZoom->setToolTip(QApplication::translate("InterfaceClass", "Scaling factor between haptic translation and robot translation", Q_NULLPTR));
#endif // QT_NO_TOOLTIP
        titleZoom->setText(QApplication::translate("InterfaceClass", "Zoom", Q_NULLPTR));
        labelZoom->setText(QApplication::translate("InterfaceClass", "000.0", Q_NULLPTR));
        toolBar->setWindowTitle(QApplication::translate("InterfaceClass", "toolBar", Q_NULLPTR));
    } // retranslateUi

};

namespace Ui {
    class InterfaceClass: public Ui_InterfaceClass {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_INTERFACE_H
