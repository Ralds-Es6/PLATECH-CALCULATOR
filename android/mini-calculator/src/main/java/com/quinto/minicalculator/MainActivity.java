package com.quinto.minicalculator;

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
    private TextView exprView;
    private TextView displayView;
    private final StringBuilder expr = new StringBuilder();
    private boolean justEvaluated;
    private boolean hasError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad12 = NeonGlassViews.dp(this, 12);
        int pad18 = NeonGlassViews.dp(this, 18);

        FrameLayout outer = new FrameLayout(this);
        outer.setBackgroundColor(NeonGlassViews.CANVAS_BG);
        outer.setPadding(pad12, pad12, pad12, pad12);

        NeonGlassLayout frame = new NeonGlassLayout(this);
        frame.setPadding(pad18, NeonGlassViews.dp(this, 20), pad18, NeonGlassViews.dp(this, 20));

        // Clean header: icon + title only
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        CalculatorIconView icon = new CalculatorIconView(this);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(
            NeonGlassViews.dp(this, 26), NeonGlassViews.dp(this, 30));
        iconLp.rightMargin = NeonGlassViews.dp(this, 10);
        header.addView(icon, iconLp);

        TextView title = new TextView(this);
        title.setText("Calculator");
        title.setTextColor(NeonGlassViews.TEXT_WHITE);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        header.addView(title);

        frame.addView(header, new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Display area
        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.BOTTOM | Gravity.END);
        display.setPadding(NeonGlassViews.dp(this, 6), NeonGlassViews.dp(this, 16),
            NeonGlassViews.dp(this, 6), NeonGlassViews.dp(this, 16));

        exprView = new TextView(this);
        exprView.setText(" ");
        exprView.setTextColor(NeonGlassViews.TEXT_MUTED);
        exprView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
        exprView.setGravity(Gravity.END);
        exprView.setSingleLine(true);
        exprView.setEllipsize(TextUtils.TruncateAt.START);

        displayView = new TextView(this);
        displayView.setText("0");
        displayView.setTextColor(NeonGlassViews.TEXT_WHITE);
        displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 52);
        displayView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
        displayView.setGravity(Gravity.END);
        displayView.setSingleLine(true);
        displayView.setEllipsize(TextUtils.TruncateAt.START);

        display.addView(exprView);
        display.addView(displayView);

        LinearLayout.LayoutParams dispLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1.15f);
        frame.addView(display, dispLp);

        // 5x4 Button Grid
        String[][] keys = {
            {"AC", "( )", "%",   "÷"},
            {"7",  "8",   "9",   "×"},
            {"4",  "5",   "6",   "−"},
            {"1",  "2",   "3",   "+"},
            {"0",  ".",   "DEL", "="}
        };

        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        int gap = NeonGlassViews.dp(this, 5);

        for (int r = 0; r < keys.length; r++) {
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.HORIZONTAL);
            for (int c = 0; c < 4; c++) {
                final String k = keys[r][c];
                BtnStyle style = (c == 3) ? BtnStyle.NEON : BtnStyle.DIGIT;
                GlassButton btn = new GlassButton(this, k, style, r);
                btn.setOnClickListener(v -> onInput(k));

                LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f);
                bp.setMargins(gap, gap, gap, gap);
                rowLayout.addView(btn, bp);
            }
            LinearLayout.LayoutParams rp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f);
            grid.addView(rowLayout, rp);
        }

        LinearLayout.LayoutParams gridLp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 2.85f);
        frame.addView(grid, gridLp);

        outer.addView(frame, new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(outer);
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
            case "+": case "−": case "×": case "÷":
                insertOp(in.charAt(0));
                break;
            case ".": insertDot(); break;
            case "=": evaluate(); return;
            default: insertDigit(in.charAt(0)); break;
        }
        setDisplay(expr.length() == 0 ? "0" : MathParser.pretty(expr.toString()));
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
        if (expr.length() > 0) expr.deleteCharAt(expr.length() - 1);
        setDisplay(expr.length() == 0 ? "0" : MathParser.pretty(expr.toString()));
    }

    private void resetEvalFlag(boolean clearExpr) {
        if (!justEvaluated) return;
        if (clearExpr) expr.setLength(0);
        exprView.setText(" ");
        justEvaluated = false;
    }

    private void insertDigit(char d) {
        resetEvalFlag(true);
        if (expr.length() > 0 && last() == ')') expr.append('×');
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
        else if (last() == ')') expr.append("×0.");
        else expr.append('.');
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
        else expr.append(open > 0 ? ")" : "×(");
    }

    private void evaluate() {
        if (expr.length() == 0) return;
        String full = MathParser.autoClose(expr.toString());
        exprView.setText(MathParser.pretty(full));
        try {
            double res = MathParser.evaluate(full, true);
            if (!Double.isFinite(res)) throw new IllegalStateException();
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

    private void setDisplay(String text) {
        displayView.setText(text);
        int len = text.length();
        float sp = len <= 8 ? 52f : len <= 11 ? 40f : len <= 15 ? 31f : len <= 20 ? 24f : 19f;
        displayView.setTextSize(TypedValue.COMPLEX_UNIT_SP, sp);
    }
}

