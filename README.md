# kegtron-cloud

This is an dd-on app for Kegtron beer flow meter: https://kegtron.com/gen1/. I am just a homebrewer who uses Kegtron, and I am in no way associated 
with the Kegtron company. So this is just an unofficial companion app to the official app.

Kegtron is a flow meter with Bluetooth which can be installed on to a beer keezer/kegerator to monitor the amount of beer in your kegs.
Kegtron has a bluetooth, but does not have WiFi connectivity. The official Kegtron App on android does not have the ability to sync data to cloud.

The ultimate aim of this project is to seamlessly export keg levels in my Keezer and display it on digital-taplist. There are three parts to achieve this:

1. First, we need a way to export data from Kegtron device to the cloud. Since Kegtron does not have WiFi,
we need a local device to read Kegtron BLE data and send it to cloud. The Android App of this repo plays this role.

2. Second, we need a backend server/infrastructure to sync the data. For simplicity,
I have chosen Google Sheets as my backend database and Google App Script to host the WebApp. 

3. Third, we need a digital taplist which can read data from the WebApp and display the Keg status. I have chosen a simple Google Sites webpage for this.

In summary, the Android App in this repo will periodically scan BLE data from a local Kegtron device, uploads the data to Google Sheets through a WebApp. A static 
webpage in Google Sites will pull data through the WebApp, and display the Keg Levels.

You can checkout https://sites.google.com/view/sharath-taplist/home to see how I have created a simple taplist with these techniques.

In the rest of this document, I will describe on how to install and use this Android App. I've uploaded instructions and code for parts 2 and 3 here: 
https://github.com/sharathcshekhar/homebrew-taplist/

# Basic Usage
I originally wrote this app for a 2-port Kegtron device (which is the only device I have tested this app with). Later, I added some extensions to the code to 
support even a 1-port device. Since this is not tested, please play around and make any changes as required. If you are using more than one Kegtron device,
I don't know how it behaves. Feel free to add extensions to support this use case.

I have tested this app on Pixel 4 XL running Android Q. If you are running a different version/brand you have to figure out on your own to get the app working.

## Installing 
If you are familiar with Android build process, you can clone the repo and build your own apk. If you are not familiar, you can isntall Android Studio which comes
with all the build tools. 
If you are impatient, you can grab the latest apk here: https://github.com/sharathcshekhar/kegtron-cloud/raw/master/releases/app-release-unsigned.apk
To install this, you may still want to to install adb, enable developer option on your Android phone, and connect phone over USB with debug mode enabled. There
are a few other options for installation - I have not tried this, but you may find it useful - https://www.lifewire.com/install-apk-on-android-4177185.

## Starting the App
1. Start the app with Kegtron within the bluetooth range. You have to enter the following parameters into the app:
    1. WebApp URL
    2. WebApp write access key
    3. Sync Freqency in minutes.
2. After this, you can press "Start Syncing" button. 
3. Grant the permissions asked. BLE scans in background requires "Fine location access" with "always on" not just "when app is in use". 
4. The first sync happens immediately. There is a small status window in UI which displays a status of the previous sync.
5. To refresh the status, switch to a different app and switch back. If all is well, you should see "Upload Sucess" in your status window.
6. You can update any of these parameters at anytime by entering new parameters, and clicking "Save". The new values will be taken on the next scheduled scan.
7. If you want the updated parameters (e.g., sync frequency) to be reflected immediately, you will have to once again press "Start Syncing".
8. You can stop syncing at anytime with the "Stop syncing" button.

### Example usage
You can start the app with a low frequency of say once every 6 hours (360 minutes). Just before a party you're planning you can update the sync frequency to once 
every 15 mins. If you have a phone that's always plugged in, you can use a frequency as high you want.

## Working
1. The name of your device will be something like this: "Kegtron 12345" where 12345 is probably a unique ID. 
2. On the first scan, the app does not know the exact name your device nor its MAC. So it scans for all available devices and looks for a device whose self-
described name starts with "kegtron". Once it finds a match, it records the name and later periodic scans will be faster as the app now knows the exact name, and 
matches it during scans.

# Caveats
Before installing this app, you should be aware of some caveats: 
1. Any app that requires periodic sync needs to run in the backgroud. I use a Android's alarm manager to schedue periodic sync. This comes with some problems:
Android is not very favourable to apps running background and provides no guarentees of scheduling tasks. This means, sync may fail occonsionally due to BLE scan errors or fail to upload data due to network errors. 
2. Android may decide not to schedule the sync at all. This is more likely to happen if the device enters doze mode. If you notice this, open the app and close it. That brings the app to active state and Android is more likely to resume syncing.
3. This app is not a fully polished app. So you may observe battery drain or other stability issues. I run this on a dedicated phone instead of my everyday phone. 
If you have a spare old phone lying aroud, you can do the same.
4. I have not tested this app on anything other my phone and my Kegtron. So if it fails to work because of compatibility, feel free to change the code. If you
this you changes are useful for others, pleas send me a pull-request, I will be happy to aceept any enhancements.

# Inner workings

There are two ways to read data from Kegtron device. First is figure out the service UUIDs supported by the device by using a scan tool such as nRF Scan tool: https://play.google.com/store/apps/details?id=no.nordicsemi.android.mcp&hl=en_US.
This is what I first attempted and worked well. Later, I obtained the BLE spec from Kegtron by sending them an email. From the spec, I found out the device broadcasts all the required data with the BLE advertisement data. 
Just by parsing this byte array broadcasted, we can get all the informtion without the need of querying individual UUIDs. This is the technique I am using now.

Most of the code, I feel, is self explanatory. This is my very first BLE project and I don't write Android code for a living.
So I understand BLE can be confusing. I am planning to write a more detailed blog on that sometie soon. So if you're interested to know more on this, please stay 
tuned. 
