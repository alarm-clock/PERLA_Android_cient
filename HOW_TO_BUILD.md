# How to build this project

## Application

### Requirements

- Java 17
- Internet connection
- Intellij Idea Ultimate or Android Studio ( rest of this document will be about intellij)
- Having project present on system

### Build

1. Open Intellij and download android plugin. You can find it and install it in `File > Settings > Plugins`
2. After that, in `File > Settings > Languages & Frameworks > Android SDK` you download SDK with name `Android API 34`
3. Copy Android SDK location and create file `local.properties`, in folder where is gradlew script, and you will add this line `sdk.dir=PATH_TO_ANDROID_SDK` fof example `sdk.dir=/home/jozef/Android/Sdk` into `local.properties` file
4. Now what you just need to build project in `Build > Build Project`
5. If everything was done correctly, in `app/build/outputs/apk/debug` you should find `app-debug.apk`


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

