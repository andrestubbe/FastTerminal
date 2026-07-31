package fastterminal.swing;

import fasttheme.FastTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicScrollBarUI;
import java.awt.*;
import java.io.PrintStream;

public class Console extends JFrame {

    private static volatile Console instance;

    public static Console getInstance() {
        if (instance == null) {
            synchronized (Console.class) {
                if (instance == null) {
                    instance = new Console();
                }
            }
        }
        return instance;
    }

    private final JTextArea outputArea;

    public Console() {
        this(0, 0, 1600, 1024);
    }

    public Console(int x, int y, int width, int height) {
        super("FastTerminal Console");

        Color darkBg = new Color(0x0c0c0c);

        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setBounds(x, y, width, height);
        setAlwaysOnTop(false);

        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font("Consolas", Font.PLAIN, 32));
        outputArea.setBackground(darkBg);
        outputArea.setForeground(new Color(0xCCCCCC));

        JScrollPane scrollPane = new JScrollPane(outputArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        scrollPane.setViewportBorder(null);
        scrollPane.getViewport().setBorder(null);
        scrollPane.getViewport().setBackground(darkBg);
        scrollPane.setBackground(darkBg);

        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {

            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }

            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }

            private JButton createZeroButton() {
                JButton btn = new JButton();
                btn.setPreferredSize(new Dimension(0, 0));
                btn.setMinimumSize(new Dimension(0, 0));
                btn.setMaximumSize(new Dimension(0, 0));
                btn.setOpaque(false);
                btn.setBorder(null);
                return btn;
            }

            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 70, 70);
                this.trackColor = darkBg;
            }

            @Override
            protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = thumbBounds.width;
                int h = thumbBounds.height;
                int arc = w;

                g2.setColor(thumbColor);
                g2.fillRoundRect(thumbBounds.x, thumbBounds.y, w, h, arc, arc);

                g2.dispose();
            }

            @Override
            protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
                g.setColor(trackColor);
                g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
            }
        });

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);

        this.getContentPane().setBackground(darkBg);

        this.addNotify();
        long hwnd = FastTheme.getWindowHandle(this);
        if (hwnd != 0) {
            FastTheme.setTitleBarDarkMode(hwnd, true);
            FastTheme.setTitleBarColor(hwnd, darkBg.getRed(), darkBg.getGreen(), darkBg.getBlue());
            FastTheme.setWindowBackgroundColor(hwnd, darkBg.getRed(), darkBg.getGreen(), darkBg.getBlue());
        }

        setVisible(true);
    }

    public static void println(final String string) {
        getInstance().append(string + "\n");
    }

    public static void println(final boolean b) {
        getInstance().append(String.valueOf(b) + "\n");
    }

    public void append(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }

    public void appendLine(String line) {
        append(line + "\n");
    }

    public void clear() {
        SwingUtilities.invokeLater(() -> outputArea.setText(""));
    }

    public void redirectSystemOut() {
        PrintStream printStream = new PrintStream(new java.io.OutputStream() {
            @Override
            public void write(int b) {
                append(String.valueOf((char) b));
            }

            @Override
            public void write(byte[] b, int off, int len) {
                append(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) {
                append(new String(b));
            }
        });

        System.setOut(printStream);
        System.setErr(printStream);
    }
}
