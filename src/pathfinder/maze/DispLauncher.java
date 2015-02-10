package pathfinder.maze;

import pathfinder.maze.Display;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class DispLauncher extends Frame
{
    private Display disp;

    public DispLauncher(String title, int width, int height, int screen, boolean fullScreen)
    {
        super(title);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice[] gs = ge.getScreenDevices();

        setLayout(new BorderLayout());
        disp = new Display(width, height);
        disp.setSize(new Dimension(width, height));
        add(disp, BorderLayout.CENTER);
        disp.init();

        if (fullScreen)
            setUndecorated(true);

        pack();

        if (screen < 0 || screen >= gs.length)
            screen = 0;

        if (fullScreen)
        {
            gs[screen].setFullScreenWindow(this);
        }
        else
        {
            Rectangle r = gs[screen].getDefaultConfiguration().getBounds();
            setLocation(r.x, r.y);
        }

        setVisible(true);
        addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                disp.exit();
            }
        });
    }
}
