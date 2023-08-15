/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fx.hcm.pojo;

/**
 *
 * @author pscha
 */
public class SrtmRaster {

    private final String fileName;
    private int lonStart;
    private int latStart;

    private final SrtmCoord coord0;
    private final SrtmCoord coord1;
    private final SrtmCoord coord2;
    private final SrtmCoord coord3;

    /*
    0-1
    3-2
     */
    public SrtmRaster(String fileName) {
        this.fileName = fileName;

        String lon = fileName.substring(5, 7);
        String lat = fileName.substring(1, 4);

        String n = fileName.toUpperCase().substring(4, 5);
        String e = fileName.toUpperCase().substring(0, 1);

        //System.out.println(lon + " " + lat + " " + n + " " + e);

        lonStart = Integer.valueOf(lon);
        latStart = Integer.valueOf(lat);

        if (n.equals("S")) {
            lonStart = -1 * lonStart;
        }
        if (e.equals("W")) {
            latStart = -1 * latStart;
        }

        coord0 = new SrtmCoord(latStart, lonStart + 1);
        coord1 = new SrtmCoord(latStart + 1, lonStart + 1);
        coord2 = new SrtmCoord(latStart + 1, lonStart);
        coord3 = new SrtmCoord(latStart, lonStart);
    }

    public SrtmCoord getCoord0() {
        return coord0;
    }

    public SrtmCoord getCoord1() {
        return coord1;
    }

    public SrtmCoord getCoord2() {
        return coord2;
    }

    public SrtmCoord getCoord3() {
        return coord3;
    }

    public String getFileName() {
        return fileName;
    }
}
