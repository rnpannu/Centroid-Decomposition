 import java.awt.*;


public class NodeData {
    // Position as awt Point class
    private Point position;

    private String text;

    // default
    private Color color = new Color(58, 5, 94);


    public NodeData(Point position, String text) {
        this.position = position;
        this.text = text;
    }

    // Accessors
    public Point getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }


    public Color getColor() {
        return color;
    }

    // Setters
    public void setPosition(Point position) {
        this.position = position;
    }

    public void setText(String text) {
        this.text = text;
    }


    public void setColor(Color color) {
        this.color = color;
    }
 

     //Override toString
    @Override
    public String toString() {
        return getText();
    }

}
