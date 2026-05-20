package fastterminal.ui;

import fastterminal.FastTerminalScene;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * A premium, highly-responsive interactive File Navigator component.
 * Supports directory browsing, folder navigation on click, hover highlights,
 * file size formatting, and automatic scaling/resizing.
 */
public class FileNavigator extends Component {
    private File currentDir;
    private List<File> filesList;
    private int scrollOffset = 0;
    private int selectedIndex = -1; // Hovered/Selected item index

    // Themeable colors
    private int selectionBg = 0x2563EB;
    private int selectionFg = 0xFFFFFF;
    private int pathBarBg   = 0x6366F1;
    private int pathBarFg   = 0xFFFFFF;
    private double bgAlpha  = 1.0; // 1.0 = opaque, <1.0 = transparent (matches Panel bodyAlpha)

    public FileNavigator(int x, int y, int width, int height) {
        super(x, y, width, height);
        // Start at current workspace root directory
        this.currentDir = new File(".").getAbsoluteFile().getParentFile();
        refreshFiles();
    }

    private void refreshFiles() {
        filesList = new ArrayList<>();
        if (currentDir == null) return;

        // Add parent directory navigation entry if applicable
        File parent = currentDir.getParentFile();
        if (parent != null) {
            filesList.add(new File(currentDir, ".."));
        }

        File[] list = currentDir.listFiles();
        if (list != null) {
            List<File> dirs = new ArrayList<>();
            List<File> files = new ArrayList<>();
            for (File f : list) {
                if (f.isHidden()) continue;
                if (f.isDirectory()) {
                    dirs.add(f);
                } else {
                    files.add(f);
                }
            }
            dirs.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            files.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            filesList.addAll(dirs);
            filesList.addAll(files);
        }
        
        // Reset selections on directory change
        selectedIndex = -1;
        scrollOffset = 0;
    }

    @Override
    public void render(FastTerminalScene canvas) {
        if (!visible) return;

        // 1. Background: NOT drawn here — Panel already drew its (possibly transparent) body.
        //    Only hovered rows and the path/footer bars get explicit backgrounds below.

        // 2. Render Path Header bar
        for (int c = x; c < x + width; c++) {
            if (c >= 0 && c < canvas.getWidth() && y >= 0 && y < canvas.getHeight()) {
                canvas.writeCell(c, y, ' ', pathBarFg, pathBarBg);
            }
        }
        String pathText = "📂 " + truncatePath(currentDir.getAbsolutePath(), width - 7);
        canvas.writeString(x + 2, y, pathText, pathBarFg, pathBarBg);

        int visibleHeight = height - 2; // Subtract header and footer rows
        if (visibleHeight <= 0) return;

        // Calculate brightness of bgColor to adapt directory, file, and metadata colors dynamically
        int rBg = (bgColor >> 16) & 0xFF;
        int gBg = (bgColor >> 8) & 0xFF;
        int bBg = bgColor & 0xFF;
        double brightness = (0.299 * rBg + 0.587 * gBg + 0.114 * bBg) / 255.0;
        boolean isLightBg = brightness > 0.5;

        // Auto-bound selected index to valid list items
        if (selectedIndex >= filesList.size()) {
            selectedIndex = filesList.size() - 1;
        }

        // 3. Render file/folder items
        for (int i = 0; i < visibleHeight; i++) {
            int itemIndex = scrollOffset + i;
            if (itemIndex >= filesList.size()) break;

            File item = filesList.get(itemIndex);
            int ry = y + 1 + i;

            boolean isHoveredRow = (selectedIndex == itemIndex);
            int rowBg = isHoveredRow ? selectionBg : bgColor;
            int rowFg = isHoveredRow ? selectionFg : fgColor;

            // Clear row — hovered rows are fully opaque; non-hovered rows are transparent
            for (int c = x; c < x + width; c++) {
                if (c >= 0 && c < canvas.getWidth() && ry >= 0 && ry < canvas.getHeight()) {
                    if (isHoveredRow) {
                        canvas.writeCell(c, ry, ' ', rowFg, rowBg);
                    } else {
                        // Use alpha so the Panel's transparent body shows through
                        canvas.writeCellAlpha(c, ry, ' ', rowFg, rowBg, 1.0, bgAlpha);
                    }
                }
            }

            // Draw Icon and Name
            String name = item.getName();
            String icon = "📄 ";
            String sizeStr = "";

            if (name.equals("..")) {
                icon = "📁 ";
                name = "↑ ..";
            } else if (item.isDirectory()) {
                icon = "📁 ";
                sizeStr = " <DIR> ";
            } else {
                sizeStr = " " + formatFileSize(item.length()) + " ";
            }

            // Format layout: Left-aligned name, right-aligned size/meta
            String displayStr = " " + icon + name;
            if (displayStr.length() > width - 12) {
                displayStr = displayStr.substring(0, width - 12) + "...";
            }

            int folderColor = isHoveredRow ? selectionFg : fgColor; // Black/dark text for directories
            int fileColor   = isHoveredRow ? selectionFg : fgColor;
            int metaColor   = isHoveredRow ? selectionFg : (isLightBg ? 0x71717A : 0xA1A1AA);

            canvas.writeString(x + 1, ry, displayStr, item.isDirectory() ? folderColor : fileColor, rowBg);

            // Right-aligned file size
            if (!sizeStr.isEmpty()) {
                int sizeX = x + width - sizeStr.length() - 1;
                if (sizeX > x + 10) {
                    canvas.writeString(sizeX, ry, sizeStr, metaColor, rowBg);
                }
            }
        }

        // 4. Render Footer info bar
        String footerText = " Total: " + filesList.size() + " items | Hover to select";
        if (footerText.length() > width) {
            footerText = footerText.substring(0, width);
        }
        int footerColor = isLightBg ? 0x71717A : 0xA1A1AA;
        canvas.writeString(x + 1, y + height - 1, footerText, footerColor, bgColor);
    }

    @Override
    public void handleMouseMove(int cellX, int cellY) {
        super.handleMouseMove(cellX, cellY);

        if (contains(cellX, cellY)) {
            // Find which row the mouse is over
            int row = cellY - (y + 1);
            int visibleHeight = height - 2;
            if (row >= 0 && row < visibleHeight) {
                int itemIndex = scrollOffset + row;
                if (itemIndex >= 0 && itemIndex < filesList.size()) {
                    selectedIndex = itemIndex;
                    return;
                }
            }
        }
        selectedIndex = -1;
    }

    @Override
    public boolean handleMouseClick(int cellX, int cellY, boolean isPressed) {
        if (!isPressed && contains(cellX, cellY)) {
            // Trigger item click action on release
            int row = cellY - (y + 1);
            int visibleHeight = height - 2;
            if (row >= 0 && row < visibleHeight) {
                int itemIndex = scrollOffset + row;
                if (itemIndex >= 0 && itemIndex < filesList.size()) {
                    File selected = filesList.get(itemIndex);
                    if (selected.getName().equals("..")) {
                        // Go up
                        File parent = currentDir.getParentFile();
                        if (parent != null) {
                            currentDir = parent;
                            refreshFiles();
                        }
                    } else if (selected.isDirectory()) {
                        // Go in
                        currentDir = selected;
                        refreshFiles();
                    }
                    return true;
                }
            }
        }
        return super.handleMouseClick(cellX, cellY, isPressed);
    }

    private String truncatePath(String path, int maxLen) {
        if (path.length() <= maxLen) return path;
        return "..." + path.substring(path.length() - maxLen + 3);
    }

    private String formatFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public void selectPrevious() {
        if (filesList.isEmpty()) return;
        if (selectedIndex <= 0) {
            selectedIndex = filesList.size() - 1; // Wrap around
        } else {
            selectedIndex--;
        }
        ensureSelectionVisible();
    }

    public void selectNext() {
        if (filesList.isEmpty()) return;
        if (selectedIndex < 0 || selectedIndex >= filesList.size() - 1) {
            selectedIndex = 0; // Wrap around
        } else {
            selectedIndex++;
        }
        ensureSelectionVisible();
    }

    private void ensureSelectionVisible() {
        int visibleHeight = height - 2;
        if (visibleHeight <= 0) return;
        
        if (selectedIndex < scrollOffset) {
            scrollOffset = selectedIndex;
        } else if (selectedIndex >= scrollOffset + visibleHeight) {
            scrollOffset = selectedIndex - visibleHeight + 1;
        }
    }

    public boolean activateSelected() {
        if (selectedIndex >= 0 && selectedIndex < filesList.size()) {
            File selected = filesList.get(selectedIndex);
            if (selected.getName().equals("..")) {
                File parent = currentDir.getParentFile();
                if (parent != null) {
                    currentDir = parent;
                    refreshFiles();
                    return true;
                }
            } else if (selected.isDirectory()) {
                currentDir = selected;
                refreshFiles();
                return true;
            }
        }
        return false;
    }
    public int getSelectionBg() { return selectionBg; }
    public void setSelectionBg(int selectionBg) { this.selectionBg = selectionBg; }
    public int getSelectionFg() { return selectionFg; }
    public void setSelectionFg(int selectionFg) { this.selectionFg = selectionFg; }
    public int getPathBarBg() { return pathBarBg; }
    public void setPathBarBg(int pathBarBg) { this.pathBarBg = pathBarBg; }
    public int getPathBarFg() { return pathBarFg; }
    public void setPathBarFg(int pathBarFg) { this.pathBarFg = pathBarFg; }
    public double getBgAlpha() { return bgAlpha; }
    public void setBgAlpha(double bgAlpha) { this.bgAlpha = bgAlpha; }
}
