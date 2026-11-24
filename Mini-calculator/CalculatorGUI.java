import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class CalculatorGUI extends JFrame {
    private static final Color CANVAS_BG = new Color(14, 16, 24);
    private static final Color TEXT_WHITE = new Color(248, 248, 255);
    private static final Color TEXT_MUTED = new Color(176, 180, 202);
    private static final DecimalFormat SCI_FMT = new DecimalFormat("0.######E0");
    private static final MathContext PRECISION = new MathContext(12, RoundingMode.HALF_UP);
    private static final Font[] DISPLAY_FONTS = {
        new Font("SansSerif", Font.BOLD, 48), new Font("SansSerif", Font.BOLD, 38),
        new Font("SansSerif", Font.BOLD, 30), new Font("SansSerif", Font.BOLD, 24),
        new Font("SansSerif", Font.BOLD, 20)
    };

    private final JLabel exprLabel = new JLabel(" ");
    private final JLabel displayLabel = new JLabel("0");
    private final StringBuilder expr = new StringBuilder();
    private boolean justEvaluated, hasError;

    public CalculatorGUI() {
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(460, 620);
        setMinimumSize(new Dimension(400, 540));
        setLocationRelativeTo(null);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(CANVAS_BG);
        outer.setBorder(new EmptyBorder(12, 12, 12, 12));

        NeonGlassPanel frame = new NeonGlassPanel();
        frame.setLayout(new BorderLayout(0, 10));
        frame.setBorder(new EmptyBorder(20, 20, 22, 20));

        // Header: icon + title only
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        header.setOpaque(false);
        header.add(new CalculatorIcon());
        JLabel title = new JLabel("Calculator");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        header.add(title);

        // Seamless display
        JPanel display = new JPanel();
        display.setOpaque(false);
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        display.setBorder(new EmptyBorder(24, 6, 16, 6));
        exprLabel.setAlignmentX(RIGHT_ALIGNMENT);
        exprLabel.setFont(new Font("SansSerif", Font.PLAIN, 18));
        exprLabel.setForeground(TEXT_MUTED);
        displayLabel.setAlignmentX(RIGHT_ALIGNMENT);
        displayLabel.setFont(DISPLAY_FONTS[0]);
        displayLabel.setForeground(TEXT_WHITE);
        display.add(exprLabel);
        display.add(Box.createVerticalStrut(4));
        display.add(displayLabel);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(header, BorderLayout.NORTH);
        top.add(display, BorderLayout.CENTER);
        frame.add(top, BorderLayout.NORTH);

        // 5x4 GridBagLayout with wide '0' button
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weightx = g.weighty = 1.0;
        g.insets = new Insets(5, 5, 5, 5);

        String[][] keys = {
            {"AC", "( )", "%", "÷"}, {"7", "8", "9", "×"},
            {"4", "5", "6", "−"},   {"1", "2", "3", "+"},
            {"0", ".", "DEL", "="}
        };
        for (int r = 0; r < keys.length; r++) {
            for (int c = 0; c < 4; c++) {
                String k = keys[r][c];
                GlassButton btn = new GlassButton(k, c == 3, r);
                btn.addActionListener(e -> onInput(k));
                g.gridx = c; g.gridy = r;
                g.gridwidth = 1;
                grid.add(btn, g);
            }
        }

        frame.add(grid, BorderLayout.CENTER);
        outer.add(frame, BorderLayout.CENTER);
        setContentPane(outer);
        bindKeyboard();
    }

    private void bindKeyboard() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c) || c == '.' || c == '+' || c == '%') onInput(String.valueOf(c));
                else if (c == '-') onInput("−");
                else if (c == '*') onInput("×");
                else if (c == '/') onInput("÷");
                else if (c == '(' || c == ')') onInput("( )");
                else if (c == '=' || c == '\n') onInput("=");
                else if (c == 'c' || c == 'C') onInput("AC");
            }
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) backspace();
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) onInput("AC");
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) onInput("=");
            }
        });
    }

    private void onInput(String in) {
        if (hasError && !in.equals("AC")) clear();
        switch (in) {
            case "AC": clear(); return;
            case "DEL": backspace(); return;
            case "( )": insertParen(); break;
            case "%":
                if (expr.length() > 0 && (Character.isDigit(last()) || last() == ')')) {
                    resetEvalFlag(false);
                    expr.append('%');
                }
                break;
            case "+": case "−": case "×": case "÷": insertOp(in.charAt(0)); break;
            case ".": insertDot(); break;
            case "=": evaluate(); return;
            default: insertDigit(in.charAt(0)); break;
        }
        setDisplay(expr.length() == 0 ? "0" : pretty(expr.toString()));
    }

    private void clear() {
        expr.setLength(0);
        justEvaluated = hasError = false;
        exprLabel.setText(" ");
        setDisplay("0");
    }

    private void backspace() {
        if (hasError) { clear(); return; }
        resetEvalFlag(false);
        if (expr.length() > 0) expr.deleteCharAt(expr.length() - 1);
        setDisplay(expr.length() == 0 ? "0" : pretty(expr.toString()));
    }

    private void resetEvalFlag(boolean clearExpr) {
        if (!justEvaluated) return;
        if (clearExpr) expr.setLength(0);
        exprLabel.setText(" ");
        justEvaluated = false;
    }

    private void insertDigit(char d) {
        resetEvalFlag(true);
        if (expr.length() > 0 && last() == ')') expr.append('×');
        boolean singleZero = expr.length() > 0 && last() == '0' &&
            (expr.length() == 1 || isOp(expr.charAt(expr.length() - 2)) || expr.charAt(expr.length() - 2) == '(');
        if (singleZero) {
            if (d == '0') return;
            expr.deleteCharAt(expr.length() - 1);
        }
        expr.append(d);
    }

    private void insertDot() {
        resetEvalFlag(true);
        for (int i = expr.length() - 1; i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.'); i--) {
            if (expr.charAt(i) == '.') return;
        }
        if (expr.length() == 0 || isOp(last()) || last() == '(') expr.append("0.");
        else if (last() == ')') expr.append("×0.");
        else expr.append('.');
    }

    private void insertOp(char op) {
        resetEvalFlag(false);
        if (expr.length() == 0) { if (op == '−') expr.append('−'); return; }
        if (isOp(last())) expr.setCharAt(expr.length() - 1, op);
        else if (last() != '(' || op == '−') expr.append(op);
    }

    private void insertParen() {
        resetEvalFlag(true);
        long open = expr.chars().filter(c -> c == '(').count() - expr.chars().filter(c -> c == ')').count();
        if (expr.length() == 0 || isOp(last()) || last() == '(') expr.append('(');
        else expr.append(open > 0 ? ")" : "×(");
    }

    private void evaluate() {
        if (expr.length() == 0) return;
        StringBuilder full = new StringBuilder(expr);
        long open = expr.chars().filter(c -> c == '(').count() - expr.chars().filter(c -> c == ')').count();
        while (open-- > 0) full.append(')');

        String shown = pretty(full.toString());
        exprLabel.setText(shown);
        try {
            double res = new ExprParser(full.toString()).parse();
            if (!Double.isFinite(res)) throw new IllegalStateException();
            setDisplay(formatResult(res));
            expr.setLength(0);
            expr.append(toRaw(res).replace('-', '−'));
            justEvaluated = true;
        } catch (ArithmeticException ex) {
            setDisplay("Cannot divide by 0");
            hasError = true;
        } catch (Exception ex) {
            setDisplay("Error");
            hasError = true;
        }
    }

    private char last() { return expr.charAt(expr.length() - 1); }
    private static boolean isOp(char c) { return c == '+' || c == '−' || c == '×' || c == '÷'; }

    private void setDisplay(String text) {
        displayLabel.setText(text);
        int len = text.length();
        int idx = len <= 8 ? 0 : len <= 11 ? 1 : len <= 15 ? 2 : len <= 20 ? 3 : 4;
        displayLabel.setFont(DISPLAY_FONTS[idx]);
    }

    private static String toRaw(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        BigDecimal bd = BigDecimal.valueOf(v).round(PRECISION).stripTrailingZeros();
        return bd.compareTo(BigDecimal.ZERO) == 0 ? "0" : bd.toPlainString();
    }

    private static String formatResult(double v) {
        if (Math.abs(v) < 1e-12) return "0";
        if (Math.abs(v) >= 1e12 || Math.abs(v) < 1e-7) return SCI_FMT.format(v);
        String raw = toRaw(v);
        return raw.startsWith("-") ? "−" + addCommas(raw.substring(1)) : addCommas(raw);
    }

    private static String pretty(String s) {
        StringBuilder out = new StringBuilder(), num = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.') { num.append(c); continue; }
            if (num.length() > 0) { out.append(addCommas(num.toString())); num.setLength(0); }
            boolean unary = c == '−' && (i == 0 || s.charAt(i - 1) == '(' || isOp(s.charAt(i - 1)));
            out.append(isOp(c) && !unary ? " " + c + " " : c);
        }
        if (num.length() > 0) out.append(addCommas(num.toString()));
        return out.toString();
    }

    private static String addCommas(String n) {
        int dot = n.indexOf('.');
        String whole = dot < 0 ? n : n.substring(0, dot);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < whole.length(); i++) {
            if (i > 0 && (whole.length() - i) % 3 == 0) b.append(',');
            b.append(whole.charAt(i));
        }
        return dot < 0 ? b.toString() : b.append(n.substring(dot)).toString();
    }

    // Compact recursive-descent expression parser
    private static class ExprParser {
        private final String s;
        private int p;
        ExprParser(String raw) { this.s = raw.replace('−', '-').replace('×', '*').replace('÷', '/'); }

        double parse() {
            double v = addSub();
            if (p < s.length()) throw new IllegalArgumentException();
            return v;
        }
        private double addSub() {
            double x = mulDiv();
            while (p < s.length() && (s.charAt(p) == '+' || s.charAt(p) == '-'))
                x = s.charAt(p++) == '+' ? x + mulDiv() : x - mulDiv();
            return x;
        }
        private double mulDiv() {
            double x = unary();
            while (p < s.length() && (s.charAt(p) == '*' || s.charAt(p) == '/')) {
                char op = s.charAt(p++);
                double y = unary();
                if (op == '/' && y == 0) throw new ArithmeticException();
                x = op == '*' ? x * y : x / y;
            }
            return x;
        }
        private double unary() {
            if (p < s.length() && (s.charAt(p) == '+' || s.charAt(p) == '-'))
                return s.charAt(p++) == '-' ? -unary() : unary();
            double x = primary();
            while (p < s.length() && s.charAt(p) == '%') { p++; x /= 100.0; }
            return x;
        }
        private double primary() {
            if (p < s.length() && s.charAt(p) == '(') {
                p++;
                double x = addSub();
                if (p < s.length() && s.charAt(p) == ')') p++;
                return x;
            }
            int start = p;
            while (p < s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p) == '.')) p++;
            if (start == p) throw new IllegalArgumentException();
            return Double.parseDouble(s.substring(start, p));
        }
    }

    // Header mini calculator icon
    private static class CalculatorIcon extends JComponent {
        private static final Color TOP = new Color(224, 92, 245), BOT = new Color(156, 62, 240), INK = new Color(28, 16, 44);
        CalculatorIcon() { setPreferredSize(new Dimension(24, 28)); setOpaque(false); }
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, TOP, 24, 28, BOT));
            g2.fill(new RoundRectangle2D.Double(1, 2, 22, 24, 7, 7));
            g2.setColor(INK);
            g2.fill(new RoundRectangle2D.Double(5, 6, 14, 5, 2, 2));
            for (int r = 0; r < 2; r++)
                for (int c = 0; c < 3; c++)
                    g2.fill(new RoundRectangle2D.Double(5 + c * 5, 14 + r * 5, 3.5, 3.5, 1.5, 1.5));
            g2.dispose();
        }
    }

    // Neon Glass frame with gradient border & ambient glows
    private static class NeonGlassPanel extends JPanel {
        private static final float[] STOPS = {0f, 0.28f, 0.55f, 0.80f, 1f};
        private static final Color[] BORDER_COLS = {
            new Color(64, 212, 255), new Color(186, 85, 255),
            new Color(224, 92, 240), new Color(108, 92, 255), new Color(64, 184, 255)
        };
        private static final BasicStroke[] GLOW_STROKES = {
            new BasicStroke(8.8f), new BasicStroke(6.6f), new BasicStroke(4.4f), new BasicStroke(2f)
        };
        NeonGlassPanel() { setOpaque(false); }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();
            Shape card = new RoundRectangle2D.Double(4, 4, w - 8, h - 8, 34, 34);

            g2.setPaint(new GradientPaint(0, 0, new Color(16, 17, 28), w, h, new Color(11, 13, 22)));
            g2.fill(card);

            Shape clip = g2.getClip();
            g2.setClip(card);
            GeneralPath ribbon = new GeneralPath();
            ribbon.moveTo(0, 95); ribbon.quadTo(80, 45, 175, 15);
            ribbon.lineTo(175, 55); ribbon.quadTo(75, 95, 0, 158); ribbon.closePath();
            g2.setPaint(new GradientPaint(0, 130, new Color(185, 55, 215, 125), 160, 40, new Color(90, 40, 160, 0)));
            g2.fill(ribbon);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(35, 85), 140f, new float[]{0f, 1f},
                new Color[]{new Color(165, 58, 225, 65), new Color(165, 58, 225, 0)}));
            g2.fillRect(0, 0, w, h / 2);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w - 40, h - 35), 180f, new float[]{0f, 1f},
                new Color[]{new Color(72, 108, 255, 85), new Color(72, 108, 255, 0)}));
            g2.fillRect(0, h / 2, w, h / 2);
            g2.setClip(clip);

            g2.setPaint(new LinearGradientPaint(0, 0, w, h, STOPS, BORDER_COLS));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.14f));
            for (int i = 0; i < 3; i++) { g2.setStroke(GLOW_STROKES[i]); g2.draw(card); }
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
            g2.setStroke(GLOW_STROKES[3]);
            g2.draw(card);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // Frosted glass & glowing neon operator buttons
    private static class GlassButton extends JButton {
        private static final BasicStroke RIM = new BasicStroke(1.15f);
        private static final Font DIGIT_FONT = new Font("SansSerif", Font.PLAIN, 21);
        private static final Font OP_FONT = new Font("SansSerif", Font.BOLD, 26);

        private final boolean neon;
        private final Color baseTop, baseBot;
        private boolean hovered, pressed;

        GlassButton(String text, boolean neon, int row) {
            super(text);
            this.neon = neon;
            if (neon) {
                baseTop = row == 2 ? new Color(156, 72, 255) : row < 2 ? new Color(198, 86, 255) : new Color(188, 78, 255);
                baseBot = row == 2 ? new Color(118, 52, 238) : row < 2 ? new Color(142, 62, 245) : new Color(138, 60, 248);
            } else {
                baseTop = new Color(40, 40, 62, 220);
                baseBot = new Color(28, 28, 46, 235);
            }
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setOpaque(false); setFocusable(false);
            setForeground(TEXT_WHITE);
            setFont(neon ? OP_FONT : DIGIT_FONT);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                public void mouseEntered(MouseEvent e)  { hovered = true; repaint(); }
                public void mouseExited(MouseEvent e)   { hovered = pressed = false; repaint(); }
                public void mousePressed(MouseEvent e)  { pressed = true; repaint(); }
                public void mouseReleased(MouseEvent e) { pressed = false; repaint(); }
            });
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight(), pad = neon ? 3 : 1;
            Shape rect = new RoundRectangle2D.Double(pad, pad, w - pad * 2, h - pad * 2, 22, 22);

            Color t = baseTop, b = baseBot;
            if (neon) {
                g2.setColor(new Color(172, 72, 255, hovered ? 70 : 48));
                for (int i = 2; i >= 1; i--)
                    g2.fill(new RoundRectangle2D.Double(pad - i, pad - i, w - (pad - i) * 2, h - (pad - i) * 2, 24, 24));
                if (pressed) { t = t.darker(); b = b.darker(); }
                else if (hovered) { t = brighten(t, 32); b = brighten(b, 24); }
            } else if (pressed) {
                t = new Color(26, 26, 42, 235); b = new Color(20, 20, 34, 245);
            } else if (hovered) {
                t = new Color(56, 56, 86, 230); b = new Color(38, 38, 62, 240);
            }

            g2.setPaint(new GradientPaint(0, pad, t, 0, h - pad, b));
            g2.fill(rect);
            g2.setStroke(RIM);
            g2.setPaint(neon
                ? new Color(242, 210, 255, 115)
                : new GradientPaint(0, pad, new Color(175, 165, 225, 78), 0, h - pad, new Color(95, 95, 145, 38)));
            g2.draw(rect);
            g2.dispose();
            super.paintComponent(g);
        }

        private static Color brighten(Color c, int d) {
            return new Color(Math.min(255, c.getRed() + d), Math.min(255, c.getGreen() + d), Math.min(255, c.getBlue() + d));
        }
    }

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        SwingUtilities.invokeLater(() -> new CalculatorGUI().setVisible(true));
    }
}
