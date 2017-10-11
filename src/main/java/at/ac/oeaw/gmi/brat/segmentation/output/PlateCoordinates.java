package at.ac.oeaw.gmi.brat.segmentation.output;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.*;
import java.util.List;

public class PlateCoordinates implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = -2826357954582508727L;
    public double rotation;
    public double scalefactor;
    public Point2D refPt;
    public Shape plateShape;

    public Map<String, List<List<Point>>> plantCoordinates=new HashMap<String, List<List<Point>>>();
}
