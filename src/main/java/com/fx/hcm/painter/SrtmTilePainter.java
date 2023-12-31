/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fx.hcm.painter;

import com.fx.hcm.pojo.ColorRow;
import com.fx.hcm.pojo.Range;
import com.fx.hcm.pojo.RectangleInfo;
import com.fx.hcm.pojo.SrtmRaster;
import com.fx.hcm.thread.SrtmMapBoundsThread;
import com.fx.hcm.tools.HelperFunctions;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

/**
 *
 * @author pscha
 */
public class SrtmTilePainter implements Painter<JXMapViewer> {

    private Color color = Color.BLACK;
    private final boolean antiAlias = true;
    private final SrtmRaster srtmRaster;
    private final short[][] bigMap;
    private int min;
    private int max;
    private boolean showHeight;
    private boolean showAlpha;
    private boolean raster = false;
    private List<ColorRow> colors;
    private GeoPosition geoPositionSel;
    private GeneralPath generalPath;
    private JXMapViewer mapViewer;

    public SrtmTilePainter(SrtmRaster srtmRaster, short[][] bigMap, int min, int max) {
        this.srtmRaster = srtmRaster;
        this.bigMap = bigMap;
        this.min = min < 0 ? 0 : min;
        this.max = max;
    }

    @Override
    public void paint(Graphics2D g, JXMapViewer map, int i, int i1) {
        mapViewer = map;
        g = (Graphics2D) g.create();

        Rectangle rect = map.getViewportBounds();
        g.translate(-rect.x, -rect.y);

        if (antiAlias) {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        }

        g.setColor(color);
        g.setStroke(new BasicStroke(2));

        drawSrtmTile(g, map);

        g.dispose();
    }

    private void drawSrtmTile(Graphics2D g, JXMapViewer map) {
        int sizeVer = bigMap.length;
        int sizeHor = bigMap[0].length;

        //System.out.println("horSize: "+sizeHor+" verSize: "+sizeVer);
        RectangleInfo matrix[][] = new RectangleInfo[sizeVer][sizeHor];

        SrtmMapBoundsThread srtmMapBoundsThread = new SrtmMapBoundsThread(matrix, map, bigMap, srtmRaster, min, max, colors);
        srtmMapBoundsThread.setRangeListener((Range range) -> {

            GeoPosition geoPosition0 = new GeoPosition(srtmRaster.getCoord0().getLat(), srtmRaster.getCoord0().getLon());
            GeoPosition geoPosition1 = new GeoPosition(srtmRaster.getCoord1().getLat(), srtmRaster.getCoord1().getLon());
            GeoPosition geoPosition2 = new GeoPosition(srtmRaster.getCoord2().getLat(), srtmRaster.getCoord2().getLon());
            GeoPosition geoPosition3 = new GeoPosition(srtmRaster.getCoord3().getLat(), srtmRaster.getCoord3().getLon());

            Point2D pt0 = map.getTileFactory().geoToPixel(geoPosition0, map.getZoom());
            Point2D pt1 = map.getTileFactory().geoToPixel(geoPosition1, map.getZoom());
            Point2D pt2 = map.getTileFactory().geoToPixel(geoPosition2, map.getZoom());
            Point2D pt3 = map.getTileFactory().geoToPixel(geoPosition3, map.getZoom());

            generalPath = new GeneralPath();
            generalPath.moveTo(pt0.getX(), pt0.getY());
            generalPath.lineTo(pt1.getX(), pt1.getY());
            generalPath.lineTo(pt2.getX(), pt2.getY());
            generalPath.lineTo(pt3.getX(), pt3.getY());
            generalPath.closePath();

            Point2D marker = null;
            if (geoPositionSel != null) {
                marker = map.getTileFactory().geoToPixel(geoPositionSel, map.getZoom());
            }

            for (int y = range.getyStart(); y < range.getyEnde(); y += range.getVerStep()) {

                for (int x = range.getxStart(); x < range.getxEnde(); x += range.getHorStep()) {

                    if (matrix[y][x] != null) {
                        Color c = matrix[y][x].getColor();
                        if (showAlpha) {
                            g.setColor(new java.awt.Color(c.getRed(), c.getGreen(), c.getBlue(), 30));
                        } else {
                            g.setColor(c);
                        }

                        g.fill(matrix[y][x].getRectangle());
                        g.draw(matrix[y][x].getRectangle());
                    }
                }
            }

            for (int y = range.getyStart(); y < range.getyEnde(); y += range.getVerStep()) {

                for (int x = range.getxStart(); x < range.getxEnde(); x += range.getVerStep()) {
                    if (matrix[y][x] != null) {
                        if (marker != null) {
                            if (matrix[y][x].getRectangle().contains(marker)) {
                                g.setColor(Color.BLACK);

                                g.draw(matrix[y][x].getRectangle());
                                Rectangle rectangle = matrix[y][x].getRectangle();
                                g.fillOval((int) rectangle.getCenterX(), (int) rectangle.getCenterY(), 3, 3);

                                g.setFont(new Font("Arial", Font.PLAIN, 10));
                                g.drawString(matrix[y][x].getHeight() + " m", (float) rectangle.getCenterX() + 5, (float) rectangle.getCenterY() - 5);

                            }
                        }
                    }
                }
            }
            g.setColor(Color.BLACK);
            g.draw(generalPath);

            if (map.getZoom() <= 11) {
                g.setColor(Color.BLUE);

                int length = 20;

                g.setStroke(new BasicStroke(2));

                g.drawLine((int) pt0.getX(), (int) pt0.getY(), (int) pt0.getX(), (int) pt0.getY() - length);
                g.drawLine((int) pt1.getX(), (int) pt1.getY(), (int) pt1.getX(), (int) pt1.getY() - length);

                g.drawLine((int) pt2.getX(), (int) pt2.getY(), (int) pt2.getX(), (int) pt2.getY() + length);
                g.drawLine((int) pt3.getX(), (int) pt3.getY(), (int) pt3.getX(), (int) pt3.getY() + length);

                g.drawLine((int) pt1.getX(), (int) pt1.getY(), (int) pt1.getX() + length, (int) pt1.getY());
                g.drawLine((int) pt2.getX(), (int) pt2.getY(), (int) pt2.getX() + length, (int) pt2.getY());

                g.setStroke(new BasicStroke(2));

                double widthUp = HelperFunctions.getDistance(geoPosition0.getLongitude(), geoPosition0.getLatitude(), geoPosition1.getLongitude(), geoPosition1.getLatitude());
                double widthDown = HelperFunctions.getDistance(geoPosition3.getLongitude(), geoPosition3.getLatitude(), geoPosition2.getLongitude(), geoPosition2.getLatitude());
                double height = HelperFunctions.getDistance(geoPosition0.getLongitude(), geoPosition0.getLatitude(), geoPosition3.getLongitude(), geoPosition3.getLatitude());

                Font font = new Font("Arial", Font.BOLD, 15);
                g.setFont(font);

                String strWidthUp = formatLength(widthUp) + " km";
                String strWidthDown = formatLength(widthDown) + " km";
                String strHeight = formatLength(height) + " km";

                Rectangle2D rectangle2dUp = g.getFontMetrics(font).getStringBounds(strWidthUp, g);
                Rectangle2D rectangle2dDown = g.getFontMetrics(font).getStringBounds(strWidthDown, g);
                Rectangle2D rectangle2dHeight = g.getFontMetrics(font).getStringBounds(strHeight, g);

                float xUp = (float) ((pt0.getX() + pt1.getX()) / 2.0 - rectangle2dUp.getCenterX());
                float xDown = (float) ((pt0.getX() + pt1.getX()) / 2.0 - rectangle2dDown.getCenterX());
                float yHeight = (float) ((pt1.getY() + pt2.getY()) / 2.0 - rectangle2dHeight.getCenterY());

                g.drawString(strWidthUp, xUp, (float) (pt0.getY() - 5));
                g.drawString(strWidthDown, xDown, (float) (pt3.getY() + rectangle2dDown.getHeight()));
                g.drawString(strHeight, (float) pt1.getX() + 5, (float) (yHeight));
            }

        });
        srtmMapBoundsThread.start();
        try {
            srtmMapBoundsThread.join();
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    private String formatLength(double d) {
        return String.format("%.4f", d);
    }

    public void setColors(List<ColorRow> colors) {
        this.colors = colors;
    }

    public void setMinMax(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void setShowHeight(boolean showHeight) {
        this.showHeight = showHeight;
    }

    public void setShowAlpha(boolean showAlpha) {
        this.showAlpha = showAlpha;
    }

    public void setGeoPositionSel(GeoPosition geoPositionSel) {
        this.geoPositionSel = geoPositionSel;
    }

    public boolean isOut(GeoPosition geoPosition) {
        Point2D marker = mapViewer.getTileFactory().geoToPixel(geoPosition, mapViewer.getZoom());
        return !generalPath.contains(marker);
    }
}
