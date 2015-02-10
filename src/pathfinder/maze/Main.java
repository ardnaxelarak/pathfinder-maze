package pathfinder.maze;

import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.io.IOException;

import static java.util.Arrays.asList;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        OptionParser parser = new OptionParser();

        OptionSpec<Integer> width = parser.acceptsAll(asList("w", "width"), "width of display").withRequiredArg().describedAs("width").ofType(Integer.class).defaultsTo(800);
        OptionSpec<Integer> height = parser.acceptsAll(asList("h", "height"), "height of display").withRequiredArg().describedAs("height").ofType(Integer.class).defaultsTo(600);
        OptionSpec<Integer> display = parser.acceptsAll(asList("d", "display"), "screen to display on").withRequiredArg().describedAs("num").ofType(Integer.class).defaultsTo(0);
        OptionSpec fullScreen = parser.acceptsAll(asList("f", "full-screen", "fullscreen"), "shows display fullscreen");
        OptionSpec<String> filename = parser.acceptsAll(asList("m", "map"), "map file").withRequiredArg().describedAs("filename").ofType(String.class);
        OptionSpec<Integer> minimap = parser.acceptsAll(asList("t", "thumbnail"), "show thumbnail").withOptionalArg().describedAs("size").ofType(Integer.class).defaultsTo(4);

        try
        {
            OptionSet options = parser.parse(args);
            DispLauncher dl = new DispLauncher("Maze", options.valueOf(width), options.valueOf(height), options.valueOf(display), options.has(fullScreen), options.valueOf(filename));
            if (options.has(minimap))
                dl.setMiniMap(0, options.valueOf(minimap));
            else
                dl.setMiniMap(2, 4);
        }
        catch (OptionException e)
        {
            parser.printHelpOn(System.out);
            return;
        }
        catch (IllegalArgumentException e)
        {
            parser.printHelpOn(System.out);
            return;
        }
    }
}
