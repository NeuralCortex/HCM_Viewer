package com.fx.hcm.controller.tabs;

import com.fx.hcm.Globals;
import com.fx.hcm.cell.ColorRowCell;
import com.fx.hcm.controller.MainController;
import com.fx.hcm.controller.PopulateInterface;
import com.fx.hcm.painter.CrossPainter;
import com.fx.hcm.painter.SrtmTilePainter;
import com.fx.hcm.pojo.ColorRow;
import com.fx.hcm.pojo.PngPos;
import com.fx.hcm.pojo.SrtmRaster;
import com.fx.hcm.tools.HelperFunctions;
import com.fx.hcm.tools.MousePositionListener;
import com.fx.hcm.tools.PosSelectionAdapter;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.embed.swing.SwingNode;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class TileController implements Initializable, PopulateInterface {
    
    @FXML
    private BorderPane borderPane;
    @FXML
    private Button btnLoad;
    @FXML
    private Button btnReset;
    @FXML
    private Button btnPos;
    @FXML
    private Button btnExportHf;
    @FXML
    private Button btnExportCol;
    @FXML
    private Button btnExportPov;
    @FXML
    private Label lbExport;
    @FXML
    private Label lbColor;
    @FXML
    private Label lbTile;
    @FXML
    private Label lbType;
    @FXML
    private Label lbTileMouse;
    @FXML
    private SwingNode swingNode;
    @FXML
    private HBox hboxTable;
    @FXML
    private HBox hboxOben;
    @FXML
    private HBox hboxDir;
    @FXML
    private VBox vboxData;
    @FXML
    private TableView<ColorRow> tableColor;
    @FXML
    private TextField tfPercent;
    @FXML
    private ColorPicker cpColor;
    @FXML
    private CheckBox cbAlpha;
    @FXML
    private CheckBox cbHf;
    @FXML
    private CheckBox cbCol;
    @FXML
    private CheckBox cbPov;
    @FXML
    private Button btnSaveColor;
    @FXML
    private Button btnColor;
    @FXML
    private TextField tfLon;
    @FXML
    private TextField tfLat;
    
    private static final Logger _log = LogManager.getLogger(TileController.class);
    private final MainController mainController;
    
    private final JXMapViewer mapViewer = new JXMapViewer();
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();
    private GeoPosition marker;
    private List<ColorRow> colors;
    
    private final double lon = 10.671745101119196;
    private final double lat = 50.661742127393836;
    
    private short bigMap[][];
    private int min = 9999;
    private int max = -1000;
    
    public TileController(MainController mainController) {
        this.mainController = mainController;
    }
    
    @Override
    public void initialize(URL url, ResourceBundle bundle) {
        
        String colorMap = Globals.propman.getProperty(Globals.HCM_TILE_COLOR, null);
        if (colorMap == null) {
            colors = initColors();
        } else {
            colors = expandColorMap(colorMap);
        }
        
        vboxData.setPrefWidth(300.0);
        
        hboxTable.setId("hec-background-blue");
        hboxOben.setId("hec-background-blue");
        hboxDir.setId("hec-background-blue");
        lbExport.setId("hec-text-white");
        lbColor.setId("hec-text-white");
        lbTile.setId("hec-text-white");
        lbType.setId("hec-text-white");
        lbTileMouse.setId("hec-text-white");
        cbAlpha.setId("hec-text-white");
        
        lbTile.setText("");
        lbType.setText("");
        cbAlpha.setText("Alpha");
        lbExport.setText("Export");
        lbColor.setText(bundle.getString("table.srtm.color"));
        btnLoad.setText(bundle.getString("btn.load.srtm.tile"));
        btnReset.setText(bundle.getString("btn.reset"));
        btnSaveColor.setText(bundle.getString("btn.update"));
        btnPos.setText(bundle.getString("btn.pos"));
        tfLon.setPromptText(bundle.getString("tf.lon"));
        tfLat.setPromptText(bundle.getString("tf.lat"));
        tfPercent.setPromptText(bundle.getString("table.percent"));
        //Export
        btnExportHf.setText("Export Heightfield PNG");
        btnExportCol.setText("Export Color PNG");
        btnExportPov.setText("Export POV-Ray POV+TEX");
        btnColor.setText(bundle.getString("btn.reset.colors"));
        
        double w = 150.0;
        btnExportHf.setMinWidth(w);
        btnExportCol.setMinWidth(w);
        btnExportPov.setMinWidth(w);
        
        cbHf.setText(bundle.getString("cb.marker"));
        cbCol.setText(bundle.getString("cb.marker"));
        cbPov.setText(bundle.getString("cb.marker"));
        
        cbHf.setSelected(true);
        cbCol.setSelected(true);
        cbPov.setSelected(true);
        
        TableColumn<ColorRow, Integer> colIndex = new TableColumn<>(bundle.getString("table.idx"));
        TableColumn<ColorRow, Double> colPercent = new TableColumn<>(bundle.getString("table.percent"));
        TableColumn<ColorRow, javafx.scene.paint.Color> colPaint = new TableColumn<>(bundle.getString("table.color"));
        
        colIndex.setCellValueFactory(new PropertyValueFactory<>("idx"));
        colPercent.setCellValueFactory(new PropertyValueFactory<>("percent"));
        colPaint.setCellValueFactory(new PropertyValueFactory<>("color"));
        colPaint.setCellFactory((param) -> {
            return new ColorRowCell();
        });
        
        colIndex.setSortable(false);
        colPercent.setSortable(false);
        colPaint.setSortable(false);
        
        tableColor.getColumns().addAll(colIndex, colPercent, colPaint);
        tableColor.setItems(FXCollections.observableArrayList(colors));
        
        tableColor.getSelectionModel().selectedItemProperty().addListener((ov, o, n) -> {
            if (n != null) {
                double percent = n.getPercent();
                javafx.scene.paint.Color color = n.getColor();
                
                tfPercent.setText(percent + "");
                cpColor.setValue(color);
            }
        });
        
        initOsmMap();
        
        borderPane.widthProperty().addListener(e -> {
            mapViewer.repaint();
        });
        borderPane.heightProperty().addListener(e -> {
            mapViewer.repaint();
        });
        
        btnLoad.setOnAction(e -> {
            
            min = 9999;
            max = -1000;
            marker = null;
            
            FileChooser fileChooser = new FileChooser();
            
            String saveDir = Globals.propman.getProperty(Globals.HCM_TILE_DIR, System.getProperty("user.dir"));
            
            fileChooser.setInitialDirectory(new File(saveDir));
            File file = fileChooser.showOpenDialog(mainController.getStage());
            
            if (file != null && file.isFile()) {
                
                Globals.propman.setProperty(Globals.HCM_TILE_DIR, file.getParent());
                Globals.propman.save();
                
                try {
                    bigMap = HelperFunctions.readHcmFile(file.getPath(), Globals.BYTE_ORDER);
                } catch (Exception ex) {
                    _log.info(file.getPath() + " not found.");
                }
                
                for (int j = 0; j < bigMap.length; j++) {
                    for (int k = 0; k < bigMap[j].length; k++) {
                        short height = bigMap[j][k];
                        if (height < min) {
                            min = height;
                        }
                        if (height > max) {
                            max = height;
                        }
                    }
                }
                
                int horRes = bigMap[0].length;
                int verRes = bigMap.length;
                
                lbTile.setText(file.getName());
                lbType.setText(horRes + "x" + verRes + " Pixel");
                
                mapViewer.setOverlayPainter(null);
                for (int i = painters.size() - 1; i >= 0; i--) {
                    Painter painter = painters.get(i);
                    if (painter instanceof SrtmTilePainter) {
                        painters.remove(painter);
                    }
                    if (painter instanceof CrossPainter) {
                        painters.remove(painter);
                    }
                }
                mapViewer.repaint();
                
                SrtmRaster srtmRaster = new SrtmRaster(file.getName());
                SrtmTilePainter srtmTilePainter = new SrtmTilePainter(srtmRaster, bigMap, min, max);
                srtmTilePainter.setColors(colors);
                painters.add(srtmTilePainter);
                
                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                mapViewer.repaint();
                
                reorderPainters();
            }
        });
        
        cbAlpha.selectedProperty().addListener((ov, o, n) -> {
            for (int i = 0; i < painters.size(); i++) {
                Painter painter = painters.get(i);
                if (painter instanceof SrtmTilePainter) {
                    SrtmTilePainter srtmTilePainter = (SrtmTilePainter) painter;
                    srtmTilePainter.setShowAlpha(n);
                    mapViewer.repaint();
                    break;
                }
            }
        });
        
        btnReset.setOnAction(e -> {
            //tableViewDir.getItems().clear();
            //tableView.getItems().clear();
            marker = null;
            bigMap = null;
            lbTile.setText("");
            lbType.setText("");
            mapViewer.setOverlayPainter(null);
            painters.clear();
            mapViewer.repaint();
        });
        
        btnSaveColor.setOnAction(e -> {
            ColorRow colorRow = tableColor.getSelectionModel().getSelectedItem();
            if (colorRow != null) {
                try {
                    double percent = Double.valueOf(tfPercent.getText());
                    javafx.scene.paint.Color color = cpColor.getValue();
                    
                    colorRow.setPercent(percent / 100.0);
                    colorRow.setColor(color);
                    
                    for (int i = 0; i < painters.size(); i++) {
                        Painter painter = painters.get(i);
                        if (painter instanceof SrtmTilePainter) {
                            SrtmTilePainter srtmTilePainter = (SrtmTilePainter) painter;
                            srtmTilePainter.setColors(tableColor.getItems());
                            mapViewer.repaint();
                            break;
                        }
                    }
                    
                    Globals.propman.put(Globals.HCM_TILE_COLOR, flattenColorMap());
                    Globals.propman.save();
                    
                    tableColor.refresh();
                } catch (Exception ex) {
                    _log.error(ex.getMessage());
                }
            }
        });
        
        btnExportHf.setOnAction(e -> {
            exportMap(bundle, Globals.EXPORT_TYPE.HEIGHT_FIELD);
        });
        
        btnExportCol.setOnAction(e -> {
            exportMap(bundle, Globals.EXPORT_TYPE.COLOR_MAP);
        });
        
        btnExportPov.setOnAction(e -> {
            exportMap(bundle, Globals.EXPORT_TYPE.POV_RAY);
        });
        
        tfLat.setOnKeyPressed(e -> {
            getClipboardFromGoogleMaps(e);
        });
        
        tfLon.setOnKeyPressed(e -> {
            getClipboardFromGoogleMaps(e);
        });
        
        btnPos.setOnAction(e -> {
            if (!tfLat.getText().isEmpty() && !tfLon.getText().isEmpty()) {
                double lat = Double.valueOf(tfLat.getText());
                double lon = Double.valueOf(tfLon.getText());
                
                marker = new GeoPosition(lat, lon);
                
                for (int i = painters.size() - 1; i >= 0; i--) {
                    Painter painter = painters.get(i);
                    if (painter instanceof CrossPainter) {
                        painters.remove(painter);
                    }
                }
                
                CrossPainter crossPainter = new CrossPainter(marker, bigMap, isOut(marker));
                painters.add(crossPainter);
                CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
                mapViewer.setOverlayPainter(painter);
                mapViewer.repaint();
            }
        });
        
        btnColor.setOnAction(e -> {
            colors = initColors();
            
            tableColor.getItems().clear();
            tableColor.setItems(FXCollections.observableArrayList(colors));
            
            for (int i = 0; i < painters.size(); i++) {
                Painter painter = painters.get(i);
                if (painter instanceof SrtmTilePainter) {
                    SrtmTilePainter srtmTilePainter = (SrtmTilePainter) painter;
                    srtmTilePainter.setColors(tableColor.getItems());
                    mapViewer.repaint();
                    break;
                }
            }
            
            tfPercent.setText("");
            cpColor.setValue(javafx.scene.paint.Color.WHITE);
            
            Globals.propman.put(Globals.HCM_TILE_COLOR, flattenColorMap());
            Globals.propman.save();
        });
    }
    
    private void reorderPainters() {
        CrossPainter crossPainter = null;
        SrtmTilePainter srtmTilePainter = null;
        for (int i = 0; i < painters.size(); i++) {
            Painter painter = painters.get(i);
            if (painter instanceof SrtmTilePainter) {
                srtmTilePainter = (SrtmTilePainter) painter;
            }
            if (painter instanceof CrossPainter) {
                crossPainter = (CrossPainter) painter;
            }
        }
        painters.clear();
        if (srtmTilePainter != null) {
            painters.add(srtmTilePainter);
        }
        if (crossPainter != null) {
            painters.add(crossPainter);
        }
        
        CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
        mapViewer.setOverlayPainter(painter);
        mapViewer.repaint();
    }
    
    private void getClipboardFromGoogleMaps(KeyEvent e) {
        if (e.isControlDown() && e.getCode() == KeyCode.V) {
            try {
                String rawData = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                String wgsPos[] = rawData.split(",");
                tfLat.setText(wgsPos[0].trim());
                tfLon.setText(wgsPos[1].trim());
            } catch (Exception ex) {
                _log.error(ex.getMessage());
            }
        }
    }
    
    private void exportMap(ResourceBundle bundle, Globals.EXPORT_TYPE export_type) {
        if (bigMap != null) {
            
            int height = bigMap.length;
            int width = bigMap[0].length;
            
            BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            for (int j = 0; j < bigMap[0].length; j++) {
                for (int i = 0; i < bigMap.length; i++) {
                    short h = bigMap[i][j];
                    
                    if (export_type == Globals.EXPORT_TYPE.HEIGHT_FIELD) {

                        //System.out.println("min: "+min+" max: "+max);
                        double percent = HelperFunctions.getPercentFromHeight(min, max, h);
                        percent = percent / 100.0f * 255.0f;
                        bufferedImage.setRGB(j, i, HelperFunctions.RGBtoInt((int) percent, (int) percent, (int) percent));
                        
                    } else if (export_type == Globals.EXPORT_TYPE.COLOR_MAP) {
                        
                        double percent = HelperFunctions.getPercentFromHeight(min, max, h);
                        javafx.scene.paint.Color colorHeight = HelperFunctions.genColor(colors, percent);
                        
                        double red = colorHeight.getRed();
                        double green = colorHeight.getGreen();
                        double blue = colorHeight.getBlue();
                        
                        java.awt.Color c = new java.awt.Color((int) (red * 255), (int) (green * 255), (int) (blue * 255));
                        bufferedImage.setRGB(j, i, c.getRGB());
                        
                    } else if (export_type == Globals.EXPORT_TYPE.POV_RAY) {
                        
                        double percent = HelperFunctions.getPercentFromHeight(min, max, h);
                        percent = percent / 100.0f * 255.0f;
                        bufferedImage.setRGB(j, i, HelperFunctions.RGBtoInt((int) percent, (int) percent, (int) percent));
                        
                    }
                    
                }
            }
            
            if (export_type == Globals.EXPORT_TYPE.HEIGHT_FIELD) {
                if (cbHf.isSelected() && marker != null) {
                    drawPosInPng(bufferedImage.createGraphics(), width, height);
                }
            } else if (export_type == Globals.EXPORT_TYPE.COLOR_MAP) {
                if (cbCol.isSelected() && marker != null) {
                    drawPosInPng(bufferedImage.createGraphics(), width, height);
                }
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initOwner(mainController.getStage());
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setHeaderText(lbTile.getText());
            Button btnOk = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
            btnOk.setText(bundle.getString("btn.save"));
            Image image = SwingFXUtils.toFXImage(bufferedImage, null);
            
            VBox vBox = new VBox();
            
            ImageView imageView = new ImageView(image);
            imageView.setFitHeight(612);
            imageView.setPreserveRatio(true);
            
            VBox.setMargin(imageView, new Insets(10, 10, 0, 10));
            
            vBox.getChildren().add(imageView);
            
            alert.getDialogPane().setContent(vBox);
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                File pngDir = new File(Globals.PNG_PATH);
                if (!pngDir.exists()) {
                    pngDir.mkdir();
                }
                
                File povDir = new File(Globals.POV_PATH);
                if (!povDir.exists()) {
                    povDir.mkdir();
                }
                
                double x = -1000, z = -1000;
                
                if (marker != null && cbPov.isSelected()) {
                    if (!isOut(marker)) {
                        PngPos pngPos = positionMarker(width, height);
                        int lon = pngPos.getLon();
                        int lat = pngPos.getLat();
                        
                        if (lbTile.getText().toUpperCase().contains("N") && lbTile.getText().toUpperCase().contains("E")) {
                            x = lon / (double) (width);
                            z = 1.0 - lat / (double) (height);
                        }
                        
                        if (lbTile.getText().toUpperCase().contains("S") && lbTile.getText().toUpperCase().contains("E")) {
                            x = lon / (double) (width);
                            z = 1.0 - lat / (double) (height);
                        }
                        
                        if (lbTile.getText().toUpperCase().contains("N") && lbTile.getText().toUpperCase().contains("W")) {
                            x = lon / (double) (width);
                            z = 1.0 - lat / (double) (height);
                        }
                        
                        if (lbTile.getText().toUpperCase().contains("S") && lbTile.getText().toUpperCase().contains("W")) {
                            x = lon / (double) (width);
                            z = 1.0 - lat / (double) (height);
                        }
                    }
                }
                
                String fileName = "";
                String fileNamePNG = "";
                String fileNamePOV = "";
                
                String end = "";
                String type = "";
                
                int horRes = bigMap[0].length;
                
                fileName = lbTile.getText();
                
                if (horRes == 612) {
                    if (fileName.toUpperCase().endsWith("E")) {
                        end = ".63E";
                        type = "E";
                    } else {
                        end = ".63M";
                        type = "M";
                    }
                } else {
                    if (fileName.toUpperCase().endsWith("E")) {
                        end = ".33E";
                        type = "E";
                    } else {
                        end = ".33M";
                        type = "M";
                    }
                }
                
                if (export_type == Globals.EXPORT_TYPE.HEIGHT_FIELD) {
                    fileName = lbTile.getText().replace(end, "") + "_" + type + "_HEIGHTFIELD.png";
                } else if (export_type == Globals.EXPORT_TYPE.COLOR_MAP) {
                    fileName = lbTile.getText().replace(end, "") + "_" + type + "_COLOR.png";
                } else if (export_type == Globals.EXPORT_TYPE.POV_RAY) {
                    fileNamePNG = lbTile.getText().replace(end, "") + "_" + type + "_TEX.png";
                    fileNamePOV = lbTile.getText().replace(end, "") + "_" + type + ".pov";
                }
                
                File outputFile = new File(Globals.PNG_PATH + "/" + fileName);
                
                if (export_type == Globals.EXPORT_TYPE.POV_RAY) {
                    outputFile = new File(Globals.POV_PATH + "/" + fileNamePNG);
                }
                
                try {
                    if (export_type == Globals.EXPORT_TYPE.POV_RAY) {
                        String blue = loadBluePrint();
                        String pov = blue.replace("#file", fileNamePNG);
                        pov = pov.replace("#x1", x + "");
                        pov = pov.replace("#z1", z + "");
                        
                        BufferedWriter writer = new BufferedWriter(new FileWriter(new File(Globals.POV_PATH + "/" + fileNamePOV)));
                        writer.write(pov);
                        writer.close();
                    }
                    
                    ImageIO.write(bufferedImage, "png", outputFile);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    
    private String loadBluePrint() throws Exception {
        Path path = Path.of(Globals.POV_BLUEPRINT_HEIGHT_PATH);
        return Files.readString(path);
    }
    
    private void drawPosInPng(Graphics2D graphics2D, int width, int height) {
        if (!isOut(marker)) {
            PngPos pngPos = positionMarker(width, height);
            int lon = pngPos.getLon();
            int lat = pngPos.getLat();
            
            graphics2D.setColor(Color.RED);
            BasicStroke bs = new BasicStroke(3);
            graphics2D.setStroke(bs);
            
            int halfLine = 40;
            
            graphics2D.drawLine((int) lon - halfLine, (int) lat, (int) lon + halfLine, (int) lat);
            graphics2D.drawLine((int) lon, (int) lat - halfLine, (int) lon, (int) lat + halfLine);
        }
    }
    
    private PngPos positionMarker(int width, int height) {
        double markLon = marker.getLongitude();
        double markLat = marker.getLatitude();
        
        double dezLon = Math.abs(marker.getLongitude());
        double dezLat = Math.abs(marker.getLatitude());

        //System.out.println("markLon: " + markLon + " markLat: " + markLat);
        int intLon = (int) dezLon;
        int intLat = (int) dezLat;
        
        double diffLon = dezLon - intLon;
        double diffLat = dezLat - intLat;
        
        double diffLonPov = diffLon;
        double diffLatPov = diffLat;

        //NE
        if (markLon > 0 && markLat > 0) {
            diffLat = 1.0 - diffLat;
        }

        //SE
        if (markLon > 0 && markLat < 0) {
            //Do nothing
        }

        //SW
        if (markLon < 0 && markLat < 0) {
            diffLon = 1.0 - diffLon;
        }

        //NW
        if (markLon < 0 && markLat > 0) {
            diffLon = 1.0 - diffLon;
            diffLat = 1.0 - diffLat;
        }
        
        int lon = (int) (width * diffLon);
        int lat = (int) (height * diffLat);
        
        return new PngPos(lon, lat);
    }
    
    private String flattenColorMap() {
        String erg = "";
        for (ColorRow colorRow : tableColor.getItems()) {
            erg += colorRow.getIdx() + "#";
            erg += colorRow.getPercent() + "#";
            //erg += (int) colorRow.getRed() + ";" + (int) colorRow.getGreen() + ";" + (int) colorRow.getBlue() + "#";
            erg += getColorWeb(colorRow) + "#";
        }
        return erg.substring(0, erg.length() - 1);
    }
    
    private String getColorWeb(ColorRow colorRow) {
        return String.format("%02X%02X%02X", ((int) colorRow.getRed()), ((int) colorRow.getGreen()), ((int) colorRow.getBlue()));
    }
    
    private List<ColorRow> expandColorMap(String flatMap) {
        List<ColorRow> list = new ArrayList<>();
        String row[] = flatMap.split("#");
        for (int i = 0; i < row.length; i += 3) {
            String idxStr = row[0 + i];
            String percentStr = row[1 + i];
            String colorWeb = row[2 + i];
            
            int idx = Integer.valueOf(idxStr);
            double percent = Double.valueOf(percentStr);
            
            list.add(new ColorRow(idx, percent / 100.0, javafx.scene.paint.Color.web("#" + colorWeb)));
        }
        return list;
    }
    
    private List<ColorRow> initColors() {
        List<ColorRow> colorRow = new ArrayList<>();
        colorRow.add(new ColorRow(0, 0.0, javafx.scene.paint.Color.web("#0000ff")));
        colorRow.add(new ColorRow(1, 0.5, javafx.scene.paint.Color.web("#ADFF2F")));
        colorRow.add(new ColorRow(2, 0.6, javafx.scene.paint.Color.web("#9ACD32")));
        colorRow.add(new ColorRow(3, 0.7, javafx.scene.paint.Color.web("#FFA500")));
        colorRow.add(new ColorRow(4, 0.8, javafx.scene.paint.Color.web("#B8860B")));
        colorRow.add(new ColorRow(5, 0.9, javafx.scene.paint.Color.web("#B3801A")));
        colorRow.add(new ColorRow(6, 1.0, javafx.scene.paint.Color.web("#FFFFFF")));
        return colorRow;
    }
    
    private Color genColor() {
        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);
        
        Color color = new Color(r, g, b);
        
        return color;
    }
    
    private void initOsmMap() {
        
        TileFactoryInfo tileFactoryInfo = new OSMTileFactoryInfo();
        DefaultTileFactory defaultTileFactory = new DefaultTileFactory(tileFactoryInfo);
        defaultTileFactory.setThreadPoolSize(Runtime.getRuntime().availableProcessors());
        mapViewer.setTileFactory(defaultTileFactory);
        
        final JLabel labelAttr = new JLabel();
        mapViewer.setLayout(new BorderLayout());
        mapViewer.add(labelAttr, BorderLayout.SOUTH);
        labelAttr.setText(defaultTileFactory.getInfo().getAttribution() + " - " + defaultTileFactory.getInfo().getLicense());

        // Set the focus
        GeoPosition zm = new GeoPosition(lat, lon);
        
        mapViewer.setZoom(11);
        mapViewer.setAddressLocation(zm);

        // Add interactions
        MouseInputListener mil = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(mil);
        mapViewer.addMouseMotionListener(mil);
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));
        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
        
        MousePositionListener mousePositionListener = new MousePositionListener(mapViewer);
        mousePositionListener.setGeoPosListener((GeoPosition geoPosition) -> {
            Platform.runLater(() -> {
                mainController.getLbStatus().setText("Longitude: " + geoPosition.getLongitude() + " Latitude: " + geoPosition.getLatitude());
                lbTileMouse.setText(HelperFunctions.getTileNameHCM(geoPosition.getLongitude(), geoPosition.getLatitude()));
                
                for (Painter painter : painters) {
                    if (painter instanceof SrtmTilePainter) {
                        SrtmTilePainter srtmTilePainter = (SrtmTilePainter) painter;
                        srtmTilePainter.setGeoPositionSel(geoPosition);
                    }
                }
                mapViewer.repaint();
            });
        });
        mapViewer.addMouseMotionListener(mousePositionListener);

        //Custom
        PosSelectionAdapter posSelectionAdapter = new PosSelectionAdapter(mapViewer);
        posSelectionAdapter.setPosSelectionAdapterListener((GeoPosition geoPosition) -> {
            marker = geoPosition;
            
            for (int i = painters.size() - 1; i >= 0; i--) {
                Painter painter = painters.get(i);
                if (painter instanceof CrossPainter) {
                    painters.remove(painter);
                }
            }
            
            CrossPainter crossPainter = new CrossPainter(marker, bigMap, isOut(marker));
            painters.add(crossPainter);
            CompoundPainter<JXMapViewer> painter = new CompoundPainter<>(painters);
            mapViewer.setOverlayPainter(painter);
            mapViewer.repaint();
        });
        mapViewer.addMouseListener(posSelectionAdapter);
        
        try {
            SwingUtilities.invokeAndWait(() -> {
                swingNode.setContent(mapViewer);
                swingNode.requestFocus();
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                swingNode.getContent().repaint();
            }
        }, Globals.WAIT_TIME_SWING);
    }
    
    private boolean isOut(GeoPosition geoPosition) {
        boolean out = true;
        for (int i = painters.size() - 1; i >= 0; i--) {
            Painter painter = painters.get(i);
            if (painter instanceof SrtmTilePainter) {
                SrtmTilePainter srtmTilePainter = (SrtmTilePainter) painter;
                out = srtmTilePainter.isOut(geoPosition);
            }
        }
        return out;
    }
    
    @Override
    public void populate() {
        
    }
    
    @Override
    public void reset() {
        
    }
    
    @Override
    public void clear() {
        
    }
}
