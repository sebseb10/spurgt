package com.example.osmparsing;

import com.example.osmparsing.MVC.Model;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.xml.stream.XMLStreamException;
import java.io.FileNotFoundException;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    private static Model testModel;


    //Initializing JavaFX Application Thread
    @BeforeAll
    static void initJavaFX() {
        Platform.startup(() -> {});
    }

    @BeforeEach
    void setUp() {
        try {
            testModel = Model.load("data/Levsø.osm");
        } catch (FileNotFoundException e) {
            fail("Test OSM file not found: " + e.getMessage());
        } catch (IOException | XMLStreamException | ClassNotFoundException e) {
            fail("Error loading test OSM file: " + e.getMessage());
        }
    }

    @Test
    void testModel() {
        assertNotNull(testModel); //TEST IF FILE IS LOADED
        assertFalse(testModel.getAddresses().isEmpty()); //TEST IF ADDRESSES ARE LOADED
        assertFalse(testModel.ways.isEmpty()); //TEST IF WAYS ARE ADDED

    }
}