# kegtron-cloud

This is a add-on app for Kegtron beer flow meter: https://kegtron.com/gen1/

Kegtron is a flow meter with Bluetooth which can be installed on to a beer keezer/kegerator to monitor the amount of beer in your kegs. Kegtron has a bluetooth, but does not have WiFi connectivity. The official Kegtron App on android does not have the ability to sync data to cloud.

The ultimate aim of this project is to seamlessly export Keg levels in my Keezer and display them over a digital Taplist. There are three parts to achieve this:

1. First, we need a way to export data from Kegtron device to the cloud. Since Kegtron does not have WiFi, we need a local device to read Kegtron BLE data and send it to cloud. The Android App of this repo is meant for that.
2. Second, we need a backend server/infrastructure to sync the data. For simplicity, I have chosen Google Sheets as my backend database and Google App Script to host the WebApp. 
3. Third, we need a digital taplist which can read data from the WebApp and display the Keg status. I have chosen a simple Google Sites webpage for this. It is not super pretty, but I a reasonably happy.

In summary, the Android App in this repo will periodically scan BLE data from a local Kegtron device, uploads the data to Google Sheets through a WebApp. A static webpage in Google Sites will pull data through the WebApp, and display the Keg Levels.

I will update the description or a blog to give more information on Part-2 and Part-3 to complete the set up. In the rest of this document, I will describe on how to install and use this Android App.
