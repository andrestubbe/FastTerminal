package fastterminal.emojis;

/**
 * Universal Emoji, Glyphs, and Unicode Column-Width Measurement Engine.
 * Provides premium console glyph constants and implements robust East Asian Width (EAW)
 * and Emoji width calculation for pixel-perfect TUI layouts under Windows Terminal and other emulators.
 */
public class FastEmojis {

    // ============================================================================
    // Premium TUI Glyph Constants
    // ============================================================================

    // Emojis & Status Icons (Double-Width, 2 Columns)
    
    // Core Dashboard & Diagnostics
    public static final String LIGHTNING = "⚡";
    public static final String TARGET = "🎯";
    public static final String PALETTE = "🎨";
    public static final String CHART = "📊";
    public static final String PLUG = "🔌";
    public static final String SPARKLES = "✨";
    public static final String GEAR = "⚙️";
    public static final String BUG = "🐛";
    public static final String FIRE = "🔥";
    public static final String ROCKET = "🚀";

    // Smileys & Emoticons (Double-Width, 2 Columns)
    public static final String SMILE = "😊";
    public static final String SMILEY = "😀";
    public static final String GRIN = "😁";
    public static final String WINK = "😉";
    public static final String COOL = "😎";
    public static final String LAUGH = "😂";
    public static final String CELEBRATE = "🥳";
    public static final String THINKING = "🤔";
    public static final String SHOCKED = "😱";
    public static final String SAD = "😢";
    public static final String CRY = "😭";
    public static final String ANGRY = "😠";
    public static final String SWEAT = "😅";
    public static final String ROBOT = "🤖";
    public static final String ALIEN = "👽";
    public static final String GHOST = "👻";
    public static final String POOP = "💩";

    // System Components & Storage
    public static final String TERMINAL = "💻";
    public static final String SERVER = "🖥️";
    public static final String FLOPPY = "💾";
    public static final String HARD_DISK = "💽";
    public static final String KEYBOARD = "⌨️";
    public static final String MOUSE = "🖱️";
    public static final String PRINTER = "🖨️";

    // File Management
    public static final String FOLDER = "📁";
    public static final String FILE = "📄";
    public static final String ARCHIVE = "📦";
    public static final String TRASH = "🗑️";

    // Security & Access
    public static final String LOCK = "🔒";
    public static final String KEY = "🔑";
    public static final String SHIELD = "🛡️";

    // Feedback, Alerts & Status Circles
    public static final String SUCCESS_GREEN = "🟢";
    public static final String ERROR_RED = "🔴";
    public static final String INFO_BLUE = "🔵";
    public static final String WARN_YELLOW = "🟡";
    public static final String OK_HEART = "💚";
    public static final String CHECK = "✅";
    public static final String WARN = "⚠️";
    public static final String CRITICAL = "🚨";
    public static final String SKULL = "☠️";

    // Tools & Engineering
    public static final String WRENCH = "🔧";
    public static final String HAMMER = "🔨";
    public static final String TOOLS = "🛠️";
    public static final String CONSTRUCT = "🚧";
    public static final String MEASURING_TAPE = "📏";

    // Interaction & Collaboration
    public static final String SPEECH_BUBBLE = "💬";
    public static final String ENVELOPE = "✉️";
    public static final String LINK = "🔗";
    public static final String GLOBE = "🌐";
    public static final String WIFI = "📶";

    // Time & Metrics
    public static final String ALARM_CLOCK = "⏰";
    public static final String HOURGLASS = "⌛";
    public static final String HOURGLASS_FLOW = "⏳";
    public static final String CALENDAR = "📅";
    public static final String SEARCH = "🔍";

    // Gamification & Rewards
    public static final String TROPHY = "🏆";
    public static final String MEDAL = "🏅";
    public static final String STAR = "⭐";
    public static final String TADA = "🎉";
    public static final String HEART = "❤️";
    public static final String LIGHTBULB = "💡";

    // Weather & Elements
    public static final String SUN = "☀️";
    public static final String CLOUD = "☁️";
    public static final String RAIN = "🌧️";
    public static final String SNOW = "❄️";
    public static final String WIND = "🍃";
    public static final String TREE = "🌲";

    // Premium UI Symbols (Single-Width, 1 Column)
    public static final String INFO = "ℹ";
    public static final String CROSS = "✘";
    public static final String CHECKMARK = "✔";
    public static final String GLYPH_STAR = "✦";
    public static final String GLYPH_DOT = "•";
    public static final String GLYPH_RIGHT = "›";
    public static final String GLYPH_LEFT = "‹";

    // Box Drawing Elements (Single-Width, 1 Column)
    public static final String BOX_HORIZONTAL = "─";
    public static final String BOX_VERTICAL = "│";
    public static final String BOX_TOP_LEFT = "┌";
    public static final String BOX_TOP_RIGHT = "┐";
    public static final String BOX_BOTTOM_LEFT = "└";
    public static final String BOX_BOTTOM_RIGHT = "┘";
    
    // Rounded Corners Box Drawing
    public static final String BOX_ROUND_TOP_LEFT = "╭";
    public static final String BOX_ROUND_TOP_RIGHT = "╮";
    public static final String BOX_ROUND_BOTTOM_LEFT = "╰";
    public static final String BOX_ROUND_BOTTOM_RIGHT = "╯";

    // Double Borders Box Drawing
    public static final String BOX_DOUBLE_HORIZONTAL = "═";
    public static final String BOX_DOUBLE_VERTICAL = "║";
    public static final String BOX_DOUBLE_TOP_LEFT = "╔";
    public static final String BOX_DOUBLE_TOP_RIGHT = "╗";
    public static final String BOX_DOUBLE_BOTTOM_LEFT = "╚";
    public static final String BOX_DOUBLE_BOTTOM_RIGHT = "╝";

    // Block Elements (Single-Width, 1 Column)
    public static final String BLOCK_FULL = "█";
    public static final String BLOCK_DARK = "▓";
    public static final String BLOCK_MEDIUM = "▒";
    public static final String BLOCK_LIGHT = "░";
    
    // Partial vertical blocks
    public static final String BLOCK_LEFT_7_8 = "▉";
    public static final String BLOCK_LEFT_3_4 = "▊";
    public static final String BLOCK_LEFT_5_8 = "▋";
    public static final String BLOCK_LEFT_1_2 = "▌";
    public static final String BLOCK_LEFT_3_8 = "▍";
    public static final String BLOCK_LEFT_1_4 = "▎";
    public static final String BLOCK_LEFT_1_8 = "▏";

    // Partial horizontal blocks
    public static final String BLOCK_BOTTOM_1_8 = " ";
    public static final String BLOCK_BOTTOM_1_4 = "▂";
    public static final String BLOCK_BOTTOM_3_8 = "▃";
    public static final String BLOCK_BOTTOM_1_2 = "▄";
    public static final String BLOCK_BOTTOM_5_8 = "▅";
    public static final String BLOCK_BOTTOM_3_4 = "▆";
    public static final String BLOCK_BOTTOM_7_8 = "▇";

    // ============================================================================
    // Comprehensive Unicode East Asian Width (EAW) and Emoji Width Calculation
    // ============================================================================

    /**
     * Determines whether a Unicode codepoint renders as a double-width (2-column) emoji
     * or East Asian character, a single-width character, or a zero-width modifier.
     */
    public static int getWidth(int codepoint) {
        // 1. Zero-width characters, combining marks, and modifiers
        if (codepoint == 0x200B || // Zero-width space
            codepoint == 0x200D || // Zero-width joiner (ZWJ)
            codepoint == 0xFE0F || // Variation Selector 16 (VS16, forces emoji rendering)
            codepoint == 0xFE0E || // Variation Selector 15 (VS15, forces text rendering)
            (codepoint >= 0x0300 && codepoint <= 0x036F) || // Combining Diacritical Marks
            (codepoint >= 0x1DC0 && codepoint <= 0x1DFF) || // Combining Diacritical Marks Supplement
            (codepoint >= 0x20D0 && codepoint <= 0x20FF) || // Combining Diacritical Marks for Symbols
            (codepoint >= 0xFE20 && codepoint <= 0xFE2F)) { // Combining Half Marks
            return 0;
        }

        // 2. Explicit double-width terminal icons and status emojis
        if (codepoint == 0x26A1 || // ⚡
            codepoint == 0x26A0 || // ⚠️
            codepoint == 0x2699 || // ⚙️
            codepoint == 0x2705 || // ✅
            codepoint == 0x2728 || // ✨
            codepoint == 0x270F || // ✏️
            codepoint == 0x2709 || // ✉️
            codepoint == 0x270C || // ✌️
            codepoint == 0x2714 || // ✔️ (emoji version)
            codepoint == 0x2716 || // ✖️ (emoji version)
            codepoint == 0x274C || // ❌
            codepoint == 0x274E || // ❎
            codepoint == 0x2139 || // ℹ️ (emoji version)
            codepoint == 0x2611 || // ☑️
            codepoint == 0x2615 || // ☕
            codepoint == 0x26FD || // ⛽
            codepoint == 0x26C5 || // ⛅
            codepoint == 0x260E || // ☎️
            codepoint == 0x2764 || // ❤️
            codepoint == 0x2B50 || // ⭐
            codepoint == 0x23F0 || // ⏰
            codepoint == 0x231B || // ⌛
            codepoint == 0x23F3 || // ⏳
            codepoint == 0x2600 || // ☀️
            codepoint == 0x2601 || // ☁️
            codepoint == 0x2744 || // ❄️
            codepoint == 0x2702 || // ✂️
            codepoint == 0x2708 || // ✈️
            codepoint == 0x2620 || // ☠️
            codepoint == 0x2693 || // ⚓
            codepoint == 0x26F2 || // ⛲
            codepoint == 0x26F5 || // ⛵
            codepoint == 0x26BD || // ⚽
            codepoint == 0x26BE || // ⚾
            codepoint == 0x26F3) { // ⛳
            return 2;
        }

        // 3. East Asian Wide (W) and Fullwidth (F) Ranges
        // CJK Unified Ideographs, Hiragana, Katakana, Hangul Syllables, etc.
        if ((codepoint >= 0x1100 && codepoint <= 0x115F) || // Hangul Jamo
            (codepoint >= 0x2E80 && codepoint <= 0x303E) || // CJK Radicals & Symbols
            (codepoint >= 0x3040 && codepoint <= 0x309F) || // Hiragana
            (codepoint >= 0x30A0 && codepoint <= 0x30FF) || // Katakana
            (codepoint >= 0x3100 && codepoint <= 0x312F) || // Bopomofo
            (codepoint >= 0x31A0 && codepoint <= 0x31BF) || // Bopomofo Extended
            (codepoint >= 0x31C0 && codepoint <= 0x31EF) || // CJK Strokes
            (codepoint >= 0x3200 && codepoint <= 0x4DBF) || // Enclosed CJK, CJK Extension A
            (codepoint >= 0x4E00 && codepoint <= 0x9FFF) || // CJK Unified Ideographs
            (codepoint >= 0xAC00 && codepoint <= 0xD7A3) || // Hangul Syllables
            (codepoint >= 0xF900 && codepoint <= 0xFAFF) || // CJK Compatibility Ideographs
            (codepoint >= 0xFE10 && codepoint <= 0xFE19) || // Vertical forms
            (codepoint >= 0xFE30 && codepoint <= 0xFE6F) || // CJK Compatibility Forms
            (codepoint >= 0xFF01 && codepoint <= 0xFF60) || // Fullwidth Forms
            (codepoint >= 0xFFE0 && codepoint <= 0xFFE6) || // Fullwidth Symbol Variants
            (codepoint >= 0x20000 && codepoint <= 0x2FFFD) || // CJK Extension B/C/D/E/F
            (codepoint >= 0x30000 && codepoint <= 0x3FFFD)) {  // CJK Extension G/H
            return 2;
        }

        // 4. Standard Emoji Ranges (Misc Symbols and Pictographs, Emoticons, Transport, etc.)
        if ((codepoint >= 0x1F000 && codepoint <= 0x1F9FF) || // Standard Emojis
            (codepoint >= 0x1FA00 && codepoint <= 0x1FAFF) || // Chess Symbols, Pictographs
            (codepoint >= 0x1FC00 && codepoint <= 0x1FFFD)) {  // Supplementary Emojis
            return 2;
        }

        // 5. Default fallback to standard single column width
        return 1;
    }
}
