package com.fx.hcm.tools;

import com.fx.hcm.Globals;
import com.fx.hcm.pojo.ColorRow;
import java.awt.Color;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Screen;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.util.SloppyMath;
import org.jxmapviewer.viewer.GeoPosition;

public class HelperFunctions {

    private static final Logger _log = LogManager.getLogger(HelperFunctions.class);
    public static double SF = 180.0 / Math.PI;

    public static void centerWindow(Window window) {
        window.addEventHandler(WindowEvent.WINDOW_SHOWN, (WindowEvent event) -> {
            Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
            window.setX((screenBounds.getWidth() - window.getWidth()) / 2);
            window.setY((screenBounds.getHeight() - window.getHeight()) / 2);
        });
    }

    public Node loadFxml(ResourceBundle bundle, String path, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path), bundle);
            loader.setController(controller);
            Node node = loader.load();
            return node;
        } catch (IOException ex) {
            _log.error(ex.getMessage());
        }
        return null;
    }

    public static Tab addTab(ResourceBundle bundle, TabPane tabPane, String path, Object controller, String tabName) {
        long start = System.currentTimeMillis();
        Tab tab = new Tab(tabName);
        tabPane.getTabs().add(tab);
        HelperFunctions helperFunctions = new HelperFunctions();
        Node node = helperFunctions.loadFxml(bundle, path, controller);
        node.setUserData(controller);
        tab.setContent(node);
        long end = System.currentTimeMillis();
        System.out.println("Loadtime (" + controller.toString() + ") in ms: " + (end - start));
        return tab;
    }

    public static BorderPane createTab(ResourceBundle bundle, String path, Object controller) {
        long start = System.currentTimeMillis();
        BorderPane borderPane = new BorderPane();
        HelperFunctions helperFunctions = new HelperFunctions();
        Node node = helperFunctions.loadFxml(bundle, path, controller);
        node.setUserData(controller);
        borderPane.setCenter(node);
        long end = System.currentTimeMillis();
        System.out.println("Loadtime (" + controller.toString() + ") in ms: " + (end - start));
        return borderPane;
    }

    public static Node addPlugin(ResourceBundle bundle, String path, Object controller) {
        long start = System.currentTimeMillis();
        HelperFunctions helperFunctions = new HelperFunctions();
        Node node = helperFunctions.loadFxml(bundle, path, controller);
        node.setUserData(controller);
        long end = System.currentTimeMillis();
        System.out.println("Loadtime (" + controller.toString() + ") in ms: " + (end - start));
        return node;
    }

    public static byte[] doubleToByte(double coord, ByteOrder byteOrder) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).order(byteOrder).putDouble(coord);
        return bytes;
    }

    public static double byteToDouble(byte[] bytes, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bytes).order(byteOrder).getDouble();
    }

    public static List<Short> readSrtmFile(String path, ByteOrder byteOrder) throws Exception {
        List<Short> rawList = new ArrayList<Short>();

        DataInputStream reader = new DataInputStream(new FileInputStream(new File(path)));
        int nBytesToRead = reader.available();

        if (nBytesToRead > 0) {

            long start = System.currentTimeMillis();

            byte[] bytes = new byte[nBytesToRead];
            reader.read(bytes);

            for (int i = 0; i < nBytesToRead / 2; i++) {
                byte[] slice = Arrays.copyOfRange(bytes, 0 + (2 * i), 2 + (2 * i));
                ByteBuffer buffer = ByteBuffer.wrap(slice);
                buffer.order(byteOrder);
                rawList.add(buffer.getShort());
            }

            long end = System.currentTimeMillis();
            if (Globals.DEBUG) {
                System.out.println("Time Read: " + (end - start) + " ms");
            }
        }
        reader.close();

        return rawList;
    }

    public static double getPercentFromHeight(int min, int max, double height) {
        double erg = 0;
        erg = ((height - min) / (max - min)) * 100.0;
        if (min == max && min == 0) {
            return 0;
        }
        return erg;
    }

    public static int RGBtoInt(int r, int g, int b) {
        int rgb;

        if ((r < 256 && g < 256 && b < 256) && (r >= 0 && g >= 0 && b >= 0)) {
            rgb = new Color(r, g, b).getRGB();
        } else {
            rgb = new Color(255, 255, 255).getRGB();
        }

        if (r < 0 && g < 0 && b < 0) {
            rgb = new Color(0, 0, 0).getRGB();
        }

        return rgb;
    }

    public static javafx.scene.paint.Color genColor(List<ColorRow> colors, double pct) {
        //pct = pct / 100.0;
        int z = 1;
        for (int i = 1; i < colors.size() - 1; i++) {
            if (pct < colors.get(i).getPercent()) {
                break;
            }
            z++;
        }
        //ColorRow lower = colors.get(z - 1);
        //ColorRow upper = colors.get(z);
        ColorRow lower = colors.get(0);
        ColorRow upper = colors.get(0);
        if (colors.size() - 1 > 0) {
            lower = colors.get(z - 1);
            upper = colors.get(z);
        }
        double range = upper.getPercent() - lower.getPercent();
        double rangePct = (pct - lower.getPercent()) / range;
        double pctLower = 1 - rangePct;
        double pctUpper = rangePct;
        int r = (int) Math.floor(lower.getRed() * pctLower + upper.getRed() * pctUpper);
        int g = (int) Math.floor(lower.getGreen() * pctLower + upper.getGreen() * pctUpper);
        int b = (int) Math.floor(lower.getBlue() * pctLower + upper.getBlue() * pctUpper);
        return javafx.scene.paint.Color.rgb(r, g, b);
    }

    public static double getDistance(double lon1, double lat1, double lon2, double lat2) {
        double dist = SloppyMath.haversinMeters(lat1, lon1, lat2, lon2) / 1000.0;
        return dist;
    }

    public static String getTileNameSRTM(double lon, double lat) {
        String tileName = "";

        int lod = (int) Math.floor(lon);
        int lad = (int) Math.floor(lat);

        if (lad >= 0) {
            tileName += "N";
        } else {
            tileName += "S";
        }
        tileName += String.format("%02d", Math.abs(lad));

        if (lod >= 0) {
            tileName += "E";
        } else {
            tileName += "W";
        }
        tileName += String.format("%03d", Math.abs(lod));

        tileName += ".hgt";

        return tileName;
    }

    public static String getTileNameHCM(double lon, double lat) {
        String tileName = "";

        int lod = (int) Math.floor(lon);
        int lad = (int) Math.floor(lat);

        if (lod >= 0) {
            tileName += "E";
        } else {
            tileName += "W";
        }
        tileName += String.format("%03d", Math.abs(lod));

        if (lad >= 0) {
            tileName += "N";
        } else {
            tileName += "S";
        }
        tileName += String.format("%02d", Math.abs(lad));

        if (Math.abs(lad) < 50) {
            tileName += ".33";
        } else {
            tileName += ".63";
        }

        return tileName;
    }

    private static int[][] createTableLayout() {

        int array[][] = new int[12][12];
        int phiCount = 0;// Beginnt mit 0
        int thetaCount = 12;

        for (int i = 0; i < 12; i += 1) {
            int subArray[] = new int[12];
            thetaCount--;
            for (int j = 0; j < 12; j += 1) {
                subArray[j] = phiCount;
                phiCount++;
            }
            array[thetaCount] = subArray;
        }

        return array;
    }

    private static HashMap<Integer, List<List<Short>>> createTiles(List<Short> stream, int resh) {
        HashMap<Integer, List<List<Short>>> map = new HashMap<Integer, List<List<Short>>>();

        int stride = stream.size() / 144;
        for (int i = 0; i < 12 * 12; i++) {
            List<List<Short>> kachel = scan(stream, stride * i, stride * (i + 1), resh);
            map.put(i, kachel);
        }

        return map;
    }

    private static List<List<Short>> scan(List<Short> rawList, int start, int end, int resh) {
        List<Short> block0 = rawList.subList(start, end);
        List<List<Short>> kachel = new ArrayList<List<Short>>();

        for (int i = 101; i > 0; i--) {
            List<Short> sub = block0.subList(i * resh - resh, i * resh);
            kachel.add(sub);
        }

        return kachel;
    }

    private static short[][] createBigMap(int[][] tableLayout, int resh, HashMap<Integer, List<List<Short>>> tileMap) {
        short bigMap[][] = new short[12 * 101][12 * resh];

        for (int i = 0; i < tableLayout.length; i++) {
            for (int j = 0; j < tableLayout[i].length; j++) {
                List<List<Short>> tile = tileMap.get(tableLayout[i][j]);

                for (int z = 0; z < 101; z++) {
                    for (int k = 0; k < resh; k++) {
                        bigMap[i * 101 + z][j * resh + k] = tile.get(z).get(k);
                    }
                }
            }
        }

        return bigMap;
    }

    public static short[][] readHcmFile(String path, ByteOrder byteOrder) {
        List<Short> fileStream = null;
        try {
            fileStream = readSrtmFile(path, byteOrder);
        } catch (Exception e) {
            _log.error(e.getMessage());
        }

        int[][] tableLayout = createTableLayout();

        int resh = 51;
        if (fileStream.size() == 1212 * 1212) {
            resh = 101;
        }

        HashMap<Integer, List<List<Short>>> tileMap = createTiles(fileStream, resh);

        short[][] bigMap = createBigMap(tableLayout, resh, tileMap);

        return bigMap;
    }

    public static int getHeightFromTile(short bigMap[][], GeoPosition geoPosition) {
        double dezLon = Math.abs(geoPosition.getLongitude());
        double dezLat = Math.abs(geoPosition.getLatitude());

        int intLon = (int) dezLon;
        int intLat = (int) dezLat;

        double diffLon = dezLon - intLon;
        double diffLat = dezLat - intLat;
        diffLat = 1.0 - diffLat;

        int sizeHor = bigMap[0].length;
        int sizeVer = bigMap.length;

        double x = sizeHor * diffLon;
        double y = sizeVer * diffLat;

        return bigMap[(int) y][(int) x];
    }
}
