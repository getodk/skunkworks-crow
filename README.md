# Share

## Table of Contents
1. [Introduction](#introduction)
    1. [About Share](#about-share)
    2. [Why Share](#why-share)
2. [Installation and setup](#installation)
    1. [Install and setting up ODK Collect](#install-collect)
    2. [Install Share](#install-share)
3. [Using Share](#using-share)
    1. [Viewing category of forms](#view-category)
        1. [View statistics of forms received](#view-statistics)
        2. [View sent forms](#view-sent-forms)
        3. [View Received forms](#view-received-forms)
        4. [Review forms](#review-forms)
    2. [Send filled forms](#send-filled-forms)
    3. [Receive filled forms](#receive-filled-forms)

## 1. Introduction<a name="introduction"></a>
### 1.1 About Share<a name="about-share"></a>

Share is an Android application which is a companion app to ODK Collect and it helps different enumerators to share the filled and blanks forms with each other, it also helps organization’s supervisor in reviewing the forms before actually submitting to the server.

### 1.2 Why Share<a name="why-share"></a>


Share is designed to enable offline transfer of blank and filled forms. There are three major use cases for this multi-part surveys, supervisor review and blank form distribution.
ODK Collect is designed in such a way that it allows enumerators to download the blank forms and provide them with the interface to fill the blank forms, but sometimes there can be more than one enumerators who want to fill the same form. Collect doesn’t allow more than one enumerators to collaborate on a single form. This is where Share comes into the picture using Share one can select any number of forms and can send to the other enumerator will all the resources attached to it.

Also, sometimes organizations want to review filled forms before sending to the server. So enumerators can send the forms to the supervisor then the supervisor can mark forms as reviewed (accepted/rejected) with some feedback and then can send back to the same enumerator if required.

In some areas, enumerators don’t have any internet connectivity, so they can’t fetch blank forms from the server. Share also allows to send a blank form in case of no internet connectivity provided the devices should be close enough to create a local connection.

## 2. Installation and setup<a name="installation"></a>

The Share app requires ODK Collect installed on the device first because it uses its database and storage for all its forms transfer.

### 2.1 Install and setting up ODK Collect<a name="install-collect"></a>

To install ODK Collect visit this link.
To know more about ODK Collect visit the this documentation

### 2.2 Install Share<a name="install-share"></a>

Share app can be downloaded and installed from the link provided below.
APK LINK [Add it here]

## 3. Using Share<a name="using-share"></a>
Share app can be used by enumerators in sharing forms and by supervisor in reviewing the forms.

### 3.1 Viewing category of forms<a name="view-category"></a>

Launch the app to see  a listing of all distinct versions of blank forms on the home screen. Tapping on any form will open a screen with four different tabs showing the statistics, sent, received and reviewed.

To view details of any form category tap on any category shown on home screen.
<p align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/91ad9210c67615263aaccced89616562f77249fa/screenshots/home_screen.png" width="280" height="500"/>
</p>

#### 3.1.1 View statistics of forms received<a name="view-statistics"></a>

To view the statistics of any specific blank form on the device, tap on any item on the home screen. The statistics screen will show the number of filled forms sent, filled forms reviewed and unreviewed. This will give the supervisor a glimpse of all the details of form transfer happened through the device.
<p align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/statistics_tab.png" width="280" height="500"/>
</p>

#### 3.1.2 View sent forms<a name="view-sent-forms"></a>

To view the details of the form sent from the device navigate to ‘Sent’ tabs, the screen shows the details of sent filled forms if any forms were sent for a review and the supervisor sent back the forms with feedback then the user will be able to see the form reviewed status with feedback.
<p align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/send_tab.png" width="280" height="500"/>
</p>

#### 3.1.3 View Received forms<a name="view-received-forms"></a>

To view the details of forms received navigate to the ‘Received’ tab. To review the forms, tap on any received form to open the filled form in ODK Collect. After viewing the form, the user will be navigated to a screen asking for feedback and can mark the form as accepted or rejected. If the user is not ready to provide feedback,  even if wants to review later can tap on “Review later”. Using this option user will be navigated back to the receive screen so that user can review other forms. If supervisor already opened the form previously then the user will be navigated to a feedback screen directly if the supervisor wants to view the form again then tap on “View from in ODK Collect” to view it again.

<p float="left" align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/review.gif" width="280" height="500"/>
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/receive_review.gif" width="280" height="500"/>
</p>


#### 3.1.4 Review forms<a name="review-forms"></a>

To view the details of forms reviewed, tap on the ‘Reviewed’ tab. This shows all the forms reviewed by you with the form status. To send it back to the enumerator who asked for the feedback, select the forms and tap on ‘Send’ this will send all the forms with reviewed status as well as feedback back to the enumerator. If the enumerator didn’t send those forms for review then it won’t send any forms back only those enumerators will get the feedback who asked for it.





### 3.2 Send filled forms<a name="send-filled-forms"></a>

After the launch of the app, on the home screen a send button is shown at the bottom. Tap on that button to open the screen which consists of a list showing all the filled forms available in Collect and user can select any number of forms of any version of blank form which is required to be sent. It contains all the filled forms irrespective of its send/receive status.

It will display all the filled forms available in ODK Collect (it also contains the received forms so that user can pass that to another enumerator for further form filling)

After selecting the forms, press ‘send’ to initiate the connection process so that receiver can connect to your device and start receiving.

Tapping on ‘Send’ button opens up the screen which will initiate the communication process and start the wifi hotspot so that receiver can connect to it and start receiving the forms.

A Wifi Hotspot is used as the sender’s communication method. which creates a wifi network and sends files using the socket connection. When the wifi hotspot is initiated, it turns off the wifi if already enabled and creates new wifi configuration and saves the last saved hotspot configuration which is restored at the end of a transfer.

If hotspot initiation is successful then the screen will show the QR Code generated so that receiver can scan the QR Code to connect to it and start receiving the forms.

Is user wants to manually connect to it then the screen shows the network name so that user can connect to it by tapping on the network, and after wifi is connected it will ask for a port number which is required to create the connection.

<p align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/send_normal_mode.gif" width="280" height="500"/>
</p>

For devices having Android version 7.0 and 7.1 user will be sent to settings to enable the wifi hotspot functionality. The user will then need to tap the device back button to come back to share.
And for other device it will be enabled automatically.


### 3.3 Receive filled forms<a name="receive-filled-forms"></a>

Tap on the “Receive” button shown at the bottom of the home screen to see a list of all wifi networks available in the nearby area which are started by only Share app.

A receiver will need to connect to the network the sender has created. There are two ways to connect to the network:
A QR code generated by the sender can be scanned by a receiver, it will get all the information required to connect to the sender (wifi name, port,  and password if the connection is protected).
Tap on the wifi network available to connect and after successful connection enter port number to start receiving the forms.

Connecting to the sender’s network will disable all the wifi network available so that no other network can interrupt the current connection. After receiving the forms it will restore the last saved state.

For some devices, the user is not allowed to configure the wifi hotspot from the app. Currently for Android versions < 8, “ODK-SHARE-odk-share” is set as the default network name and for versions >= 8 it is AndroidShare-XXXX which is set by Android.

<p float="left" align="center">
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/received_for_review.gif" width="280" height="500"/>
  <img src="https://github.com/lakshyagupta21/share/blob/SHARE-78/screenshots/received_qr_code.gif" width="280" height="500"/>
</p>

