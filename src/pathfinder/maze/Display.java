package pathfinder.maze;

import processing.core.*;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Scanner;
import java.util.Stack;
import java.util.TreeMap;

import static java.lang.Math.abs;

public class Display extends PApplet
{
    private final int defwidth, defheight;

    private class Stone implements Comparable<Stone>
    {
        private int x, y;
        private Tile place;

        public Stone(int x, int y, Tile place)
        {
            this.x = x;
            this.y = y;
            this.place = place;
        }

        public Stone(int x, int y)
        {
            this.x = x;
            this.y = y;
            this.place = midlayer[y][x];
        }

        public int compareTo(Stone s)
        {
            if (x > s.x)
                return 1;
            else if (x < s.x)
                return -1;
            else if (y > s.y)
                return 1;
            else if (y < s.y)
                return -1;
            else
                return place.compareTo(s.place);
        }
    }

    private TreeMap<Stone, Integer> colors;

    private Tile[][] map, midlayer, seen;
    private int[] colorList;
    private int xPos, yPos;
    private int mmcsize, score, steps;
    private int mmmode;
    private float xOld, yOld;
    private float scalesize = 1.00f;
    private boolean pickaxe;
    private int seedist;
    private int bombs;
    private PImage imPick, imGem, imCandle, imBomb, imUp, imOoze, imDelver;
    private LinkedList<String> messages;
    private String filename;

    public Display(int width, int height, String filename)
    {
        this.defwidth = width;
        this.defheight = height;
        this.filename = filename;
        setPreferredSize(new Dimension(defwidth, defheight));
    }

    public void setup()
    {
        size(defwidth, defheight);
        if (frame != null)
            frame.setResizable(true);

        colorList = new int[10];
        colorList[0] = color(255);
        colorList[1] = color(247, 160, 190);
        colorList[2] = color(100, 227, 161);
        colorList[3] = color(90, 197, 250);
        colorList[4] = color(222, 157, 44);
        colorList[5] = color(250, 80, 250);
        colorList[6] = color(251, 255, 122);
        colorList[7] = color(205, 154, 219);
        colorList[8] = color(41, 168, 196);
        colorList[9] = color(82, 196, 67);

        mmcsize = 4;
        mmmode = 1;
        imPick = loadImage("images/pickaxe.png");
        imGem = loadImage("images/gem0.png");
        imCandle = loadImage("images/candle.png");
        imBomb = loadImage("images/bomb.png");
        imUp = loadImage("images/stairs-up.png");
        imOoze = loadImage("images/magma-ooze.gif");
        imDelver = loadImage("images/delver.jpg");
        imageMode(CENTER);
        newgame();
    }

    private Stone curStone()
    {
        return new Stone(xPos, yPos, midlayer[yPos][xPos]);
    }

    public void setMiniMap(int mode, int size)
    {
        mmcsize = size;
        mmmode = mode;
    }

    private void newgame()
    {
        try
        {
            mapFromFile(filename);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return;
        }
        colors = new TreeMap<Stone, Integer>();
        score = 0;
        steps = 0;
        xOld = xPos;
        yOld = yPos;
        seedist = 3;
        pickaxe = false;
        bombs = 0;
        messages = new LinkedList<String>();
        checkseen();
        sendMessage("Welcome to explorEr: Capital E Edition!");
        sendMessage("You can move with the arrow keys to explore the map.");
        sendMessage("Pressing 2 will turn the minimap off, 1 will make it semi-transparent, and 0 will make it opaque.");
    }

    private void sendMessage(String format, Object... args)
    {
        messages.addLast(String.format(format, args));
        if (messages.size() > 5)
            messages.removeFirst();
    }

    public void draw()
    {
        fill(100);
        rect(0, 0, width, height);
        drawMap();
        if (mmmode == 0)
            drawMiniMap(255);
        else if (mmmode == 1)
            drawMiniMap(100);
        fill(200, 200);
        /*
        rect(0, 0, width, 91);
        fill(0);
        ListIterator<String> mi = messages.listIterator(0);
        int y = 7;
        textAlign(LEFT, TOP);
        while (mi.hasNext())
        {
            text(mi.next(), 10, y);
            y += 15;
        }
        */
        textAlign(RIGHT, TOP);
        text("steps: " + steps, width - 10, 7);
        
        float iw = 50;
        fill(200, 200);
        //rect(width - 3 * iw, height - iw, 3 * iw, iw);
        /*
        if (pickaxe)
            image(imPick, width - 1.5f * iw, height - 0.5f * iw, iw * 0.6f, iw * 0.6f);
        image(imCandle, width - 0.5f * iw, height - 0.5f * iw, iw * 0.6f, iw * 0.6f);
        if (bombs > 0)
        {
            image(imBomb, width - 2.5f * iw, height - 0.5f * iw, iw * 0.6f, iw * 0.6f);
            fill(0);
            text(str(bombs), width - 2.70f * iw, height - 0.80f * iw);
        }
        */
    }

    private void fillAlpha(int color, float alpha)
    {
        fill(red(color), green(color), blue(color), alpha);
    }

    private void drawMap()
    {
        pushMatrix();
        float sdim = min(width / 5, height / 5);
        sdim *= scalesize;
        float wsize = width / sdim;
        float hsize = height / sdim;
        scale(sdim, sdim);
        xOld = (2 * xOld + xPos) / 3;
        yOld = (2 * yOld + yPos) / 3;
        translate(-xOld + wsize / 2f - 0.5f, -yOld + hsize / 2f - 0.5f);
        stroke(0);
        strokeWeight(0.006f);
        mapdraw(255, true, true);
        popMatrix();
    }

    private void drawMiniMap(float alph)
    {
        pushMatrix();
        translate(2 * mmcsize, height - mmcsize * (map.length + 2));
        scale(mmcsize, mmcsize);
        fill(220, 250, 50, alph);
        rect(-2, -2, (map[0].length + 4), (map.length + 4));
        mapdraw(alph, false, false);
        popMatrix();
    }

    private void drawim(PImage im, float x, float y, float size)
    {
        pushMatrix();
        translate(x + 0.5f, y + 0.5f);
        scale(0.05f, 0.05f);
        image(im, 0, 0, size, size);
        popMatrix();
    }

    private void mapdraw(float alph, boolean stroke, boolean reqSeen)
    {
        fill(0, alph);
        rect(-1, -1, (map[0].length + 2), 1);
        rect(-1, 0, 1, (map.length));
        rect(-1, map.length, (map[0].length + 2), 1);
        rect(map[0].length, 0, 1, (map.length));
        for (int i = 0; i < map.length; i++)
        {
            for (int j = 0; j < map[i].length; j++)
            {
                switch (reqSeen ? seen[i][j] : map[i][j])
                {
                    case UNSEEN:
                        fill(100, alph);
                        if (stroke)
                            stroke(100, alph);
                        else
                            noStroke();
                        rect(j, i, 1, 1);
                        break;
                    case WALL:
                        fill(0, alph);
                        if (stroke)
                            stroke(0, alph);
                        else
                            noStroke();
                        rect(j, i, 1, 1);
                        break;
                    case OPEN:
                    case CROSS_H:
                    case CROSS_V:
                        Stone cur = new Stone(j, i);
                        if (colors.containsKey(cur))
                            fillAlpha(colorList[colors.get(cur)], alph);
                        else
                            fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        break;
                    case CROSS:
                        fill(200, 0, 0, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        break;
                    case UP:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imUp, j, i, 10);
                        break;
                    case DELVER:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imDelver, j, i, 10);
                        break;
                    case OOZE:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imOoze, j, i, 10);
                        break;
                    case PICKAXE:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imPick, j, i, 10);
                        break;
                    case GEM:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imGem, j, i, 10);
                        break;
                    case BOMB:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imBomb, j, i, 10);
                        break;
                    case CANDLE:
                        fill(255, alph);
                        noStroke();
                        rect(j, i, 1, 1);
                        drawim(imCandle, j, i, 10);
                        break;
                }
            }
        }
        fill(255, 0, 0, 255 - (255 - alph) / 2);
        ellipseMode(CENTER);
        noStroke();
        ellipse((xOld + 0.5f), (yOld + 0.5f), 0.8f, 0.8f);
    }

    private Point[] getorder()
    {
        Point[] v = new Point[4];
        v[0] = new Point(0, 1);
        v[1] = new Point(0, -1);
        v[2] = new Point(1, 0);
        v[3] = new Point(-1, 0);
        Point temp;
        for (int i = 0; i < v.length; i++)
        {
            int j = (int)random(i, v.length);
            temp = v[i];
            v[i] = v[j];
            v[j] = temp;
        }
        return v;
    }

    private void createLayers()
    {
        seen = new Tile[map.length][];
        midlayer = new Tile[map.length][];
        for (int i = 0; i < map.length; i++)
        {
            seen[i] = new Tile[map[i].length];
            midlayer[i] = new Tile[map[i].length];
            for (int j = 0; j < map[i].length; j++)
            {
                seen[i][j] = Tile.UNSEEN;
                midlayer[i][j] = map[i][j];
            }
        }
    }

    private void genmap(int rows, int columns, int numgems, int numbombs)
    {
        Stack<Point> stack = new Stack<Point>();
        Point init = new Point(0, 0);
        stack.push(init);
        map = new Tile[rows * 2 - 1][columns * 2 - 1];
        for (int i = 0; i < map.length; i++)
            for (int j = 0; j < map[i].length; j++)
                map[i][j] = Tile.WALL;

        map[init.y][init.x] = Tile.OPEN;
        while (!stack.empty())
        {
            Point current = stack.peek();
            Point next;
            boolean found = false;
            Point[] list = getorder();
            for (int i = 0; i < list.length && !found; i++)
            {
                next = new Point(current.x + list[i].x * 2, current.y + list[i].y * 2);
                if (next.x < 0 || next.y < 0 || next.y >= map.length || next.x >= map[next.y].length)
                    continue;
                if (map[next.y][next.x] == Tile.WALL)
                {
                    found = true;
                    map[next.y][next.x] = Tile.OPEN;
                    stack.push(next);
                    Point temp = new Point(current.x + list[i].x, current.y + list[i].y);
                    map[temp.y][temp.x] = Tile.OPEN;
                }
            }
            if (!found)
                stack.pop();
        }
        int xp = (int)random(columns) * 2;
        int yp = (int)random(rows) * 2;
        map[yp][xp] = Tile.PICKAXE;
        xp = (int)random(columns) * 2;
        yp = (int)random(rows) * 2;
        map[yp][xp] = Tile.CANDLE;
        int placed = 0;
        while (placed < numgems)
        {
            yp = (int)random(map.length);
            xp = (int)random(map[yp].length);
            if (map[yp][xp] == Tile.OPEN)
            {
                map[yp][xp] = Tile.GEM;
                placed++;
            }
        }
        placed = 0;
        while (placed < numbombs)
        {
            yp = (int)random(map.length);
            xp = (int)random(map[yp].length);
            if (map[yp][xp] == Tile.OPEN)
            {
                map[yp][xp] = Tile.BOMB;
                placed++;
            }
        }
        createLayers();
    }

    private void makeMapArray(String mapString)
    {
        String[] pieces = split(mapString, "\n");
        map = new Tile[pieces.length][];
        int i = 0;
        int j;
        for (String s : pieces)
        {
            j = 0;
            map[i] = new Tile[s.length()];
            for (char c : s.toCharArray())
            {
                map[i][j] = Tile.fromChar(c);
                j++;
            }
            i++;
        }
        createLayers();
    }

    private void mapFromFile(String filename) throws IOException
    {
        Scanner sc = new Scanner(new File(filename));
        LinkedList<String> pieces = new LinkedList<String>();
        xPos = sc.nextInt();
        yPos = sc.nextInt();
        sc.nextLine();
        while (sc.hasNextLine())
            pieces.add(sc.nextLine());
        sc.close();
        map = new Tile[pieces.size()][];
        seen = new Tile[map.length][];
        int i = 0;
        int j;
        for (String s : pieces)
        {
            j = 0;
            map[i] = new Tile[s.length()];
            for (char c : s.toCharArray())
            {
                map[i][j] = Tile.fromChar(c);
                j++;
            }
            i++;
        }
        createLayers();
    }

    private void look(int x, int y)
    {
        if (y < 0 || x < 0 || y >= map.length || x >= map[y].length)
            return;
        seen[y][x] = midlayer[y][x];
    }

    private void updateMid(int x, int y, Tile value)
    {
        if (y < 0 || x < 0 || y >= midlayer.length || x >= midlayer[y].length)
            return;
        midlayer[y][x] = value;
    }

    private void updateMid(int x, int y)
    {
        if (y < 0 || x < 0 || y >= midlayer.length || x >= midlayer[y].length)
            return;
        if (midlayer[y][x] == Tile.WALL)
            return;
        midlayer[y][x] = map[y][x];
    }

    private void setLayers()
    {
        for (int i = 0; i < seen.length; i++)
            for (int j = 0; j < seen[i].length; j++)
            {
                seen[i][j] = Tile.UNSEEN;
                if (!map[i][j].isCross())
                    midlayer[i][j] = map[i][j];
            }

        for (int y = 0; y < midlayer.length; y++)
        {
            for (int x = 0; x < midlayer[y].length; x++)
            {
                if (map[y][x].isCross())
                {
                    int xd = abs(x - xPos);
                    int yd = abs(y - yPos);
                    if (xd > yd)
                        midlayer[y][x] = Tile.CROSS_H;
                    else if (xd < yd)
                        midlayer[y][x] = Tile.CROSS_V;
                    else if (xd != 0)
                        midlayer[y][x] = Tile.CROSS;
                }

                switch (midlayer[y][x])
                {
                    case CROSS:
                        updateMid(x - 1, y);
                        updateMid(x + 1, y);
                        updateMid(x, y - 1);
                        updateMid(x, y + 1);
                        break;
                    case CROSS_H:
                        updateMid(x - 1, y);
                        updateMid(x + 1, y);
                        updateMid(x, y - 1, Tile.WALL);
                        updateMid(x, y + 1, Tile.WALL);
                        break;
                    case CROSS_V:
                        updateMid(x - 1, y, Tile.WALL);
                        updateMid(x + 1, y, Tile.WALL);
                        updateMid(x, y - 1);
                        updateMid(x, y + 1);
                        break;
                }
            }
        }
    }

    private void checkseen()
    {
        setLayers();

        look(xPos, yPos);

        for (int xd = -1; xd <= 1; xd++)
        {
            for (int yd = -1; yd <= 1; yd++)
            {
                if (xd == 0 && yd == 0)
                    continue;

                boolean done = false;
                for (int k = 1; !done && (k <= seedist); k++)
                {
                    if (yPos + k * yd < 0 || xPos + k * xd < 0 || yPos + k * yd >= map.length || xPos + k * xd >= map[yPos + k * yd].length)
                    {
                        done = true;
                        continue;
                    }
                    else
                    {
                        look(xPos + k * xd, yPos + k * yd);
                        if (xd * yd == 0)
                        {
                            look(xPos + k * xd + yd, yPos + k * yd - xd);
                            look(xPos + k * xd - yd, yPos + k * yd + xd);
                        }
                        if (midlayer[yPos + k * yd][xPos + k * xd] == Tile.WALL)
                            done = true;
                    }
                }
            }
        }
    }

    private void movePlayer(int xd, int yd)
    {
        if (xPos + xd < 0 || yPos + yd < 0 || yPos + yd >= map.length || xPos + xd >= map[yPos + yd].length)
            return;
        boolean validmove = true;
        switch (midlayer[yPos + yd][xPos + xd])
        {
            case WALL:
                if (pickaxe)
                {
                    sendMessage("You dig through the wall.");
                    map[yPos + yd][xPos + xd] = Tile.OPEN;
                }
                else
                    validmove = false;
                break;
            case DELVER:
            case OOZE:
                map[yPos + yd][xPos + xd] = Tile.OPEN;
                break;
            case PICKAXE:
                sendMessage("You found a pickaxe! You can now dig through walls.");
                pickaxe = true;
                map[yPos + yd][xPos + xd] = Tile.OPEN;
                break;
            case GEM:
                sendMessage("You found a gem!");
                score += 1000;
                map[yPos + yd][xPos + xd] = Tile.OPEN;
                break;
            case BOMB:
                sendMessage("You found a bomb! Press B to use it.");
                bombs++;
                map[yPos + yd][xPos + xd] = Tile.OPEN;
                break;
            case CANDLE:
                sendMessage("You found a candle! Your vision is enhanced.");
                seedist = 12;
                map[yPos + yd][xPos + xd] = Tile.OPEN;
                break;
        }
        if (validmove)
        {
            xPos += xd;
            yPos += yd;
            steps++;
            checkseen();
        }
    }

    private void placebomb()
    {
        if (bombs <= 0)
        {
            sendMessage("You have no bombs.");
            return;
        }
        else
        {
            bombs--;
            sendMessage("You have " + bombs + " bomb" + ((bombs > 1)?"s":"") + " left.");
            for (int i = -1; i <= 1; i++)
                for (int j = -1; j <= 1; j++)
                {
                    if (yPos + i < 0 || xPos + j < 0 || yPos + i >= map.length || xPos + j >= map[yPos + i].length)
                        continue;
                    if (map[yPos + i][xPos + j] == Tile.WALL)
                        map[yPos + i][xPos + j] = Tile.OPEN;
                }
        }
    }

    public void keyPressed()
    {
        if (key == CODED)
        {
            switch (keyCode)
            {
                case UP:
                    movePlayer(0, -1);
                    break;
                case DOWN:
                    movePlayer(0, 1);
                    break;
                case LEFT:
                    movePlayer(-1, 0);
                    break;
                case RIGHT:
                    movePlayer(1, 0);
                    break;
                case KeyEvent.VK_F1:
                    mmmode = 2;
                    break;
                case KeyEvent.VK_F2:
                    mmmode = 1;
                    break;
                case KeyEvent.VK_F3:
                    mmmode = 0;
                    break;
                case KeyEvent.VK_PAGE_UP:
                    scalesize += 0.01f;
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    scalesize -= 0.01f;
                    break;
            }
        }
        else if (key >= '0' && key <= '9')
        {
            colors.put(curStone(), key - '0');
        }
        else if (key == 'n' || key == 'N')
            newgame();
        else if (key == 'b' || key == 'B')
            placebomb();
        else if (key == '.')
            steps++;
        else if (key == '-')
            steps = 0;
        else if (key == ESC)
            key = ' ';
    }
}
