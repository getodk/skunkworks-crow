# skunkworks-crow
This project is not actively maintained. It is archived here for reference purposes.

If you wish to maintain it, please contact the ODK team at https://getodk.org.

## Overview
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/opendatakit/skunkworks-crow.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/opendatakit/skunkworks-crow)
[![Slack status](http://slack.opendatakit.org/badge.svg)](http://slack.opendatakit.org)

## Table of Contents
1. [Introduction](#introduction)
    1. [About skunkworks-crow](#about-skunkworks-crow)
    2. [Why skunkworks-crow](#why-skunkworks-crow)
2. [Installation and setup](#installation)
    1. [Install and setting up ODK Collect](#install-collect)
    2. [Install skunkworks-crow](#install-skunkworks-crow)
3. [Using skunkworks-crow](#using-skunkworks-crow)
    1. [Viewing category of forms](#view-category)
        1. [View statistics of forms received](#view-statistics)
        2. [View sent forms](#view-sent-forms)
        3. [View Received forms](#view-received-forms)
        4. [Review forms](#review-forms)
    2. [Send filled forms](#send-filled-forms)
        1. [hotspot](#send-hotspot)
        2. [bluetooth](#send-bluetooth)
    3. [Receive filled forms](#receive-filled-forms)
        1. [hotspot](#receive-hotspot)
        2. [bluetooth](#receive-bluetooth)
4. [Switch Transferring Method](#switch-transferring-method)
5. [Setting up your development environment](#setting-up-your-development-environment)
6. [Contributing code](#contributing-code)



## 1. Introduction<a name="introduction"></a>
### 1.1 About skunkworks-crow<a name="about-skunkworks-crow"></a>

skunkworks-crow is an Android application which is a companion app to ODK Collect and it helps different enumerators to share the filled and blanks forms with each other, it also helps organization’s supervisor in reviewing the forms before actually submitting to the server.

### 1.2 Why skunkworks-crow<a name="why-skunkworks-crow"></a>


skunkworks-crow is designed to enable offline transfer of blank and filled forms. There are three major use cases for this: multi-part surveys, supervisor review and blank form distribution.
ODK Collect is designed in such a way that it allows enumerators to download the blank forms and provide them with the interface to fill the blank forms, but sometimes there can be more than one enumerators who want to fill the same form. Collect doesn’t allow more than one enumerators to collaborate on a single form. This is where skunkworks-crow comes into the picture using skunkworks-crow one can select any number of forms and can send to the other enumerator will all the resources attached to it.

Also, sometimes organizations want to review filled forms before sending to the server. So enumerators can send the forms to the supervisor then the supervisor can mark forms as reviewed (accepted/rejected) with some feedback and then can send back to the same enumerator if required.

In some areas, enumerators don’t have any internet connectivity, so they can’t fetch blank forms from the server. skunkworks-crow also allows to send a blank form in case of no internet connectivity provided the devices should be close enough to create a local connection.

## 2. Installation and setup<a name="installation"></a>

The skunkworks-crow app requires ODK Collect installed on the device first because it uses its database and storage for all its forms transfer.

### 2.1 Install and setting up ODK Collect<a name="install-collect"></a>

To install ODK Collect visit [this](https://docs.opendatakit.org/collect-install) link.
To know more about ODK Collect visit the [documentation](https://docs.opendatakit.org/collect-intro) page

### 2.2 Install skunkworks-crow<a name="install-skunkworks-crow"></a>

skunkworks-crow app can be downloaded and installed from the link provided below.

[Download APK](https://github.com/opendatakit/skunkworks-crow/releases)

## 3. Using skunkworks-crow<a name="using-skunkworks-crow"></a>
Skunkworks-crow app can be used by enumerators in sharing forms and by supervisor in reviewing the forms.

### 3.1 Viewing category of forms<a name="view-category"></a>

On launching the app, the first screen that we see contains a list of forms available in ODK Collect. Tapping on any form opens a screen with 4 tabs - statistics, sent, received, and reviewed.

To view details of any form category tap on any category shown on home screen.
<p align="center">
  <img src="/screenshots/home_screen.png" width="280" height="500"/>
</p>

#### 3.1.1 View statistics of forms received<a name="view-statistics"></a>

To view the statistics of any specific blank form on the device, tap on any item on the home screen. The statistics screen will show the number of filled forms sent, filled forms reviewed and unreviewed. This will give the supervisor a glimpse of all the details of form transfer that happened through the device.
<p align="center">
  <img src="/screenshots/statistics_tab.png" width="280" height="500"/>
</p>

#### 3.1.2 View sent forms<a name="view-sent-forms"></a>

To view the details of the form sent from the device, navigate to ‘Sent’ tabs, the screen shows the details of sent filled forms. If any forms were sent for a review and the supervisor sent back the forms with feedback, then the user will be able to see the form reviewed status with feedback.
<p align="center">
  <img src="/screenshots/send_tab.png" width="280" height="500"/>
</p>

#### 3.1.3 View Received forms<a name="view-received-forms"></a>

To view the details of forms received, navigate to the ‘Received’ tab. To review the forms, tap on any received form to open the filled form in ODK Collect. After viewing the form, the user will be navigated to a screen asking for feedback and can mark the form as approved or rejected. If the user is not ready to provide feedback, he can review later tap by tapping “Review later”. Using this option user will be navigated back to the receive screen so that user can review other forms. If supervisor already opened the form previously then the user will be navigated to a feedback screen directly. If the supervisor wants to view the form again then he can tap on “View from in ODK Collect” to view it again.

<p float="left" align="center">
  <img src="/screenshots/review.gif" width="280" height="500"/>
  <img src="/screenshots/receive_review.gif" width="280" height="500"/>
</p>


#### 3.1.4 Review forms<a name="review-forms"></a>

To view the details of forms reviewed, tap on the ‘Reviewed’ tab. This shows all the forms reviewed by you with the form status. To send it back to the enumerator who asked for the feedback, select the forms and tap on ‘Send’. This will send all the forms with reviewed status as well as feedback back to the enumerator. If the enumerator didn’t send those forms for review then it won’t send any forms back, only those enumerators will get the feedback who asked for it.


### 3.2 Send filled forms<a name="send-filled-forms"></a>

After the launch of the app, on the home screen a 'Send' button is shown at the bottom. Tap on that button to open the screen which consists of a list showing all the filled forms available in ODK Collect and user can select any number of forms of any version of blank form which is required to be sent. It contains all the filled forms irrespective of its send/receive status.

It will display all the filled forms available in ODK Collect (it also contains the received forms so that user can pass that to another enumerator for further form filling)

After selecting the forms, press ‘Send’ to initiate the connection process so that receiver can connect to your device and start receiving.

Tapping on ‘Send’ button opens up the screen which will initiate the communication process and start the wifi hotspot so that receiver can connect to it and start receiving the forms.

In the Skunkworks-crow, forms can be exchanged using either Wifi Hotspot or Bluetooth. The default preference for the transferring method can be modified through the settings page.

<p align="center">
  <img src="/screenshots/choose_default_method.png" width="280" height="500"/>
</p>

#### 3.2.1 Wifi Hotspot<a name="send-hotspot"></a>

A Wifi Hotspot is used as the sender’s communication method, which creates a wifi network and sends files using the socket connection. When the wifi hotspot is initiated, it turns off the wifi if already enabled and creates new wifi configuration and saves the last saved hotspot configuration which is restored at the end of a transfer.

If hotspot initiation is successful then the screen will show the QR Code generated so that receiver can scan the QR Code to connect to it and start receiving the forms.

If user wants to manually connect to it then the screen shows the network name so that user can connect to it by tapping on the network, and after wifi is connected it will ask for a port number which is required to create the connection.

<p align="center">
  <img src="/screenshots/send_normal_mode.gif" width="280" height="500"/>
</p>

For devices having Android version 7.0 and 7.1 user will be sent to settings to enable the wifi hotspot functionality. The user will then need to tap the device back button to come back to skunkworks-crow.
And for other device it will be enabled automatically.

#### 3.2.2 Bluetooth<a name="send-bluetooth"></a>

The bluetooth has a shorter transferring distance than hotspot, so you may need to put sender and receiver closed to establish a valid connection. In the receiver's settings page, you can enable the secure mode of bluetooth, it allows you to establish an encrypted transmission (needs a pair code when first pairing).

The bluetooth feature needs location permission. After a bluetooth socket was create, you will see a dialog to ask to make your device discoverable, after clicking that, You have 120s to let the receiver connect. Our application will automatically start data transfer if the bluetooth connection is successful.

<p align="center">
  <img src="/screenshots/send_data.gif" width="280" height="500"/>
</p>


### 3.3 Receive filled forms<a name="receive-filled-forms"></a>

#### 3.3.1 Wifi Hotspot<a name="receive-hotspot"></a>
Tap on the 'Receive' button shown at the bottom of the home screen to see a list of all wifi networks available in the nearby area which are started by only skunkworks-crow app.

A receiver will need to connect to the network the sender has created. There are two ways to connect to the network:
A QR code generated by the sender that can be scanned by a receiver, it will get all the information required to connect to the sender (wifi name, port,  and password if the connection is protected).
Or else, tap on the wifi network available to connect and after successful connection enter port number to start receiving the forms.

Connecting to the sender’s network will disable all the wifi network available so that no other network can interrupt the current connection. After receiving the forms it will restore the last saved state.

For some devices, the user is not allowed to configure the wifi hotspot from the app. Currently for Android versions < 8, “ODK-SKUNKWORKS-odk-skunkworks” is set as the default network name and for versions >= 8 it is AndroidShare-XXXX which is set by Android.

<p float="left" align="center">
  <img src="/screenshots/received_for_review.gif" width="280" height="500"/>
  <img src="/screenshots/received_qr_code.gif" width="280" height="500"/>
</p>

#### 3.3.2 Bluetooth<a name="receive-bluetooth"></a>

If we use bluetooth to receive forms, you can find a bluetooth device list in the receiver page. Just clicking the sender's item, the Skunkworks-crow will connect and start receiving once connected. You can click the `refresh` button to refresh the list if you cannot find your target sender. 

<p float="left" align="center">
  <img src="/screenshots/send_data.gif" width="280" height="500"/>
  <img src="/screenshots/receive_data.gif" width="280" height="500"/>
</p>

## 4. Switch Transferring Method<a name="switch-transferring-method"></a>
We have two methods to use, you can set a default method in settings. But when you have trouble sending/receiving forms, you can click the `switch` icon button in the tool bar, that offers a quick way for you to switch from this method to another. 

<p align="center">
  <img src="/screenshots/method_switching.gif" width="280" height="500"/>
</p>

If you changed the method of sender/receiver, don't forget to be consistent with another device.

## 5. Setting up your development environment<a name="setting-up-your-development-environment"/>

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html)

1. Fork the Skunkworks-crow project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/skunkworks-crow

    If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/skunkworks-crow`.

1. Open the project in the folder of your clone from Android Studio. To run the project, click on the green arrow at the top of the screen.

## 6. Contributing code<a name="contributing-code"/>
Any and all contributions to the project are welcome. Skunkworks-Crow is used across the world primarily by organizations with a social purpose so you can have real impact!

Issues tagged as [good first issue](https://github.com/opendatakit/skunkworks-crow/labels/good%20first%20issue) should be a good place to start. There are also currently many issues tagged as [needs reproduction](https://github.com/opendatakit/skunkworks-crow/labels/needs%20reproduction) which need someone to try to reproduce them with the current version of Skunkwork-Crow and comment on the issue with their findings.
If you're ready to contribute code, see [the contribution guide](CONTRIBUTING.md).

