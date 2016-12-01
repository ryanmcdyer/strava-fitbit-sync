# Strava Fitbit Sync

A Java utility for combining a Strava .gpx file (containing accurate GPS data but no HR data) and a Fitbit .gpx file (containing HR data but poor GPS data). The Fitbit application for tracking activities like runs or cycles has a longer interval between getting the current location than the Strava application. This can often result in the Strava application reporting the correct distance travelled, but the Fitbit application will report a distance usually 5-10% less. But some of Fitbit's tracking watches don't support Strava for Heart Rate data. This utility is designed to fix that problem.

## Usage

First record your activity on both the Fitbit application (with your bluetooth on) and the Strava application.

    javac Combiner.java
    java Combiner

Then follow the instructions on screen. Both files should be .gpx files

# TODO

## Add support for .tcx  
The Fitbit website only allows you to download .tcx files, which then need to be converted to .gpx files (usually by uploading them to strava and then downloading them).

## Add auto-sync
So the utility will detect when new activities are added and try to combine the 2 files automatically.
