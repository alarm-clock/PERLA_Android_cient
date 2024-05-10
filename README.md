# Battle Management System PERLA

PERLA is open-source battle management system extension for Locus Map application developed by check company Asamm Softvare.
This project was developed as my Bachelor's thesis at Brno University of Technology Faculty of Information Technology.
Main aims of this project is to create free battle management system that is freely available and any group of people can use it.
Whole system is divided into two parts. PERLA client, which is application for Android systems with API 29+, and 
PERLA server. In this repository you will find implementation of PERLA client, manual how to build it, and user manual 
how to install it on your device and how to use it. PERLA server is located in this repository (add link to it). 

## Features implemented by PERLA

At this point PERLA system implements these features:
- Blue force tracking,
- Dividing users into teams, team management, and team location share,
- Points of interest sharing and management.
- Chat.

What features I plant to implement in the future:
- Enhance existing features,
- Adding ability to send files and points through chat,
- Adding support other file types then photos (video is nearly finished),
- Formatted messages,
- File sharing,
- Live map drawing,
- Full admin client,
- Web client,
- In very far future implement own maps.

If you want to help I will be glad. Right now my bachelor's was submitted so anyone can help with this project.

## How to build PERLA Android client

This project was whole done on Ubuntu but because this is Kotlin (Java++) it can be built also on Windows. Especially
if you use Intellij or Android Studio you can build it anywhere. But because I use Linux this tutorial
will only cover how to build it on Linux.

### Requirements Intellij

- Java 17
- Internet connection
- Intellij Idea Ultimate or Android Studio ( rest of this document will be about intellij)
- Having project present on system

### Build Intellij (Preferred/Easier option)

Note that this option will always work.

1. Open Intellij and download android plugin. You can find it and install it in `File > Settings > Plugins`
2. After that, in `File > Settings > Languages & Frameworks > Android SDK` you download SDK with name `Android API 34`
3. Copy Android SDK location and create file `local.properties`, in folder where is gradlew script, and you will add this line `sdk.dir=PATH_TO_ANDROID_SDK` for example `sdk.dir=/home/jozef/Android/Sdk` into `local.properties` file. You may skip this step if intellij generated this file for you.
4. Now what you just need to build project in `Build > Build Project`
5. If everything was done correctly, in `app/build/outputs/apk/debug` you should find `app-debug.apk`


If you get an error that gradle AGP version is incompatible with intelli it means that you have old version of intellij and must update it or download the newest version.
In the error you will find which version is compatible with used gradle version.

### Requirements Linux terminal

- Java 17
- Internet connection
- Having project present on system

### Build Linux terminal

1. Open terminal
2. Follow steps on this official Android developers to download `sdkmanager` https://developer.android.com/tools/sdkmanager. This webpage also shows how to use this command.
3. After that install `"platforms;android-34" "build-tools;34.0.0" "sources;android-34"`
4. If sdkmanger will cry that there is no root directory, or it isn't expected structure `export ANDROID_SDK_ROOT=/path/to/sdk`. If that doesn't help, then manually set that part when running that command with option `--sdk_root=$ANDROID_SDK_ROOT`.
5. Check if everything is installed by running `sdkmanager --list`
6. Store its location in ANDROID_HOME variable `export ANDROID_HOME=/path/to/sdk` (for example for me, it is export ANDROID_HOME=/home/jozef/Android/Sdk, or ANDROID_HOME=/usr/lib/android-sdk). Basically it will be folder where you unzipped cmdline-tools. if you plan to do it multiple times store it in `.bashrc` or anywhere where it will be permanent
7. Move to folder with source code where is `gradlew` script
8. Execute this command `./gradlew && ./gradlew build`, it will take time to build, so you have time to do something else (it can be more than 16 minutes)
9. If everything was done correctly, in `app/build/outputs/apk/debug` you should find `app-debug.apk`

### Installation
The app can be installed onto the device in many ways. The simplest way to do it is by uploading the add-debug.apk file to Google Drive, where you can download it to any device. Another possible way is to upload it to the target device by using `adb`. When the file
will be present on device storage; just open it in the Storage application, and then you will be prompted to install the PERLA client.
By default, you can not install apps from the storage application, so you will be prompted to change it in settings. You must do that.
After that, the application should be installed and ready to run.



