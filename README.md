# kegtron-cloud

This is a add-on app for Kegtron beer flow meter: https://kegtron.com/gen1/

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

# Caveats
Before installing this app, you should be aware of some caveats: 
1. Any app that requires periodic sync needs to run in the backgroud. I use a Android's alarm manager to schedue periodic sync. This comes with some caveats:
Android is not very favourable to apps running background and provides no guarentees. This means, sync may fail occonsionally due to BLE scan errors or fail to
upload data due to network errors. 
2. Android may decide not to schedule the sync at all. This is more likely to happen if the device enters doze mode.
3. This app is not a fully polished app. So you may observe battery drain or other stability issues. I run this on a dedicate phone instead of my everyday phone. 
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
