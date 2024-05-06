# How to build this project

This project was whole done on Ubuntu but because this is Kotlin (Java++) it can be built also on Windows. Especially
if you use Intellij or Android Studio you can build it anywhere. But because I use better OS this tutorial
will only cover how to build it on Linux. 

## Application

### Requirements Intellij

- Java 17
- Internet connection
- Intellij Idea Ultimate or Android Studio ( rest of this document will be about intellij)
- Having project present on system

### Build Intellij

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
2. Download android-sdk (for example `sudo apt update && sudo apt install android-sdk` ), this will download Android API 34 SDK on system
3. Find its location. Most likely it will be `/home/[user name]/Android/Sdk`. If not then other possible location can be found here https://stackoverflow.com/questions/34556884/how-to-install-android-sdk-on-ubuntu.
4. Store its location in ANDROID_HOME variable `export ANDROID_HOME=/home/[user name]/Android/Sdk` (for example for me, it is export ANDROID_HOME=/home/jozef/Android/Sdk), if you plan to do it multiple times store it in `.bashrc` or anywhere where it will be permanent
5. Move to folder with source code where is `gradlew` script
6. Execute this command `./gradlew && ./gradlew build`, it will take time to build, so you have time to do something else (it can be more than 16 minutes)
7. If everything was done correctly, in `app/build/outputs/apk/debug` you should find `app-debug.apk`

### Installation
App can be installed onto device in many ways. Simplest way to do it is by uploading add-debug.apk file to google drive
from where you can download it to any device. Other possible way is to upload it to target device by using `adb`. When file
will present on device storage just open it in Storage application, and then you will be prompted to install PERLA client.
By default, you can not install apps from storage application, so you will be prompted to change it in settings. You must do that.
After that application should be installed and ready to run.

## Server



### Requirements

- Java 17
- Internet connection
- Having project present on system

### Build

1. Run `build.sh` script 

### Run

To run server just run `run.sh` script.


