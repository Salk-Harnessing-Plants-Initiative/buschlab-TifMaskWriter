package at.ac.oeaw.gmi.brat.coordfix;

import at.ac.oeaw.gmi.brat.segmentation.output.PlateCoordinates;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WriteMasker {

    // QC file to drop wrong segmented pix
    private static HashMap<String, ArrayList<Integer>> qcMap =
            readOcFile("/home/GMI/alexander.bindeus/Desktop/Baohai_set1/processed/brat-qc-results.txt");

    public static void main(String[] args) {

        double maxRot = 0.0;
        String maxRotImage = null;

        // path to tif images
        String inDir = "/home/GMI/alexander.bindeus/Desktop/Baohai_set1/processed";

        try (DirectoryStream<Path> myFiles = Files.newDirectoryStream(Paths.get(inDir), "*.{ser}")) {
            for (Path entry : myFiles) {
                Brat_V2_Coords bcoo = new Brat_V2_Coords();
                bcoo.read_serialized_Coords(entry.toString());
                // fixed tif sizes due to scanner props
                writeMask(entry.toString(),6614, 6608, bcoo);

                if (Math.abs(bcoo.getPlateCoordinates().rotation) > maxRot) {
                    maxRot = bcoo.getPlateCoordinates().rotation;
                    maxRotImage = entry.toString();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.out.println("maximum Rotation: " + maxRot + " in Image " + maxRotImage);
    }

    private static HashMap<String, ArrayList<Integer>> readOcFile(String inQcFile) {

        HashMap<String, ArrayList<Integer>> qcMap = new HashMap<>();
        String line, cooFileName, aux;
        Integer plantId;
        Pattern pattern = Pattern.compile("_[0-9]{2}_");
        Matcher match;

        // collecting plantIDs per coordinate file which passed QC
        try (BufferedReader br = new BufferedReader(new FileReader(inQcFile))) {
            while ((line = br.readLine()) != null) {
                if (Character.isDigit(line.charAt(line.length()-1))) {
                    match = pattern.matcher(line);
                    if (match.find()) {
                        aux = line.replaceFirst("Plant_\\d+_", "");
                        cooFileName = aux.substring(0, aux.indexOf("."));

                        if (!qcMap.containsKey(cooFileName))
                            qcMap.put(cooFileName, new ArrayList<Integer>());

                        plantId = Integer.parseInt(match.group().replaceAll("_", ""));
                        qcMap.get(cooFileName).add(plantId);
                    }
                }
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        return qcMap;
    }

    private static void writeMask(String inCooFile, int height, int width, Brat_V2_Coords inCoo) {

        PlateCoordinates pc = inCoo.getPlateCoordinates();
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        File f = null;

        double sin = Math.sin(-pc.rotation);
        double cos = Math.cos(-pc.rotation);

        int xOff = (int)pc.refPt.getX();
        int yOff = (int)pc.refPt.getY();

        int rx, ry;

        // pixel values for root, shoot, main path (3 for overlap)
        byte[] myCol3 = {2, 1, 4};
        // root, shoot, main path pixels
        ArrayList<Integer> drawOrder = new ArrayList<>(Arrays.asList(1,0,2));

        WritableRaster raster = img.getRaster();
        byte[]pixVals = new byte[width *height];
        byte[][]tempMatrix = new byte [width][height];

        String key = inCooFile.substring(inCooFile.lastIndexOf('/')+1, inCooFile.lastIndexOf('.'));
        key = key.replace("Coordinates","Diagnostics");
        for(Map.Entry<String, List<List<Point>>> entry : pc.plantCoordinates.entrySet()) {
            try {
                if (qcMap.get(key).contains(Integer.parseInt(entry.getKey()))) {
                    for (Integer i : drawOrder) {
                        if (entry.getValue().get(i) != null) {    // for safety check in shoot and root
                            for (Point onePoint : entry.getValue().get(i)) {

                                // old version
//                                rx = (int) (onePoint.x * cos - onePoint.y * sin + 0.5) + xOff;
//                                // spiegelung
//                                rx = width - rx -1;
//                                ry = (int) (onePoint.x * sin + onePoint.y * cos + 0.5) + yOff;
//
//                                rx = width - (onePoint.x + xOff);
//                                ry = onePoint.y + yOff;
//                                if (i == 0) { //for root pixel !!!
//                                    if (pixVals[ry * width + rx] == 1) {
//                                        pixVals[ry * width + rx] = 3;
//                                        continue;
//                                    }
//                                }
//                                pixVals[ry * width + rx] = myCol3[i];


                                rx = onePoint.x;
                                ry = onePoint.y;

                                if (i == 0) { //for root pixel !!!
                                    if (tempMatrix[rx][ry] == 1) {
                                        tempMatrix[rx][ry] = 3;
                                        continue;
                                    }
                                }
                                tempMatrix[rx][ry] = myCol3[i];
                            }
                        }
                    }
                } else {
                    System.out.println(inCooFile + " plant " + entry.getKey() + " not segmented or skipped due to QC");
                }
            }
            catch (Exception e) {
                System.out.println(entry.getKey() + " not found in QC map for file " + inCooFile);
                e.printStackTrace();
            }

            int x,y,tx,ty;

            try {
                for (x = 0; x < width - xOff; ++x) {
                    for (y = 0; y < height - yOff; ++y) {
                        rx = (int) (x * cos - y * sin + 0.5);
                        ry = (int) (x * sin + y * cos + 0.5);

                        if (ry <0 || rx < 0)
                            continue;

                        if (ry >= height || rx >= width)
                            continue;

                        tx = width - (x + xOff) -1; // -1 due to pixel indexing starting at 0
                        ty = y + yOff;
                        pixVals[ty * width + tx] = tempMatrix[rx][ry];
                    }
                }
            }
            catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
            }
        }

        raster.setDataElements(0,0,width,height,pixVals);
        String outputFile = inCooFile.replace(".ser", "_bitmask.tiff");
        outputFile = outputFile.replace("processed", "bitmasks");

        try{
            f = new File(outputFile);
            ImageIO.write(img, "TIFF", f);
            System.out.println("Wrote output file " + outputFile);
        } catch(IOException e){
            System.out.println("Error: " + e);
        }
    }
}
