package com.example.osmparsing.MVC;

import com.example.osmparsing.*;
import com.example.osmparsing.RTree.BoundingBox;
import com.example.osmparsing.Routing.RoutingAlgorithms.Edge;
import com.example.osmparsing.kdTree.RectHV;
import com.example.osmparsing.relations.MultiPolygon;
import com.example.osmparsing.wayEnums.HighwayType;
import com.example.osmparsing.wayEnums.NaturalType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.transform.Affine;
import javafx.scene.transform.NonInvertibleTransformException;
import javafx.scene.Group;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

public class View {
    int CANVAS_WIDTH = 1280;
    int CANVAS_HEIGHT = 720;
    double TRANSFORM_LATITUDE = 0.56; //
    Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    GraphicsContext gc = canvas.getGraphicsContext2D();

    double x1 = 100;
    double y1 = 100;
    double x2 = 200;
    double y2 = 800;

    private double pinLat = Double.NaN;
    private double pinLon = Double.NaN;
    private double destPinLat = Double.NaN;
    private double destPinLon = Double.NaN;

    Affine trans = new Affine();
    Model model;
    Controller controller;

    Address searchedAddress;
    Address destinationAddress;
    Group multipolygonGroup = new Group();
    boolean hiddenPOIs = true;

    private OSMElement highlightedElement = null;

    // Fields related to initController
    private ComboBox<String> originDropdown;
    private ComboBox<String> destinationDropdown;
    private Button searchRouteButton;
    private Button clearButton;
    private Button poiButton;
    private Slider slider;
    private ComboBox<String> colorModes;
    private Label travelTimeLabel;
    private Label routeInfoLabel;
    //
    // Fields related to initLayout
    private HBox topControls;
    private HBox bottomControls;
    private VBox rightPane;
    private StackPane mapHolder;
    //

    /**
     * Constructor for View class, constructs the window in runtime. It renders the map and buttons on it.
     * @param model uses the parser from model based
     * @param stage sets the stage for the window
     * @throws NonInvertibleTransformException
     */
    public View(Model model, Stage stage) throws NonInvertibleTransformException{

       init_Model_and_Controller(model);
       initControls();
       initLayout(stage);
       initEventHandlers();
       init_Map_Binding_and_Initial_View();
    }

    /**
     * Initialises the model and controller
     * @param model instantiates this model
     */
    private void init_Model_and_Controller(Model model) {
       this.model = model;
       controller = new Controller(model, this);
    }

    /**
     * Initialises the controls for dropdown menus and buttons.
     */
    private void initControls() {
       originDropdown = prepareComboBox(address -> {
           searchedAddress = address;
           pinLat = address.getLat();
           pinLon = address.getLon();
       });
       originDropdown.setPromptText("Search Address...");
       originDropdown.setMaxWidth(250);

       destinationDropdown = prepareComboBox(address -> {
           destinationAddress = address;
           pinLat = address.getLat();
           pinLon = address.getLon();
       });
       destinationDropdown.setPromptText("Destination Address...");
       destinationDropdown.setMaxWidth(250);

       searchRouteButton = new Button("Search Route");
       searchRouteButton.setStyle("-fx-padding: 10; -fx-font-weight: bold");
       clearButton = new Button("Clear");
       clearButton.setStyle("-fx-padding: 10; -fx-font-weight: bold");

       poiButton = preparePOIButton();
       slider = prepareSlider();
       colorModes = prepareColorModes();
       travelTimeLabel = new Label();
       routeInfoLabel = new Label("Select mode of transport");
       routeInfoLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold");
   }

    /**
     * Builds and displays the application’s scene graph:
     *  </br>
     *  - wraps the map canvas in a resizable StackPane
     *  </br>
     *  - arranges top, bottom, and right control bars in a BorderPane
     *  </br>
     *  - binds canvas size so it automatically resizes
     *  </br>
     *  - creates and shows the Scene on the given Stage
     */
    private void initLayout(Stage stage) {
       mapHolder = new StackPane(canvas);
       bindCanvasSize();

       topControls = new HBox(10, originDropdown, destinationDropdown, searchRouteButton, clearButton);
       topControls.setAlignment(Pos.TOP_LEFT);
       topControls.setStyle("-fx-padding: 10;");

       bottomControls = new HBox(10, poiButton, slider, colorModes, travelTimeLabel);
       bottomControls.setAlignment(Pos.CENTER_LEFT);
       bottomControls.setStyle("-fx-padding: 10;");

       rightPane = new VBox(10, routeInfoLabel);
       rightPane.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10;");
       rightPane.setPrefWidth(200);
       rightPane.setVisible(false);

       BorderPane rootPane = new BorderPane(mapHolder);
       rootPane.setTop(topControls);
       rootPane.setCenter(mapHolder);
       rootPane.setBottom(bottomControls);
       rootPane.setRight(rightPane);

       Scene scene = new Scene(rootPane, CANVAS_WIDTH, CANVAS_HEIGHT);
       stage.setScene(scene);
       stage.show();
       stage.setTitle("Noodle Maps");
    }

    /**
     * Initialises the event handling on pressing the buttons, the zooming, and all the other map functions.
     * Simply wires up all user driven interactions:
     * </br>
     * - "Search Route" button for displaying the road.
     * </br>
     * - "Clear" button resets inputs and hides the info panel
     * </br>
     * - mouse scroll on the canvas zooms in/out at pointer
     * </br>
     * - slider drags zoom around canvas center
     * <p>
     *     Does not modify layout; only attaches listeners.
     */
    private void initEventHandlers() {

        // Route searching button
        searchRouteButton.setOnAction(e -> {
            if (searchedAddress != null && destinationAddress != null) {
                controller.computeRouteBetween(searchedAddress, destinationAddress);
                rightPane.setVisible(true);
                rightPane.getChildren().clear();
                rightPane.getChildren().add(routeInfoLabel);

                double timeSec = controller.getRouteTime();
//
//                System.out.println(timeSec);
                VBox carBox = createTransportBox("CAR", Math.ceil(timeSec /60.0 * 1.5), "car.png");
                VBox bikeBox = createTransportBox("BIKE", Math.ceil(timeSec/60.0 * 3), "bike.png");
                rightPane.getChildren().addAll(carBox, bikeBox);

                safeRedraw();
            }
        });

        // Clear search button
        clearButton.setOnAction(e -> {
            searchedAddress = null;
            destinationAddress = null;
            pinLat = pinLon = destPinLat = destPinLon = Double.NaN;
            originDropdown.getItems().clear();
            destinationDropdown.getItems().clear();
            controller.currentRoute.clear();
            rightPane.setVisible(false);

            safeRedraw();
        });

        //ZOOM
        canvas.setOnScroll(e -> {
            boolean zoomIn = e.getDeltaY() > 0;
            double factor = zoomIn ? controller.baseZoomFactor : 1 / controller.baseZoomFactor;
            try {
                zoom(e.getX(), e.getY(), factor);
                slider.setValue(slider.getValue() + (zoomIn ? 0.1 : -0.1));
            } catch (NonInvertibleTransformException ex) {
                throw new RuntimeException(ex);
            }
        });

        //ZoomSlider (som i burgeren)
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            double factor = Math.pow(2, newValue.doubleValue() - oldValue.doubleValue());
            try {
                zoom(canvas.getWidth() / 2, canvas.getHeight() / 2, factor);
            } catch (NonInvertibleTransformException ex) {
                throw new RuntimeException(ex);
            }
        });
    }

    /**
     * Performs the initial map‐view setup:
     * </br>
     *  1. renders the map once in its default transform
     *  </br>
     *  2. pans the map so the data’s northwest corner aligns at the canvas origin
     *  </br>
     *  3. zooms the map so that its full latitude span fits the canvas height
     * </br>
     * This ensures the user sees the entire map on first launch.
     */
    private void init_Map_Binding_and_Initial_View() {
        safeRedraw();

        try {
            pan(-TRANSFORM_LATITUDE *model.minlon, model.maxlat, false);
            double scale = canvas.getHeight() / (model.maxlat - model.minlat);
            zoom(0, 0, scale);
        } catch (NonInvertibleTransformException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Error prevents redraw by catching and converting
     * NonInvertibleTransformException into a RuntimeException.
     * <p> Use this in event handlers to avoid checked‐exception clutter.
     */
    private void safeRedraw() {
        try { redraw(); }
        catch (NonInvertibleTransformException ex) { throw new RuntimeException(ex); }
    }

    /**
     * Binds the canvas width/height to its container (mapHolder)
     * and requests a redraw whenever its size changes.
     */
    private void bindCanvasSize() {
       canvas.widthProperty().bind(mapHolder.widthProperty());
       canvas.heightProperty().bind(mapHolder.heightProperty());

       canvas.widthProperty().addListener((o, ov, nv) -> safeRedraw());
       canvas.heightProperty().addListener((o, ov, nv) -> safeRedraw());
    }

    /**
     * Renders everything: clears canvas, draws map, pins, POIs, and routes.
     */
    void redraw() throws NonInvertibleTransformException {
        clearCanvas();
        renderBaseMap();
        renderPins();
        renderPOIs();
        renderRoutes();
    }

    /**
     * Clears the canvas for redraw to a white background,
     * resets the transform to current {@link #trans} and resets line based on current scale.
     * <p>
     *     Called at the start of all redraw so that it doesn't become a hellhole-daemonpit
     */
    private void clearCanvas() {
        gc.setTransform(new Affine());
        gc.setFill(Color.WHITE);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setTransform(trans);
        gc.setLineWidth(1/Math.sqrt(trans.determinant()));
    }

    /**
     * Renders the initial map:
     * </br> - performs a range search to draw 'dem polygons and highways within view.
     * </br> - Then proceed to draw relations on top.
     * </br> - Lastly it draws the OG pin if set
     *
     * @throws NonInvertibleTransformException
     */
    private void renderBaseMap() throws NonInvertibleTransformException {
        drawWithinRange(gc, trans);

        for (Relation relation : model.relations) {
            relation.drawRelation(gc, model);
        }

        //Draw PIN set by Address
        if (!Double.isNaN(pinLat) && !Double.isNaN(pinLon)) {
            drawPin(pinLat, pinLon);
        }
    }

    /**
     * Draws both the start and end destination pins on the canvas (well not the start anymore, currently),
     * if their latitude/longitude values are not NaN.
     */
    private void renderPins() {
        if (!Double.isNaN(pinLat) && !Double.isNaN(pinLon))     drawPin(pinLat,  pinLon);
        if (!Double.isNaN(destPinLat) && !Double.isNaN(destPinLon)) drawPin(destPinLat, destPinLon);
    }

    /**
     * If POIs are enabled, draws each point-of-interest as a small
     * pin-thingy on the map, using the current transform.
     */
    private void renderPOIs() {
        if(!hiddenPOIs){
            poiPin("show");
        }
    }

    /**
     * Draws the currently computed route as a red polyline.
     * Iterates through successive nodes, converts each Node’s
     * lon/lat to canvas X/Y, and strokes line segments.
     */
    private void renderRoutes() {
        if (!controller.hasRoute()) return;

        List<Node> route = controller.getRouteNodes();
        gc.setStroke(Color.RED);
        gc.setLineWidth(2 / Math.sqrt(trans.determinant()));

        System.out.println("Route:");

        for (int i = 0; i < route.size() - 1; i++) {
            Node a = route.get(i);
            Node b = route.get(i + 1);

            double x1 = modelToCanvasX(a.getLon());
            double y1 = modelToCanvasY(a.getLat());
            double x2 = modelToCanvasX(b.getLon());
            double y2 = modelToCanvasY(b.getLat());

            gc.strokeLine(x1, y1, x2, y2);
        }
    }

    /**
     * Converts a geographic longitude (degrees east) into a canvas X.
     *
     * @param lon  longitude in degrees
     * @return     the corresponding X coordinate on the canvas
     */
    private double modelToCanvasX(double lon) {
        return TRANSFORM_LATITUDE * lon;
    }

    /**
     * Converts a geographic latitude (degrees north) into a canvas Y.
     * Note: positive latitudes map to negative Y because screen Y grows downward.
     *
     * @param lat  latitude in degrees
     * @return     the corresponding Y coordinate on the canvas
     */
    private double modelToCanvasY(double lat) {
        return -lat;
    }

    private void updateDropdownSuggestions(String input, ComboBox<String> dropdown) {
        List<Address> matches = model.getMatchingAddresses(input);
        ObservableList<String> suggestions = FXCollections.observableArrayList();

        for (Address address : matches) {
            suggestions.add(address.getAddress());
        }
        dropdown.setItems(suggestions);
    }

    private VBox createTransportBox(String type, double timeMinutes, String iconFile) {
        VBox box = new VBox(8);
        box.setStyle("""
        -fx-border-color: #cccccc;
        -fx-border-radius: 8;
        -fx-background-color: #fdfdfd;
        -fx-background-radius: 8;
        -fx-padding: 10;
        -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 5, 0, 2, 2); """);

        ImageView icon = new ImageView(getClass().getResource("/icons/" + iconFile).toExternalForm());
        icon.setFitWidth(24);
        icon.setFitHeight(24);

        Label titleLabel = new Label(type);
        titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        Label timeLabel = new Label(timeMinutes + " min");
        timeLabel.setStyle("-fx-font-size: 12px;");

        HBox topRow = new HBox(10, icon, titleLabel, timeLabel);
        topRow.setAlignment(Pos.CENTER_LEFT);

        ComboBox<String> directionsDropdown = new ComboBox<>();
        directionsDropdown.setPromptText("Show Directions...");
        directionsDropdown.setVisible(false);

        Button toggleButton = new Button("Show " + type + " Directions");
        toggleButton.setOnAction(e -> directionsDropdown.setVisible(!directionsDropdown.isVisible()));

        directionsDropdown.getItems().addAll("Turn left...", "Keep right...", "Arrive at destination"); // placeholder

        box.getChildren().addAll(topRow, toggleButton, directionsDropdown);
        return box;
    }

    /**
     * Creates and returns an editable ComboBox that:
     * - suggests matching addresses as you type
     * - Calls upown the given Consumer<Address> when an item is selected
     *
     * @param onAddressSelected code to run when user picks an address
     * @return a fully configured address-search ComboBox
     */
    private ComboBox<String> prepareComboBox(Consumer<Address> onAddressSelected) {
        ComboBox<String> addressDropdown = new ComboBox<>();
        addressDropdown.setPromptText("Search address: ");
        addressDropdown.setEditable(true);
        addressDropdown.setMaxWidth(250);

        addressDropdown.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue.length() > 1) {
                updateDropdownSuggestions(newValue, addressDropdown);
                addressDropdown.show();
            } else {
                addressDropdown.hide();
            }
        });

        addressDropdown.setOnAction(e -> {
            String selected = addressDropdown.getSelectionModel().getSelectedItem();
            if (selected != null && !model.getMatchingAddresses(selected).isEmpty()) {
                Address selectedAddr = model.getMatchingAddresses(selected).get(0);
                onAddressSelected.accept(selectedAddr);
                try {
                    redraw();
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
                try {
                    tryRoute(); // attempt to route if both set
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        return addressDropdown;
    }
    //---???----------------------------------------
    private void tryRoute() throws NonInvertibleTransformException {
        if (searchedAddress != null && destinationAddress != null) {
            System.out.println("Calling controller.computeRouteBetween");
            controller.computeRouteBetween(searchedAddress, destinationAddress);

            try {
                centerMapAndZoom(pinLat, pinLon, 5.0);
                //centerMap(pinLat, pinLon);
            } catch (NonInvertibleTransformException ex) {
                ex.printStackTrace();
            }

        } else {
            System.out.println(" !!!!-------!!!!_---One or both addresses are null!!!____------");
        }
    }

    private Slider prepareSlider() {
        slider = new Slider(-5, 5, 0); // min zoom, max zoom, initial zoom
        slider.setShowTickMarks(true);
        slider.setMajorTickUnit(0.5);
        slider.setBlockIncrement(0.1);
        slider.setPrefWidth(200);

        // Bind slider zoom to map zoom logic
        slider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double factor = Math.pow(2, newVal.doubleValue() - oldVal.doubleValue());
            try {
                zoom(canvas.getWidth() / 2, canvas.getHeight() / 2, factor);
            } catch (NonInvertibleTransformException e) {
                throw new RuntimeException(e);
            }
        });

        return slider;
    }

    private Button preparePOIButton() {
        Button poiButton = new Button("Show POIs");
        poiButton.setStyle("-fx-padding: 10;");
        poiButton.setOnAction(e ->{
            if(hiddenPOIs){ //Shows POIs if they are hidden when the button is pressed
                List<POI> pois = controller.getPOIController().getAllPOIs();
                System.out.println("Number of POIs: " + pois.size());

                for(int i = 0; i < Math.min(3, pois.size()); i++){
                    POI poi = pois.get(i);
                    double x = TRANSFORM_LATITUDE * poi.getLon();
                    double y = -poi.getLat();
                    System.out.println("POI " + (i + 1) + ": " + poi.getLat() + ", " + poi.getLon() + "-> Transformed: " + x + ", " + y);
                }
                try {
                    redraw();
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
                poiPin("show");
                hiddenPOIs = false;
                poiButton.setText("Hide POIs");

            } else { //Hides POIs if they are shown when the button is pressed
                hiddenPOIs = true;
                try {
                    redraw();
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
                poiButton.setText("Show POIs");
            }
        });
        return poiButton;
    }

    private Button prepareSearchButton(ComboBox<String> addressDropdown) {
        Button searchButton = new Button("Search");

        searchButton.setOnAction(e -> {
            String address = addressDropdown.getEditor().getText();
            if (!address.isEmpty()) {
                try {
                    searchAndCenterMap(address);
                } catch (NonInvertibleTransformException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        return searchButton;
    }

    /**
     * Draws a pin image at the given model coordinates.
     * Applies the current transform to map model→screen, and
     * scales the image so it remains visible at all zoom levels.
     *
     * @param lat  latitude of the pin
     * @param lon  longitude of the pin
     */
    void drawPin(double lat, double lon) {
        double transformedX = TRANSFORM_LATITUDE * lon;
        double transformedY = -lat;

        InputStream inputStream = getClass().getResourceAsStream("/icons/pin.png");
        assert inputStream != null;
        Image pinImage = new Image(inputStream);

        Double scaledSize = 10e-4;

        double zoomFactor = Math.sqrt(trans.determinant());
        double imageSize = 20.0 / zoomFactor;
        gc.drawImage(pinImage, transformedX - imageSize / 2, transformedY - imageSize / 2, imageSize, imageSize);

        //gc.drawImage(pinImage, transformedX - (scaledSize/2), transformedY - scaledSize, scaledSize, scaledSize);
    }

    void pan(double dx, double dy, boolean shouldRedraw) throws NonInvertibleTransformException {
        trans.prependTranslation(dx, dy);
        if(shouldRedraw){
            redraw();
        }
    }

    void zoom(double dx, double dy, double factor) throws NonInvertibleTransformException {
        pan(-dx, -dy, false);
        trans.prependScale(factor, factor);
        pan(dx, dy, true);

        controller.setZoomLevel(Math.sqrt(trans.determinant()));
    }

    public Point2D mousetoModel(double lastX, double lastY) {
        try {
            return trans.inverseTransform(lastX, lastY);
        } catch (NonInvertibleTransformException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    public Point2D modelToScreen(double modelX, double modelY) {
        return trans.transform(modelX, modelY);
    }

    public void searchAndCenterMap(String address) throws NonInvertibleTransformException {
        for (Address addr : model.getAddresses()) {
            if (addr.getAddress().equalsIgnoreCase(address)) {
                searchedAddress = addr;
                pinLat = addr.getLat();
                pinLon = addr.getLon();

                redraw();
                return;
            }
        }
        System.out.println("Address not found: " + address);
    }

    void poiPin(String status) {
        if (status.equals("show")) {
            POIController controllerPOIs = controller.getPOIController();
            List<POI> pois = controllerPOIs.getAllPOIs();

            if (pois.isEmpty()) {
                System.out.println("No POIs to display!");
                return;
            }

            // Set up for drawing the pins
            gc.setTransform(trans);
            gc.setFill(Color.RED);
            gc.setStroke(Color.BLACK);

            // Make pins more visible
            double pinSize = 10 / Math.sqrt(trans.determinant());

            for (POI poi : pois) {
                double x = TRANSFORM_LATITUDE * poi.getLon();
                double y = -poi.getLat();

                // Draw a more visible pin
                gc.fillOval(x - pinSize/2, y - pinSize/2, pinSize, pinSize);
                gc.strokeOval(x - pinSize/2, y - pinSize/2, pinSize, pinSize);
            }
        }
    }

    public void addPOI(double lat, double lon){
        POI poi = new POI(lat, lon);
        controller.getPOIController().addPOI(poi);
    }

    private void drawFilledMultipolygon(GraphicsContext gc, MultiPolygon mp) {
        List<List<Node>> outerRings = mp.getOrderedOuterRings();
        List<List<Node>> innerRings = mp.getOrderedInnerRings();

        Color fillColor = determineColorFromTags(mp.getTags());
        Color backgroundColor = Color.WHITE; // Same as canvas background

        // --- Draw outer filled polygons ---
        for (List<Node> ring : outerRings) {
            double[] xPoints = new double[ring.size()];
            double[] yPoints = new double[ring.size()];
            for (int i = 0; i < ring.size(); i++) {
                xPoints[i] = TRANSFORM_LATITUDE * ring.get(i).getLon();
                yPoints[i] = -ring.get(i).getLat();
            }
            gc.setFill(fillColor);
            gc.fillPolygon(xPoints, yPoints, ring.size());
            //gc.setStroke(Color.DARKGRAY);
            //gc.strokePolygon(xPoints, yPoints, ring.size());
        }

        // --- Draw inner holes using background color ---
        for (List<Node> ring : innerRings) {
            double[] xPoints = new double[ring.size()];
            double[] yPoints = new double[ring.size()];
            for (int i = 0; i < ring.size(); i++) {
                xPoints[i] = TRANSFORM_LATITUDE * ring.get(i).getLon();
                yPoints[i] = -ring.get(i).getLat();
            }
            gc.setFill(backgroundColor);
            gc.fillPolygon(xPoints, yPoints, ring.size());
            //gc.setStroke(Color.DARKGRAY);
            //gc.strokePolygon(xPoints, yPoints, ring.size());
        }
    }

    private Color determineColorFromTags(Map<String, String> tags) {
        if (tags == null) return Color.LIGHTGRAY;

        String natural = tags.get("natural");
        String building = tags.get("building");
        String landuse = tags.get("landuse");

        if ("water".equals(natural)) return Color.LIGHTBLUE;
        if ("wood".equals(natural) || "forest".equals(landuse)) return Color.LIGHTGREEN;
        if (building != null) return Color.LIGHTGRAY;
        return Color.BEIGE;
    }

    private ComboBox<String> prepareColorModes (){
        colorModes = new ComboBox<>();
        colorModes.setMaxWidth(100);
        colorModes.getItems().addAll(
                "Default",
                "Protanopia",
                "Deuteranopia",
                "Tritanopia"
        );
        colorModes.setValue("Default");
        colorModes.setEditable(false);

        colorModes.setOnAction(e ->{
            String selectedMode = colorModes.getValue();
            HighwayType.updateColorsForMode(selectedMode);
            NaturalType.updateColorsForMode(selectedMode);
            Landuse.LanduseType.updateColorsForMode(selectedMode);
            try {
                redraw();
            } catch (NonInvertibleTransformException ex) {
                throw new RuntimeException(ex);
            }
        });
        return colorModes;
    }

    /**
     * Draws all visible OSM elements (ways, multipolygons, buildings)
     * within the current viewport, yee, using the provided GraphicsContext
     * and transform.
     *
     * @param gc    the graphics context to draw on
     * @param currentTrans the current affine transform mapping model to screen
     * @throws NonInvertibleTransformException if trans.inverseTransform() fails
     * <p>
     *     This keeps the R-Tree satisfied and makes our dynamic rendering possible
     */
    public void drawWithinRange(GraphicsContext gc, Affine currentTrans) throws NonInvertibleTransformException {
        Point2D topLeft = currentTrans.inverseTransform(50, 50);
        Point2D bottomRight = currentTrans.inverseTransform(canvas.getWidth()-100, canvas.getHeight()-100);

        topLeft = currentTrans.inverseTransform(0,0);
        bottomRight = currentTrans.inverseTransform(canvas.getWidth(),canvas.getHeight());

        /*
        // FOR DRAWING BOX - USE FIRST POINTS !!!
        gc.setFill(Color.BLACK);
        gc.strokeRect(topLeft.getX(), topLeft.getY(), bottomRight.getX()-topLeft.getX(), bottomRight.getY()-topLeft.getY());
         */

        BoundingBox box = new BoundingBox(topLeft.getY(), topLeft.getX(), bottomRight.getY(), bottomRight.getX());

        List<OSMElement> foundOthers = model.otherRTree.rangeSearch(box);
        List<OSMElement> found = model.highwayRTree.rangeSearch(box);

        for (MultiPolygon mp : model.getMultipolygons()) {
            drawFilledMultipolygon(gc, mp);
        }

        for(OSMElement element : foundOthers) {
            element.drawEle(gc);
        }

        for(OSMElement element : found) {
            element.drawEle(gc);
        }

        double zoomLevel = controller.getZoomLevel();

        if(zoomLevel >  35000.0 ) { // 25.0)
            List<OSMElement> foundBuildings = model.buildingRTree.rangeSearch(box);
            for(OSMElement element : foundBuildings) {
                element.drawEle(gc);
            }
        }
    }

    public void setHighlightedElement(OSMElement element) {
        this.highlightedElement = element;
    }

    public void centerMap(double lat, double lon) throws NonInvertibleTransformException {
        double targetX = TRANSFORM_LATITUDE * lon;
        double targetY = -lat;

        Point2D screenPoint = modelToScreen(targetX, targetY);
        double dx = canvas.getWidth() / 2 - screenPoint.getX();
        double dy = canvas.getHeight() / 2 - screenPoint.getY();
        pan(dx, dy, true);
    }

    public void centerMapAndZoom(double lat, double lon, double zoomFactor) throws NonInvertibleTransformException {
        double targetX = TRANSFORM_LATITUDE * lon;
        double targetY = -lat;

        // Step 1: get screen coords before zoom
        Point2D screenPointBeforeZoom = modelToScreen(targetX, targetY);

        // Step 2: zoom in on the current point
        zoom(screenPointBeforeZoom.getX(), screenPointBeforeZoom.getY(), zoomFactor);

        // Step 3: recalculate screen point after zoom
        Point2D screenPointAfterZoom = modelToScreen(targetX, targetY);

        // Step 4: now pan to center the point
        double dx = canvas.getWidth() / 2 - screenPointAfterZoom.getX();
        double dy = canvas.getHeight() / 2 - screenPointAfterZoom.getY();
        pan(dx, dy, true);
    }


    public void centerMapAndZoom2(double lat, double lon, double zoomFactor) throws NonInvertibleTransformException {
        double targetX = TRANSFORM_LATITUDE * lon;
        double targetY = -lat;

        Point2D screenPoint = modelToScreen(targetX, targetY);

        double dx = canvas.getWidth() / 2 - screenPoint.getX();
        double dy = canvas.getHeight() / 2 - screenPoint.getY();

        pan(dx, dy, true); // Center the point

        // Now zoom in centered on the point
        zoom(screenPoint.getX(), screenPoint.getY(), zoomFactor);
    }
}
