package uk.ac.ed.inf.heatmap;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.mapbox.geojson.*;


public class App 
{
	// We take in one argument, the filepath to the prediction list
    public static void main( String[] args )
    {
    	// Define values for south-west corner of map, as well as the size of each rectangle
    	final double initLng = -3.192473;
    	final double initLat = 55.942617;
    	final double lngIncrement = 0.0008154;
    	final double latIncrement = 0.0003606;
    	
    	// Early exit if not passed an argument
        if(args.length < 1) {
            System.out.println("Error: No filename provided.");
            System.exit(0);
        }
        
        int[][] predictions = readPredictionsFromFile(args[0]);
    	
    	List<Feature> featureList = createFeatureList(initLng, initLat, lngIncrement, latIncrement, predictions);
    	
        // Once we have a list of geo-json features we can create a collection and turn it into JSON for output
        FeatureCollection featureColl = FeatureCollection.fromFeatures(featureList);
        String featureJSON = featureColl.toJson();
    	System.out.println(featureJSON);
    }

    // Populate 2D array with the values given in the input file
    // We use the convention that each value is separated by a comma
	private static int[][] readPredictionsFromFile(String filepath) {
		Scanner reader;
        int[][] predictions = new int[10][10];
        
		try {
			reader = new Scanner(new File(filepath));
	        for(int line = 0; line < 10; line++) {
	            String[] predictionStrings = (reader.nextLine()).split(","); // Split current line into 10 strings, then advance
	            for(int i = 0; i < predictionStrings.length; i++) {
	            	predictions[line][i] = Integer.parseInt(predictionStrings[i].strip()); // Strip whitespace then convert to an integer
	            }
	        }
		} catch (FileNotFoundException e) { // If the given path does not lead to a file, throw an error
            System.out.println("Error: Invalid filename provided.");
            System.exit(0);
		}
		return predictions;
	}

	// Create the geo-json feature list, assigning the correct properties to each rectangle
	// We iterate through the list from the southwest corner, incrementing longitude then latitude
	private static List<Feature> createFeatureList(final double initLng, final double initLat, final double lngIncrement, final double latIncrement, int[][] predictions) {
		double lng;
		double lat;
		List<Feature> featureList = new ArrayList<Feature>();
    	
        for(int j = 0; j < 10; j++) {
        	lat = initLat + (j * latIncrement);
            for(int i = 0; i < 10; i++) {
            	lng = initLng + (i * lngIncrement);
            	List<Point> coords = new ArrayList<Point>();
            	coords.add(Point.fromLngLat(lng, lat)); // The coords list is sensitive to the order of elements, they must be added in this order
            	coords.add(Point.fromLngLat(lng+lngIncrement, lat));
            	coords.add(Point.fromLngLat(lng+lngIncrement, lat+latIncrement));
            	coords.add(Point.fromLngLat(lng, lat+latIncrement));
            	coords.add(Point.fromLngLat(lng, lat)); // Returning to the start is necessary to generate the polygon
            	Polygon polygon = Polygon.fromLngLats(List.of(coords));
            	Feature feature = Feature.fromGeometry((Geometry) polygon);
            	feature.addNumberProperty("fill-opacity", 0.75);
            	String rgb = getRGBFromPredictions(predictions[9-j][i]);
            	feature.addStringProperty("rgb-string", rgb);
            	feature.addStringProperty("fill", rgb);
            	featureList.add(feature);
            }
        }
		return featureList;
	}
	
	// Assign the correct RGB colour using the prediction
	private static String getRGBFromPredictions(int prediction) {
		if(inRange(prediction, 0, 32)) {
			return "#00ff00";
		} else if(inRange(prediction, 32, 64)) {
			return "#40ff00";
		} else if(inRange(prediction, 64, 96)) {
			return "#80ff00";
		} else if(inRange(prediction, 96, 128)) {
			return "#c0ff00";
		} else if(inRange(prediction, 128, 160)) {
			return "#ffc000";
		} else if(inRange(prediction, 160, 192)) {
			return "#ff8000";
		} else if(inRange(prediction, 192, 224)) {
			return "#ff4000";
		} else if(inRange(prediction, 224, 256)) {
			return "#ff0000";
		} else { // Throw an error if the input file contained a value outside the 0 - 256 range
			System.out.println("Error: Prediction not in range.");
			System.exit(0);
		}
		return ""; // This should never be reached, but the method is required to have a return
	}
	
	// Helper function that stands in for min <= x < max
	private static boolean inRange(int val, int min, int max)
	{
	  return((val >= min) && (val < max));
	}
}
