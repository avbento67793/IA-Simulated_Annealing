package distanceMatrix;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/* A distance matrix with an ArrayList of all cities and an ArrayList of ArrayLists of Integers
 * representing the lines and rows of distances as shown in the file.
 */

public class DistanceMatrix {

    private ArrayList<String> cities;
    private ArrayList<ArrayList<Integer>> distances;

    /* Creates a new distance matrix given a file in the indicated format. */
    public DistanceMatrix(String fileName) {
        cities = new ArrayList<>();
        distances = new ArrayList<>();
        Scanner sc = null;
        try {
            sc = new Scanner(new BufferedReader(new FileReader(fileName)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String[] line = sc.nextLine().trim().split("\\s+");
        cities.add(line[1]);
        while(sc.hasNextLine()) {
            ArrayList<Integer> list = new ArrayList<>();
            line = sc.nextLine().trim().split("\\s+");
            for (int i = 1; i < line.length; i++) {
                if (i==line.length-1) {
                    cities.add(line[i]);
                }
                else {
                    list.add(Integer.parseInt(line[i]));
                }
            }
            distances.add(list);
        }
    }

    /* Creates a distance matrix given another and a list of cities occurring in it.
     * Assumes that the list of cities follows the order given in the original matrix. */
    public DistanceMatrix(DistanceMatrix m, ArrayList<String> cityList) {
        cities = cityList;
        distances = new ArrayList<>();
        for (int i=1; i<cities.size();i++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int j=0; j<i;j++) {
            	list.add(m.distance(cityList.get(i),cityList.get(j)));
            }
            distances.add(list);
        }
    }
    

    /* Creates a distance matrix given another and a String of initials. Assumes that the
    String provides the initials of a list of cities in the order of the original matrix. */
    public DistanceMatrix(DistanceMatrix m, String initials) {
    	cities = m.getCities(initials);
        distances = new ArrayList<>();
        for (int i=1; i<cities.size();i++) {
            ArrayList<Integer> list = new ArrayList<>();
            for (int j=0; j<i;j++) {
            	list.add(m.distance(cities.get(i),cities.get(j)));
            }
            distances.add(list);
        }
    }

    /* Returns the distance between two given cities. */
    public Integer distance(String city1, String city2) {
        int indexC1 = cities.indexOf(city1);
        int indexC2 = cities.indexOf(city2);
        if (indexC1<indexC2)
            return distances.get(indexC2-1).get(indexC1);
        else
            return distances.get(indexC1-1).get(indexC2);
    }

    /* Returns the list of cities*/
    public ArrayList<String> getCities() {return cities;}
    
    /* Given a String filter, returns a subset of cities with initials in filter. */
    public ArrayList<String> getCities(String filter) {   	
		ArrayList<String> cityList = new ArrayList<String>();
		for (int i=0; i < filter.length(); i++) {
			cityList.add(getCity(filter.charAt(i)));
		}
    	return cityList;
    }
    
    /* Given a char c, returns the city with the first letter c. */
    public String getCity(char c) {
    	for (int i=0;i<cities.size();i++) {
    		if (cities.get(i).charAt(0) == c) return cities.get(i);
    	}
    	return null;
    }
    
    /* From a list of cities cityList, returns a String with their initials */
    public String getInitials(ArrayList<String>  cityList) {  
    	String initials = "";
    	for (int i=0;i<cityList.size();i++) {
    		initials += cityList.get(i).charAt(0);
    	}
    	return initials;
    }

    /* Returns the distances*/
    public ArrayList<ArrayList<Integer>> getDistances() {
        return distances;
    }

    /* Private: shows all cities in the matrix (but the last) for the sake of showing the distance matrix. */
    private void showCities() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<cities.size()-1;i++) {
            sb.append(cities.get(i));
            if (i<cities.size()-2)
                sb.append(", ");
        }
        sb.append("]");
        System.out.println( sb.toString());
    }

    /* Private: shows all distances in a row in the matrix with adequate sizes to fit the columns */
    private String showRow(Integer index) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i=0;i<distances.get(index).size();i++) {
            int l = cities.get(i).length();
            int d = distances.get(index).get(i);
            sb.append(String.format("%1$"+l+"s",d));
            if (i<distances.get(index).size()-1)
               sb.append(", ");
        }
        sb.append("]");
        return sb.toString();
    }

    /* Presents the distance matrix. */
    public void showDistances() {
        System.out.print("           ");
        this.showCities();
        for (int i=0;i<distances.size();i++) {
           System.out.println(String.format("%1$10s" + " " + this.showRow(i),cities.get(i+1)));
        }
    }
}