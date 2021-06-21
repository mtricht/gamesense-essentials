# gamesense-essentials
Essential OLED integrations for SteelSeries Gamesense.

This programs adds the following to your SteelSeries OLED screen:

- A clock
- Volume slider when changing
- Displaying current artist and song for:
  - Spotify
  - iTunes
  - MusicBee
  - AIMP 
  - YouTube Music Desktop App

[Download for Windows](https://github.com/mtricht/gamesense-essentials/releases/download/1.7.0/gamesense-essentials-1.7.0.msi)  

## Demo
https://user-images.githubusercontent.com/7511094/122837368-3e0fad00-d2f4-11eb-868e-980b2b29e1c1.mp4

## Run on windows startup
To run after boot, create a shortcut to gamesense-essentials inside the "Startup" folder. Follow this [tutorial](https://www.howtogeek.com/208224/how-to-add-programs-files-and-folders-to-system-startup-in-windows-8.1/) if you're having trouble.

## Tick rate
Starting with v1.3.0, the tick rate/delay is now configurable through the system tray icon. This value determines how many times per second the OLED screen is updated.
The default on windows is 50 milliseconds and on macOS 200 milliseconds.

This is because the lower the number, the higher the CPU usage will be. For my 8-core gaming desktop, 50 milliseconds is barely noticeable, but for my macbook pro, 50 milliseconds is way too fast and the CPU fans will go crazy.   
