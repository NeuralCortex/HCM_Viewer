# HCM-Viewer 1.0.0

![image](https://github.com/NeuralCortex/HCM_Viewer/blob/main/images/hcm.png)

## Funktionsweise des Programms

Der HCM-Viewer ist ein JavaFX-Projekt und ein Fork des [SRTM-Viewer](https://github.com/NeuralCortex/SRTM_Viewer).</br>
Es besitzt die gleiche Funktionalität des SRTM-Viewers, unterscheidet sich aber in den Format der einlesbaren Dateien.</br>
Im HCM-Viewer können SRTM-Dateien im HCM-Format eingelesen und angezeigt werden.</br>
Dabei werden 2 Arten von Dateien unterstützt - Topografische und Morphologische Dateien.</br>
Testdaten wurden unter folgenden [Link](http://www.hcm-agreement.eu/http/englisch/verwaltung/index_europakarte.htm) veröffentlicht.</br>
Die Topografischen und Morphologischen Daten sind zentrale Eingaben für den [HCM-Algorithmus](https://github.com/5chufti/HCM_V7).</br>
Zusammen mit den Grenzlinien einer bestimmten Region sind damit Funkverträglichkeitsuntersuchungen einer Funkzelle möglich.

## How the program works

The HCM Viewer is a JavaFX project and a fork of the [SRTM Viewer](https://github.com/NeuralCortex/SRTM_Viewer).</br>
It has the same functionality as the SRTM viewer, but differs in the format of the files that can be read.</br>
SRTM files in HCM format can be read and displayed in the HCM viewer.</br>
2 types of files are supported - topographical and morphological files.</br>
Test data was published under the following [Link](http://www.hcm-agreement.eu/http/englisch/verwaltung/index_europakarte.htm).</br>
The topographic and morphological data are central inputs for the [HCM algorithm](https://github.com/5chufti/HCM_V7).</br>
Together with the border lines of a specific region, radio compatibility tests of a radio cell are possible.

## Aufbau einer Datei

Anders als eine SRTM-Datei der NASA hat der Dateiname typischerweise die Form: "E010N50.63E" für topografische Dateien bzw.: </br>
"E010N50.63M" für morphologische Dateien. Weiterhin unterscheiden sich die Dateien in der Auflösung in Pixel was nachfolgend näher betrachtet wird.

### Interner Aufbau

Eine HCM-Datei Datei enthält 16-Bit signed Integer Werte, ohne Header oder Trailer.

HCM-Datei mit Endung ".63E" oder ".63M":
<pre>
                1x1 Grad    63-Kachel
Nord X=0,Y=1212 ********************* X=612,Y=1212</br>
                *********************</br>
                *********************</br>
                *********************</br>
    Süd X=0,Y=0 ********************* X=612,Y=0</br>
                West              Ost
</pre>

HCM-Datei mit Endung ".33E" oder ".33M":
<pre>
                1x1 Grad    33-Kachel
Nord X=0,Y=1212 ********************* X=1212,Y=1212</br>
                *********************</br>
                *********************</br>
                *********************</br>
    Süd X=0,Y=0 ********************* X=1212,Y=0</br>
                West              Ost
</pre>

Dabei ist zu beachten, das der interne Aufbau einer HCM-Datei komplexer ist als eine SRTM-Datei der NASA.</br>
Deswegen beschreibt das oben beschriebene Format nur das Result des Einleseprozesses in Pixeln.</br>
Der genaue Aufbau der Datei kann [hier](https://github.com/5chufti/HCM_V7/blob/master/Point_info.f90) nachgelesen werden.

Im Vergleich der Auflösung in Pixeln mit den NASA-Daten ergibt sich folgende Matrix:
<pre>
         | SRTM-1 | SRTM-3
---------|--------|-------
.63(E|M) |    <   |   <
---------|--------|-------
.33(E|M) |    <   |   >
</pre>

## Structure of a file

Unlike a NASA SRTM file, the filename typically takes the form: "E010N50.63E" for topographic files, or: </br>
"E010N50.63M" for morphological files. Furthermore, the files differ in the resolution in pixels, which is examined in more detail below.

### Internal structure

An HCM file contains 16-bit signed integer values, with no header or trailer.

HCM file with extension ".63E" or ".63M":
<pre>
                  1x1 degree    63 tile
 North X=0,Y=1212 ********************* X=612,Y=1212</br>
                  *********************</br>
                  *********************</br>
                  *********************</br>
    South X=0,Y=0 ********************* X=612,Y=0</br>
                  West             East
</pre>

HCM file with extension ".33E" or ".33M":
<pre>
                  1x1 degree    33 tile
 North X=0,Y=1212 ********************* X=1212,Y=1212</br>
                  *********************</br>
                  *********************</br>
                  *********************</br>
    South X=0,Y=0 ********************* X=1212,Y=0</br>
                  West             East
</pre>

It should be noted that the internal structure of an HCM file is more complex than a NASA SRTM file.</br>
Therefore, the format described above only describes the result of the reading process in pixels.</br>
The exact structure of the file can be read [here](https://github.com/5chufti/HCM_V7/blob/master/Point_info.f90).

A comparison of the resolution in pixels with the NASA data results in the following matrix:
<pre>
         | SRTM-1 | SRTM-3
---------|--------|-------
.63(E|M) |    <   |   <
---------|--------|-------
.33(E|M) |    <   |   >
</pre>

## Hinweis

Für den Betrieb des Programms ist eine ständige Internetverbindung erforderlich.

## A notice

A constant internet connection is required to run the program.

## Verwendete Technologie

Dieses JavaFX-Projekt wurde erstellt mit der Apache NetBeans 17 IDE [NetBeans 17](https://netbeans.apache.org/).

Folgende Frameworks sollten installiert sein:

- JAVA-SDK [JAVA 19](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- SceneBuilder für GUI-Entwicklung [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
- JAVA-FX-SDK [JavaFX](https://gluonhq.com/products/javafx/)

## Technology used

This JavaFX project was built with the Apache NetBeans 17 IDE [NetBeans 17](https://netbeans.apache.org/).

The following frameworks should be installed:

- JAVA SDK [JAVA 19](https://www.oracle.com/java/technologies/javase/jdk19-archive-downloads.html)
- SceneBuilder for GUI development [Gluon SceneBuilder](https://gluonhq.com/products/scene-builder/)
- JAVA FX SDK [JavaFX](https://gluonhq.com/products/javafx/)
