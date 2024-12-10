 import java.awt.*;


public class EdgeData {

    private Double distance;

    private Color color = new Color(8,83,109); //default color


    public EdgeData(Double distance) {
        this.distance = distance;
    }


    //Override the toString to get the edge distance as string

    public String toString() {
        return distance.toString();
    }

    // Getters
    public double getDistance() {
        return distance;
    }


    public Color getColor() {
        return color;
    }

    // Setters
    public void setDistance(double distance) {
        this.distance = distance;
    }


    public void setColor(Color color) {
        this.color = color;
    }

}
