package pathfinder.maze;

public enum Tile
{
    UNSEEN(false),
    WALL(false),
    OPEN(false),
    UP(false),
    DOWN(false),
    CROSS(true),
    CROSS_H(true),
    CROSS_V(true),
    PICKAXE(false),
    CANDLE(false),
    GEM(false),
    OOZE(false),
    DELVER(false),
    BOMB(false);

    private final boolean isCross;

    private Tile(boolean cross)
    {
        isCross = cross;
    }

    public boolean isCross()
    {
        return isCross;
    }

    public static Tile fromChar(char t)
    {
        switch (t)
        {
            case ' ':
            case '.':
                return OPEN;
            case '<':
                return UP;
            case '>':
                return DOWN;
            case '+':
                return CROSS;
            case '-':
                return CROSS_H;
            case '|':
                return CROSS_V;
            case 'X':
            case '#':
            case '@':
                return WALL;
            case '!':
                return BOMB;
            case 'c':
                return CANDLE;
            case 'o':
                return OOZE;
            case 'D':
                return DELVER;
            case '*':
                return GEM;
            case 'p':
                return PICKAXE;
            default:
                return OPEN;
        }
    }
}
