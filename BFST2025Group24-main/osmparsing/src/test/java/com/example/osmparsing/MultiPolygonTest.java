package com.example.osmparsing;
import com.example.osmparsing.MVC.Model;
import com.example.osmparsing.relations.MultiPolygon;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class MultiPolygonTest {
    private static Model testModel;

    @BeforeAll
    static void initJavaFX() {
        Platform.startup(() -> {
        });
    }

    @BeforeEach
    void setUp() {
        try {
            //testModel.load("C:\\Users\\larsb\\Desktop\\BFST2025Group24\\osmparsing\\data\\VBNormal.osm");
            testModel = Model.load("data/VBNormal.osm");
        } catch (FileNotFoundException e) {
            fail("Test OSM file not found: " + e.getMessage());
        } catch (IOException | XMLStreamException | ClassNotFoundException e) {
            fail("Error loading test OSM file: " + e.getMessage());
        }
    }

    @Test
    void testParsedMultiPolygon() {
        assertNotNull(testModel, "Test model is null");
        assertNotNull(testModel.getRelations(), "Relations is null, therefore poo");
        assertNotNull(testModel.getMultipolygons(), "In terms of multipolygons, we have no multipolygoons");
        for (MultiPolygon mp : testModel.getMultipolygons()) {
            System.out.println("Found a multipolygon: " + mp.toString());
        }
    }

    @Test
    void testLinePathOuter() {
        System.out.println("Test1");
        assertFalse(testModel.getMultipolygons().isEmpty());
        System.out.println("Multipolygon is parsed and added to model. \n" + "Multipolygons size: " + testModel.getMultipolygons().size());
        for (MultiPolygon mp : testModel.getMultipolygons()) {
            assertFalse(mp.getOuterWays().isEmpty(), "Outer ways should not be null");
            System.out.println(mp.getOuterWays());
        }
        System.out.println("Test3");
    }

    @Test
    void testLinePathInner() {
        System.out.println("Test Start");
        assertFalse(testModel.getMultipolygons().isEmpty());
        System.out.println("Multipolygon is parsed and added to model. \n" + testModel.getMultipolygons());
        for (MultiPolygon mp : testModel.getMultipolygons()) {
            assertFalse(mp.getInnerWays().isEmpty(), "Inner ways should not be null");
            System.out.println(mp.getInnerWays());
        }
    }

    @Test
    void testWayOrdering() {
        System.out.println("Test Start");
        try {
            testModel = Model.load("data/bornholm.osm.zip");
        } catch (FileNotFoundException e) {
            fail("Test OSM file not found: " + e.getMessage());
        } catch (IOException | XMLStreamException | ClassNotFoundException e) {
            fail("Error loading test OSM file: " + e.getMessage());
        }
        for (MultiPolygon mp : testModel.getMultipolygons()) {
            if (!mp.getOuterWays().isEmpty()) {
                if (mp.getOrderedOuterRings().isEmpty()) {
                    System.err.println("WARNING: This MULTIPOLYGON has outer ways but no closed outer rings. Possibly incomplete data.");
                } else {
                    for (List<Node> ring : mp.getOrderedOuterRings()) {
                        assertEquals(ring.get(0), ring.get(ring.size() - 1),
                                "Outer ring is not fully closed, but ended up in orderedOuterRings.");
                    }
                }
            }
            if (!mp.getInnerWays().isEmpty()) {
                if (mp.getOrderedInnerRings().isEmpty()) {
                    System.err.println("WARNING: This MULTIPOLYGON has inner ways but no closed inner ring. Possibly partial data. " + "Tags: " + mp.getTags());
                } else {
                    for (List<Node> ring : mp.getOrderedInnerRings()) {
                        assertEquals(ring.get(0), ring.get(ring.size() - 1), "Inner ring is not fully closed, but ended up in orderedInnerRings.");
                    }
                }
            }
        }
    }
}