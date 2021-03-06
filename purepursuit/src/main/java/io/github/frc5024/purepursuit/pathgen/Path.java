package io.github.frc5024.purepursuit.pathgen;

import java.util.ArrayList;
import java.util.Arrays;

import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.util.Units;

import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;

/**
 * A "Path" is a list of closely spaces points in space for a robot to follow.
 * The default Path class will generate paths with any number of waypoints, and
 * create linear sub-paths.
 */
public class Path {

    // Path points
    protected ArrayList<Translation2d> points;
    protected Translation2d[] waypoints;

    /**
     * Create a motion path from points
     * 
     * @param waypoints Path waypoints to follow
     */
    public Path(Translation2d... waypoints) {
        this(Units.inchesToMeters(6.0), waypoints);
    }

    /**
     * Create a motion path from points
     * 
     * @param spacing   Amount of space between "inner" points in meters
     * @param waypoints Path waypoints to follow
     */
    public Path(double spacing, Translation2d... waypoints) {
        this.points = new ArrayList<>();
        this.waypoints = waypoints;

        // Fill in extra points
        int i = 0;
        for (; i < waypoints.length - 1; i++) {

            // Get the start and end translations
            Translation2d startTrans = waypoints[i];
            Translation2d endTrans = waypoints[i + 1];

            // Find the distance to the next point
            Translation2d displacement = new Translation2d(endTrans.getX() - startTrans.getX(),
                    endTrans.getY() - startTrans.getY());

            // Find the normal
            double theta = Math.atan2(displacement.getY(), displacement.getX());
            Translation2d normal = new Translation2d(Math.cos(theta), Math.sin(theta));

            // Determine the number of points we can fit between the start and end
            double innerCount = Math.ceil(Math.hypot(displacement.getX(), displacement.getY()) / spacing);

            // For each point, multiply it by the normal to get an inner point
            for (int j = 0; j < innerCount; j++) {

                // Determine the magnitude for this point
                double magnitude = spacing * j;

                // Create a new vector for this magnitude
                Translation2d innerPoint = new Translation2d(normal.getX() * magnitude, normal.getY() * magnitude);

                // Add the point to it's "base" point
                innerPoint = innerPoint.plus(startTrans);

                // Add this vector to the list
                this.points.add(innerPoint);
            }
        }

        if (waypoints.length != 0) {
            // Add the last point to the points list
            this.points.add(waypoints[i]);

            // Remove the first point, as it may cause bugs with followers
            this.points.remove(0);
        }
    }

    /**
     * Get a list of all poses along the path
     * 
     * @return Poses
     */
    public Translation2d[] getPoses() {
        return this.points.toArray(new Translation2d[this.points.size()]);
    }

    @Override
    public String toString() {
        return String.format("<Path: %s>", Arrays.deepToString(getPoses()));
    }

    /**
     * Get a chart showing every generated path point in 2D space. Can be written to
     * disk for debugging and demos.
     * 
     * @return Path visualization
     */
    public XYChart getPathVisualization() {

        // Create X and Y datasets for points
        double[] xData = new double[this.points.size()];
        double[] yData = new double[this.points.size()];

        // Save each point as an X and a Y
        int i = 0;
        for (Translation2d point : this.points) {
            xData[i] = point.getX();
            yData[i] = point.getY();
            i++;
        }

        // Create X and Y datasets for waypoints
        double[] wxData = new double[this.waypoints.length];
        double[] wyData = new double[this.waypoints.length];

        // Save each point as an X and a Y
        i = 0;
        for (Translation2d point : this.waypoints) {
            wxData[i] = point.getX();
            wyData[i] = point.getY();
            i++;
        }

        // Build as a chart
        // XYChart chart = QuickChart.getChart("Generated Path", "X (meters)", "Y
        // (meters)", "path", xData, yData);
        XYChart chart = new XYChartBuilder().width(1000).height(600).build();

        // Add data
        chart.addSeries("Generated Path", xData, yData);
        chart.addSeries("Waypoints", wxData, wyData);

        // Configure chart styling
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
        chart.getStyler().setLegendPosition(LegendPosition.OutsideE);
        chart.getStyler().setMarkerSize(8);

        return chart;
    }

}