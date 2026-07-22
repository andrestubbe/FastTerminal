package fastterminal;

public class FastStyle {

    public static final byte NONE          = 0;
    public static final byte BOLD          = 1 << 0; // 1
    public static final byte ITALIC        = 1 << 1; // 2
    public static final byte UNDERLINE     = 1 << 2; // 4
    public static final byte STRIKETHROUGH = 1 << 3; // 8
    public static final byte BLINK         = 1 << 4; // 16
    public static final byte INVERT        = 1 << 5; // 32
}
