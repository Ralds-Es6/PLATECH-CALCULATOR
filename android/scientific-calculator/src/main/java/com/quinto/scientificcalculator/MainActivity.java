package com.quinto.scientificcalculator;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.quinto.core.MathParser;
import com.quinto.core.NeonGlassViews;
import com.quinto.core.NeonGlassViews.BtnStyle;
import com.quinto.core.NeonGlassViews.CalculatorIconView;
import com.quinto.core.NeonGlassViews.GlassButton;
import com.quinto.core.NeonGlassViews.NeonGlassLayout;

public class MainActivity extends Activity {
    private TextView statusView;
    private TextView exprView;
    private TextView displayView;
    private GlassButton modeBtn;

    private final StringBuilder expr = new StringBuilder();
    private boolean degreesMode = true;
    private boolean justEvaluated;
    private boolean hasError;
    private double memoryValue = 0;
    private double lastAnswer = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad10 = NeonGlassViews.dp(this, 10);
        int pad14 = NeonGlassViews.dp(this, 14);

        FrameLayout outer = new FrameLayout(this);
        outer.setBackgroundColor(NeonGlassViews.CANVAS_BG);
        outer.setPadding(pad10, pad10, pad10, pad10);

        NeonGlassLayout frame = new NeonGlassLayout(this);
        frame.setPadding(pad14, NeonGlassViews.dp(this, 16), pad14, NeonGlassViews.dp(this, 16));

        // Header: icon + title on left, DEG/MEM indicator on right
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        CalculatorIconView icon = new CalculatorIconView(this);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
            NeonGlassViews.dp(this, 24), NeonGlassViews.dp(this, 28));
        iconLp.rightMargin = NeonGlassViews.dp(this, 10);
        header.addView(icon, iconLp);

        TextView title = new TextView(this);
        title.setText("Calculator");
        title.setTextColor(NeonGlassViews.TEXT_WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        title.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        header.addView(title, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

        statusView = new TextView(this);
        statusView.setText("DEG");
        statusView.setTextColor(NeonGlassViews.TEXT_ACCENT);
        statusView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        statusView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        header.addView(statusView);

        frame.addView(header, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Display area
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.BOTTOM | Gravity.END);
        display.setPadding(NeonGlassViews.dp(this, 6), NeonGlassViews.dp(this, 10),
            NeonGlassViews.dp(this, 6), NeonGlassViews.dp(this, 10));

        exprView = new TextView(this);
        exprView.setText(" ");
        exprView.setTextColor(NeonGlassViews.TEXT_MUTED);
        exprView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 17);
        exprView.setGravity(Gravity.END);
        exprView.setSingleLine(true);
        exprView.setEllipsize(TextUtils.TruncateAt.START);

        displayView = new TextView(this);
        displayView.setText("0");
        displayView.setTextColor(NeonGlassViews.TEXT_WHITE);
        displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 46);
        displayView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        displayView.setGravity(Gravity.END);
        displayView.setSingleLine(true);
        displayView.setEllipsize(TextUtils.TruncateAt.START);

        display.addView(exprView);
        display.addView(displayView);

        frame.addView(display, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.0f));

        // 8x5 Scientific + Standard Grid
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

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        int gap = NeonGlassViews.dp(this, 3.5f);

        for (int r = 0; r < keys.length; r++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 5; c++) {
                String k = keys[r][c];
                if (k == null) continue;

                BtnStyle style = (c == 4 && r >= 3) ? BtnStyle.NEON
                    : (r < 3 || c == 0 || r == 3) ? BtnStyle.SCI : BtnStyle.DIGIT;
                final GlassButton btn = new GlassButton(this, k, style, r);
                if (r == 0 && c == 0) modeBtn = btn;
                btn.setOnClickListener(v -> onInput(btn.getText().toString()));

                float weight = (r == 7 && c == 1) ? 2f : 1f;
                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, weight);
                bp.setMargins(gap, gap, gap, gap);
                rowLayout.addView(btn, bp);
            }
            grid.addView(rowLayout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));
        }

        frame.addView(grid, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 3.6f));

        outer.addView(frame, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(outer);
    }

    private void onInput(String in) {
        if (hasError && !in.equals("AC")) clear();
        switch (in) {
            case "DEG": case "RAD":
                degreesMode = !degreesMode;
                modeBtn.setText(degreesMode ? "DEG" : "RAD");
                updateStatus();
                return;
            case "MC": memoryValue = 0; updateStatus(); return;
            case "MR":
                resetEvalFlag(true);
                expr.append(MathParser.toRaw(memoryValue).replace('-', '−'));
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
                expr.append(MathParser.toRaw(lastAnswer).replace('-', '−'));
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
        setDisplay(expr.length() == 0 ? "0" : MathParser.pretty(expr.toString()));
    }

    private void updateStatus() {
        String mode = degreesMode ? "DEG" : "RAD";
        statusView.setText(memoryValue != 0 ? "M  •  " + mode : mode);
    }

    private void memoryOp(int sign) {
        if (expr.length() == 0) return;
        try {
            double val = MathParser.evaluate(MathParser.autoClose(expr.toString()), degreesMode);
            if (Double.isFinite(val)) {
                memoryValue += sign * val;
                updateStatus();
            }
        } catch (Exception ignored) {}
    }

    private void clear() {
        expr.setLength(0);
        justEvaluated = hasError = false;
        exprView.setText(" ");
        setDisplay("0");
    }

    private void backspace() {
        if (hasError) { clear(); return; }
        resetEvalFlag(false);
        if (expr.length() > 0) {
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
        setDisplay(expr.length() == 0 ? "0" : MathParser.pretty(expr.toString()));
    }

    private void resetEvalFlag(boolean clearExpr) {
        if (!justEvaluated) return;
        if (clearExpr) expr.setLength(0);
        exprView.setText(" ");
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
            (expr.length() == 1 || MathParser.isOp(expr.charAt(expr.length() - 2)) || expr.charAt(expr.length() - 2) == '(');
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
        if (expr.length() == 0 || MathParser.isOp(last()) || last() == '(') expr.append("0.");
        else if (needsMultiplyBeforeDigit(last())) expr.append("×0.");
        else expr.append('.');
    }

    private static boolean needsMultiplyBeforeDigit(char c) {
        return c == ')' || c == 'π' || c == 'e' || c == '%' || c == '!';
    }

    private void insertOp(char op) {
        resetEvalFlag(false);
        if (expr.length() == 0) { if (op == '−') expr.append('−'); return; }
        if (MathParser.isOp(last())) expr.setCharAt(expr.length() - 1, op);
        else if (last() != '(' || op == '−') expr.append(op);
    }

    private void insertParen() {
        resetEvalFlag(true);
        int open = MathParser.openParenDelta(expr);
        if (expr.length() == 0 || MathParser.isOp(last()) || last() == '(') expr.append('(');
        else expr.append(open > 0 ? ')' : '(');
    }

    private void evaluate() {
        if (expr.length() == 0) return;
        String full = MathParser.autoClose(expr.toString());
        exprView.setText(MathParser.pretty(full));
        try {
            double res = MathParser.evaluate(full, degreesMode);
            if (!Double.isFinite(res)) throw new IllegalStateException();
            if (Math.abs(res) < 1e-12) res = 0;
            lastAnswer = res;
            setDisplay(MathParser.formatResult(res));
            expr.setLength(0);
            expr.append(MathParser.toRaw(res).replace('-', '−'));
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

    private static boolean canFollowPostfix(char c) {
        return Character.isDigit(c) || c == ')' || c == 'π' || c == 'e' || c == '%' || c == '!';
    }

    private void setDisplay(String text) {
        displayView.setText(text);
        int len = text.length();
        float sp = len <= 9 ? 46f : len <= 13 ? 36f : len <= 18 ? 28f : len <= 24 ? 22f : 18f;
        displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }
}

