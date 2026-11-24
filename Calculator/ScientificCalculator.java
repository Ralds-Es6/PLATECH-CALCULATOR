import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;

public class ScientificCalculator extends JFrame {
    private static final Color CANVAS_BG = new Color(14, 16, 24);
    private static final Color TEXT_WHITE = new Color(248, 248, 255);
    private static final Color TEXT_MUTED = new Color(176, 180, 202);
    private static final DecimalFormat SCI_FMT = new DecimalFormat("0.######E0");
    private static final MathContext PRECISION = new MathContext(12, RoundingMode.HALF_UP);
    private static final Font[] DISPLAY_FONTS = {
        new Font("SansSerif", Font.BOLD, 44), new Font("SansSerif", Font.BOLD, 34),
        new Font("SansSerif", Font.BOLD, 26), new Font("SansSerif", Font.BOLD, 21),
        new Font("SansSerif", Font.BOLD, 17)
    };

    private final JLabel statusLabel = new JLabel("DEG");
    private final JLabel exprLabel = new JLabel(" ");
    private final JLabel displayLabel = new JLabel("0");
    private final StringBuilder expr = new StringBuilder();

    private GlassButton modeBtn;
    private boolean degreesMode = true;
    private boolean justEvaluated, hasError;
    private double memoryValue = 0;
    private double lastAnswer = 0;

    public ScientificCalculator() {
        setTitle("Calculator");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(520, 700);
        setMinimumSize(new Dimension(460, 620));
        setLocationRelativeTo(null);

        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(CANVAS_BG);
        outer.setBorder(new EmptyBorder(12, 12, 12, 12));

        NeonGlassPanel frame = new NeonGlassPanel();
        frame.setLayout(new BorderLayout(0, 10));
        frame.setBorder(new EmptyBorder(18, 20, 20, 20));

        // Clean header: icon + title on left, subtle DEG/MEM indicator on right
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        titleGroup.setOpaque(false);
        titleGroup.add(new CalculatorIcon());
        JLabel title = new JLabel("Calculator");
        title.setFont(new Font("SansSerif", Font.BOLD, 16));
        title.setForeground(TEXT_WHITE);
        titleGroup.add(title);

        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        statusLabel.setForeground(new Color(186, 140, 255));
        statusLabel.setBorder(new EmptyBorder(0, 0, 0, 6));

        header.add(titleGroup, BorderLayout.WEST);
        header.add(statusLabel, BorderLayout.EAST);

        // Seamless display
        JPanel display = new JPanel();
        display.setOpaque(false);
        display.setLayout(new BoxLayout(display, BoxLayout.Y_AXIS));
        display.setBorder(new EmptyBorder(14, 6, 10, 6));

        exprLabel.setAlignmentX(RIGHT_ALIGNMENT);
        exprLabel.setFont(new Font("SansSerif", Font.PLAIN, 16));
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

        // 8x5 Scientific + Standard Grid
        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.BOTH;
        g.weightx = g.weighty = 1.0;
        g.insets = new Insets(4, 4, 4, 4);

        String[][] keys = {
            {"DEG", "MC", "MR",  "M+", "M−"},
            {"sin", "cos", "tan", "ln", "log"},
            {"x²",  "xʸ",  "√",   "π",  "e"},
            {"AC",  "( )", "DEL", "%",  "÷"},
            {"n!",  "7",   "8",   "9",  "×"},
            {"1/x", "4",   "5",   "6",  "−"},
            {"±",   "1",   "2",   "3",  "+"},
            {"ANS", "0",   null,  ".",  "="}
        };

        for (int r = 0; r < keys.length; r++) {
            for (int c = 0; c < 5; c++) {
                String k = keys[r][c];
                if (k == null) continue;
                BtnStyle style = (c == 4 && r >= 3) ? BtnStyle.NEON
                    : (r < 3 || c == 0 || r == 3) ? BtnStyle.SCI : BtnStyle.DIGIT;
                GlassButton btn = new GlassButton(k, style, r);
                if (r == 0 && c == 0) modeBtn = btn;
                btn.addActionListener(e -> onInput(btn.getText()));
                g.gridx = c; g.gridy = r;
                g.gridwidth = (r == 7 && c == 1) ? 2 : 1;
                grid.add(btn, g);
            }
        }

        frame.add(grid, BorderLayout.CENTER);

        JLabel footer = new JLabel("QUINTO, GERALD L. PLATECH", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(TEXT_MUTED);
        footer.setBorder(new EmptyBorder(4, 0, 0, 0));
        frame.add(footer, BorderLayout.SOUTH);

        outer.add(frame, BorderLayout.CENTER);
        setContentPane(outer);
        bindKeyboard();
    }

    private void bindKeyboard() {
        setFocusable(true);
        addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (Character.isDigit(c) || c == '.' || c == '+' || c == '%' || c == '^') onInput(c == '^' ? "xʸ" : String.valueOf(c));
                else if (c == '-') onInput("−");
                else if (c == '*') onInput("×");
                else if (c == '/') onInput("÷");
                else if (c == '!') onInput("n!");
                else if (c == '(' || c == ')') onInput("( )");
                else if (c == '=' || c == '\n') onInput("=");
                else if (c == 'c' || c == 'C') onInput("AC");
            }
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE || e.getKeyCode() == KeyEvent.VK_DELETE) onInput("DEL");
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) onInput("AC");
                else if (e.getKeyCode() == KeyEvent.VK_ENTER) onInput("=");
            }
        });
    }

    private void onInput(String in) {
        if (hasError && !in.equals("AC")) clear();
        switch (in) {
            case "DEG": case "RAD":
                degreesMode = !degreesMode;
                modeBtn.setText(degreesMode ? "DEG" : "RAD");
                updateStatus();
                if (!justEvaluated) refreshLive();
                return;
            case "MC": memoryValue = 0; updateStatus(); return;
            case "MR":
                resetEvalFlag(true);
                expr.append(toRaw(memoryValue).replace('-', '−'));
                break;
            case "M+": memoryOp(1); return;
            case "M−": memoryOp(-1); return;
            case "AC": clear(); return;
            case "DEL": backspace(); return;
            case "( )": insertParen(); break;
            case "sin": case "cos": case "tan": case "ln": case "log": case "√":
                resetEvalFlag(true);
                expr.append(in).append('(');
                break;
            case "π": case "e":
                resetEvalFlag(true);
                expr.append(in);
                break;
            case "ANS":
                resetEvalFlag(true);
                expr.append(toRaw(lastAnswer).replace('-', '−'));
                break;
            case "x²":
                resetEvalFlag(false);
                if (expr.length() > 0 && canFollowPostfix(last())) expr.append("^2");
                break;
            case "xʸ":
                resetEvalFlag(false);
                if (expr.length() > 0 && canFollowPostfix(last())) expr.append('^');
                break;
            case "n!":
                resetEvalFlag(false);
                if (expr.length() > 0 && canFollowPostfix(last())) expr.append('!');
                break;
            case "%":
                resetEvalFlag(false);
                if (expr.length() > 0 && canFollowPostfix(last())) expr.append('%');
                break;
            case "1/x":
                if (expr.length() > 0) {
                    resetEvalFlag(false);
                    expr.insert(0, "1÷(").append(')');
                }
                break;
            case "±":
                toggleSign();
                break;
            case "+": case "−": case "×": case "÷":
                insertOp(in.charAt(0));
                break;
            case ".": insertDot(); break;
            case "=": evaluate(); return;
            default: insertDigit(in.charAt(0)); break;
        }
        refreshLive();
    }

    private void updateStatus() {
        String mode = degreesMode ? "DEG" : "RAD";
        statusLabel.setText(memoryValue != 0 ? "M  •  " + mode : mode);
    }

    private void memoryOp(int sign) {
        if (expr.length() == 0) return;
        try {
            double val = new ScientificParser(autoClose(expr.toString()), degreesMode).parse();
            if (Double.isFinite(val)) {
                memoryValue += sign * val;
                updateStatus();
            }
        } catch (Exception ignored) {}
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
        if (expr.length() > 0) {
            // Delete whole function token like "sin(" cleanly
            String s = expr.toString();
            String[] funcs = {"sin(", "cos(", "tan(", "log(", "ln(", "√("};
            boolean removed = false;
            for (String fn : funcs) {
                if (s.endsWith(fn)) {
                    expr.setLength(expr.length() - fn.length());
                    removed = true;
                    break;
                }
            }
            if (!removed) expr.deleteCharAt(expr.length() - 1);
        }
        refreshLive();
    }

    private void refreshLive() {
        if (expr.length() == 0) {
            exprLabel.setText(" ");
            setDisplay("0");
            return;
        }
        String raw = expr.toString();
        setDisplay(pretty(raw));
        boolean hasExpr = false;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (isOp(c) || c == '%' || c == '!' || c == '(' || c == 'π' || c == 'e' || c == '√' || Character.isLetter(c)) {
                hasExpr = true;
                break;
            }
        }
        if (hasExpr) {
            try {
                double res = new ScientificParser(autoClose(raw), degreesMode).parse();
                if (Double.isFinite(res)) {
                    exprLabel.setText("= " + formatResult(res));
                    return;
                }
            } catch (Exception ignored) {}
        }
        exprLabel.setText(" ");
    }

    private void resetEvalFlag(boolean clearExpr) {
        if (!justEvaluated) return;
        if (clearExpr) expr.setLength(0);
        exprLabel.setText(" ");
        justEvaluated = false;
    }

    private void toggleSign() {
        if (expr.length() == 0) { expr.append('−'); return; }
        resetEvalFlag(false);
        if (expr.charAt(0) == '−') expr.deleteCharAt(0);
        else expr.insert(0, '−');
    }

    private void insertDigit(char d) {
        resetEvalFlag(true);
        if (expr.length() > 0 && needsMultiplyBeforeDigit(last())) expr.append('×');
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
        for (int i = expr.length() - 1; i >= 0 && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.'); i--)
            if (expr.charAt(i) == '.') return;
        if (expr.length() == 0 || isOp(last()) || last() == '(') expr.append("0.");
        else if (needsMultiplyBeforeDigit(last())) expr.append("×0.");
        else expr.append('.');
    }

    private static boolean needsMultiplyBeforeDigit(char c) {
        return c == ')' || c == 'π' || c == 'e' || c == '%' || c == '!';
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
        else expr.append(open > 0 ? ')' : '(');
    }

    private String autoClose(String raw) {
        StringBuilder full = new StringBuilder(raw);
        long open = raw.chars().filter(c -> c == '(').count() - raw.chars().filter(c -> c == ')').count();
        while (open-- > 0) full.append(')');
        return full.toString();
    }

    private void evaluate() {
        if (expr.length() == 0) return;
        String full = autoClose(expr.toString());
        exprLabel.setText(pretty(full));
        try {
            double res = new ScientificParser(full, degreesMode).parse();
            if (!Double.isFinite(res)) throw new IllegalStateException();
            if (Math.abs(res) < 1e-12) res = 0; // clean trig rounding like sin(180°)
            lastAnswer = res;
            setDisplay(formatResult(res));
            expr.setLength(0);
            expr.append(toRaw(res).replace('-', '−'));
            justEvaluated = true;
        } catch (ArithmeticException ex) {
            setDisplay(ex.getMessage() != null ? ex.getMessage() : "Math Error");
            hasError = true;
        } catch (Exception ex) {
            setDisplay("Error");
            hasError = true;
        }
    }

    private char last() { return expr.charAt(expr.length() - 1); }
    private static boolean isOp(char c) { return c == '+' || c == '−' || c == '×' || c == '÷' || c == '^'; }
    private static boolean canFollowPostfix(char c) {
        return Character.isDigit(c) || c == ')' || c == 'π' || c == 'e' || c == '%' || c == '!';
    }

    private void setDisplay(String text) {
        displayLabel.setText(text);
        int len = text.length();
        int idx = len <= 10 ? 0 : len <= 14 ? 1 : len <= 19 ? 2 : len <= 25 ? 3 : 4;
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
            out.append((c == '+' || c == '−' || c == '×' || c == '÷') && !unary ? " " + c + " " : c);
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

    // Scientific recursive-descent parser (precedence, trig, logs, roots, powers, factorial, implicit multiply)
    private static class ScientificParser {
        private final String s;
        private final boolean deg;
        private int p;

        ScientificParser(String raw, boolean deg) {
            this.s = raw.replace('−', '-').replace('×', '*').replace('÷', '/');
            this.deg = deg;
        }

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
            double x = power();
            while (p < s.length()) {
                char c = s.charAt(p);
                if (c == '*' || c == '/') {
                    p++;
                    double y = power();
                    if (c == '/' && y == 0) throw new ArithmeticException("Cannot divide by 0");
                    x = c == '*' ? x * y : x / y;
                } else if (c == '(' || c == 'π' || c == 'e' || c == '√' || Character.isLetter(c)) {
                    x *= power(); // implicit multiplication e.g. 2π, 2(3+4), 2sin(30)
                } else break;
            }
            return x;
        }

        private double power() {
            double x = unary();
            if (p < s.length() && s.charAt(p) == '^') {
                p++;
                x = Math.pow(x, power()); // right-associative
            }
            return x;
        }

        private double unary() {
            if (p < s.length() && (s.charAt(p) == '+' || s.charAt(p) == '-'))
                return s.charAt(p++) == '-' ? -unary() : unary();
            double x = primary();
            while (p < s.length() && (s.charAt(p) == '%' || s.charAt(p) == '!')) {
                char c = s.charAt(p++);
                x = c == '%' ? x / 100.0 : factorial(x);
            }
            return x;
        }

        private double primary() {
            if (p >= s.length()) throw new IllegalArgumentException();
            char c = s.charAt(p);
            if (c == '(') {
                p++;
                double x = addSub();
                if (p < s.length() && s.charAt(p) == ')') p++;
                return x;
            }
            if (c == 'π') { p++; return Math.PI; }
            if (c == 'e') { p++; return Math.E; }
            if (c == '√') {
                p++;
                double x = primary();
                if (x < 0) throw new ArithmeticException("Invalid root");
                return Math.sqrt(x);
            }
            if (Character.isLetter(c)) {
                int start = p;
                while (p < s.length() && Character.isLetter(s.charAt(p))) p++;
                String fn = s.substring(start, p);
                double arg = primary();
                return applyFunc(fn, arg);
            }
            int start = p;
            while (p < s.length() && (Character.isDigit(s.charAt(p)) || s.charAt(p) == '.')) p++;
            if (start == p) throw new IllegalArgumentException();
            return Double.parseDouble(s.substring(start, p));
        }

        private double applyFunc(String fn, double x) {
            double rad = deg ? Math.toRadians(x) : x;
            switch (fn) {
                case "sin": return Math.sin(rad);
                case "cos": return Math.cos(rad);
                case "tan":
                    if (deg && Math.abs((x % 180) - 90) < 1e-9 || deg && Math.abs((x % 180) + 90) < 1e-9)
                        throw new ArithmeticException("Undefined");
                    return Math.tan(rad);
                case "ln":
                    if (x <= 0) throw new ArithmeticException("Domain error");
                    return Math.log(x);
                case "log":
                    if (x <= 0) throw new ArithmeticException("Domain error");
                    return Math.log10(x);
                default: throw new IllegalArgumentException();
            }
        }

        private static double factorial(double x) {
            if (x < 0 || x != Math.floor(x) || x > 170) throw new ArithmeticException("Domain error");
            double r = 1;
            for (int i = 2; i <= (int) x; i++) r *= i;
            return r;
        }
    }

    private enum BtnStyle { DIGIT, SCI, NEON }

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
            ribbon.moveTo(0, 95); ribbon.quadTo(90, 45, 220, 12);
            ribbon.lineTo(220, 52); ribbon.quadTo(85, 95, 0, 158); ribbon.closePath();
            g2.setPaint(new GradientPaint(0, 130, new Color(185, 55, 215, 120), 190, 35, new Color(90, 40, 160, 0)));
            g2.fill(ribbon);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(35, 85), 160f, new float[]{0f, 1f},
                new Color[]{new Color(165, 58, 225, 60), new Color(165, 58, 225, 0)}));
            g2.fillRect(0, 0, w, h / 2);
            g2.setPaint(new RadialGradientPaint(new Point2D.Float(w - 40, h - 35), 210f, new float[]{0f, 1f},
                new Color[]{new Color(72, 108, 255, 80), new Color(72, 108, 255, 0)}));
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

    private static class GlassButton extends JButton {
        private static final BasicStroke RIM = new BasicStroke(1.1f);
        private static final Font DIGIT_FONT = new Font("SansSerif", Font.PLAIN, 19);
        private static final Font SCI_FONT = new Font("SansSerif", Font.PLAIN, 15);
        private static final Font OP_FONT = new Font("SansSerif", Font.BOLD, 23);

        private final BtnStyle style;
        private final Color baseTop, baseBot;
        private boolean hovered, pressed;

        GlassButton(String text, BtnStyle style, int row) {
            super(text);
            this.style = style;
            if (style == BtnStyle.NEON) {
                baseTop = row == 5 ? new Color(156, 72, 255) : row < 5 ? new Color(198, 86, 255) : new Color(188, 78, 255);
                baseBot = row == 5 ? new Color(118, 52, 238) : row < 5 ? new Color(142, 62, 245) : new Color(138, 60, 248);
            } else if (style == BtnStyle.SCI) {
                baseTop = new Color(34, 34, 56, 210);
                baseBot = new Color(24, 24, 42, 225);
            } else {
                baseTop = new Color(46, 46, 72, 225);
                baseBot = new Color(32, 32, 52, 240);
            }
            setContentAreaFilled(false); setFocusPainted(false); setBorderPainted(false);
            setOpaque(false); setFocusable(false);
            setForeground(style == BtnStyle.SCI ? new Color(218, 214, 248) : TEXT_WHITE);
            setFont(style == BtnStyle.NEON ? OP_FONT : style == BtnStyle.SCI ? SCI_FONT : DIGIT_FONT);
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
            int w = getWidth(), h = getHeight(), pad = style == BtnStyle.NEON ? 3 : 1;
            Shape rect = new RoundRectangle2D.Double(pad, pad, w - pad * 2, h - pad * 2, 18, 18);

            Color t = baseTop, b = baseBot;
            if (style == BtnStyle.NEON) {
                g2.setColor(new Color(172, 72, 255, hovered ? 70 : 45));
                for (int i = 2; i >= 1; i--)
                    g2.fill(new RoundRectangle2D.Double(pad - i, pad - i, w - (pad - i) * 2, h - (pad - i) * 2, 20, 20));
                if (pressed) { t = t.darker(); b = b.darker(); }
                else if (hovered) { t = brighten(t, 32); b = brighten(b, 24); }
            } else if (pressed) {
                t = new Color(24, 24, 40, 235); b = new Color(18, 18, 32, 245);
            } else if (hovered) {
                t = new Color(62, 62, 94, 230); b = new Color(42, 42, 68, 240);
            }

            g2.setPaint(new GradientPaint(0, pad, t, 0, h - pad, b));
            g2.fill(rect);
            g2.setStroke(RIM);
            g2.setPaint(style == BtnStyle.NEON
                ? new Color(242, 210, 255, 115)
                : new GradientPaint(0, pad, new Color(175, 165, 225, 72), 0, h - pad, new Color(95, 95, 145, 35)));
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
        SwingUtilities.invokeLater(() -> new ScientificCalculator().setVisible(true));
    }
}

