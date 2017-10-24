package at.ac.oeaw.gmi.brat.coordfix;

import static org.junit.Assert.*;

public class Brat_V2_CoordsTest {
    @org.junit.Test
    public void read_serialized_Coords() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        //String testCoordPath = cl.getResource("Object_Coordinates_LP_set1_day13_20170727_002.ser").getPath();
        String testCoordPath = "/home/GMI/alexander.bindeus/Desktop/brat_test/processed_tif/processed/Object_Coordinates_heavymetal_set1_day1_20160328_001.ser";
        Brat_V2_Coords serCoords = new Brat_V2_Coords();
        serCoords.read_serialized_Coords(testCoordPath);
        assertNotNull("Plate coordinates must not be null for correct path.", serCoords.getPlateCoordinates());

        String nullCoordPath = null;
        serCoords.read_serialized_Coords(nullCoordPath);
        assertNull("Plate coordinates must be null for NULL path.", serCoords.getPlateCoordinates());

        String wrongCoordPath = "/wrong/path/to/serialized/coords";
        serCoords.read_serialized_Coords(wrongCoordPath);
        assertNull("Plate coordinates must be null for wrong path.", serCoords.getPlateCoordinates());
   }

    @org.junit.Test
    public void printStats() throws Exception {
        ClassLoader cl = getClass().getClassLoader();
        //String testCoordPath = cl.getResource("Object_Coordinates_LP_set1_day13_20170727_002.ser").getPath();
        String testCoordPath = "/home/GMI/alexander.bindeus/Desktop/brat_test/processed_tif/processed/Object_Coordinates_heavymetal_set1_day1_20160328_001.ser";
        Brat_V2_Coords serCoords = new Brat_V2_Coords();
        serCoords.read_serialized_Coords(testCoordPath);
        Brat_V2_Coords testcoords = new Brat_V2_Coords();
        serCoords.printStats();
    }
}