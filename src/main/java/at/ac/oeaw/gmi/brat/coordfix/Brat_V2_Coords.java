package at.ac.oeaw.gmi.brat.coordfix;


import at.ac.oeaw.gmi.brat.segmentation.output.PlateCoordinates;

import java.awt.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import java.io.IOException;

public class Brat_V2_Coords {
    private PlateCoordinates pc;

    public PlateCoordinates getPlateCoordinates() {
        return pc;
    }

    public void read_serialized_Coords(String coordPath) {
        System.out.print(String.format("Reading serialized Coordinates from \'%s\' ... ", coordPath));
        System.out.flush();

        ObjectInput objInput = null;
        this.pc = null;
        try{
            objInput = new ObjectInputStream(new BufferedInputStream(new FileInputStream(coordPath)));
            long start = System.nanoTime();
            pc = (PlateCoordinates) objInput.readObject();
            System.out.println(String.format("%.6fs\n", (System.nanoTime() - start)/1.0e9));
        } catch (NullPointerException | ClassNotFoundException | IOException e) {
            System.out.println("failed.\n");
            e.printStackTrace();
        }

        if (objInput != null){
            try {
                objInput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void printStats() {
        if (pc == null) {
            return;
        }

        System.out.println("Rotation:\t" + pc.rotation);
        System.out.println("Reference Point:\t" + pc.refPt);
        System.out.println("Scale factor:\t" + pc.scalefactor);
        System.out.println("Plate shape: \t" + pc.plateShape);
        System.out.println("Total number of plants:\t" + pc.plantCoordinates.size());
        System.out.println();

        for(Map.Entry<String, List<List<Point>>> entry : pc.plantCoordinates.entrySet()) {
            System.out.println(String.format("Plant %s:", entry.getKey()));
            System.out.println(String.format("\t%8d shoot pixels", entry.getValue().get(0) != null ? entry.getValue().get(0).size() : 0));
            System.out.println(String.format("\t%8d root pixels", entry.getValue().get(1) != null ? entry.getValue().get(1).size() : 0));
            System.out.println(String.format("\t%8d main path pixels", entry.getValue().get(2) != null ? entry.getValue().get(2).size() : 0));
       }
    }
}


