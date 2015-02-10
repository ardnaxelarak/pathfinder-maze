package pathfinder.maze;

import processing.core.*;

import java.awt.Dimension;

public class Display extends PApplet
{
    private final int width, height;

    public Display(int width, int height)
    {
        this.width = width;
        this.height = height;
        setPreferredSize(new Dimension(width, height));
    }

    public void setup()
    {
        size(width, height);
        if (frame != null)
            frame.setResizable(true);
        background(123, 231, 17);
    }

    public void draw()
    {
    }
}
