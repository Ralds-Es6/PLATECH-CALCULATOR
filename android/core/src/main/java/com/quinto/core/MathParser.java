package com.quinto.core;

import java.math.BigDecimal;
import java.text.DecimalFormat;

public final class MathParser {
    private static final DecimalFormat SCI_FMT = new DecimalFormat("0.######E0");

    private final String s;
    private final boolean deg;
    private int p;

    private MathParser(String raw, boolean deg) {
        this.s = raw.replace('−', '-').replace('×', '*').replace('÷', '/');
        this.deg = deg;
    }

    public static double evaluate(String expr, boolean degreesMode) {
        return new MathParser(expr, degreesMode).parse();
    }

    public static String autoClose(String raw) {
        StringBuilder full = new StringBuilder(raw);
        int open = openParenDelta(raw);
        while (open-- > 0) full.append(')');
        return full.toString();
    }

    public static int openParenDelta(CharSequence raw) {
        int open = 0;
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '(') open++;
            else if (c == ')') open--;
        }
        return open;
    }

    public static boolean isOp(char c) {
        return c == '+' || c == '−' || c == '×' || c == '÷' || c == '^';
    }

    public static String toRaw(double v) {
        BigDecimal bd = BigDecimal.valueOf(v).stripTrailingZeros();
        return bd.compareTo(BigDecimal.ZERO) == 0 ? "0" : bd.toPlainString();
    }

    public static String formatResult(double v) {
        if (Math.abs(v) >= 1e12 || (v != 0 && Math.abs(v) < 1e-7)) {
            return SCI_FMT.format(v);
        }
        String raw = toRaw(v);
        return raw.startsWith("-") ? "−" + addCommas(raw.substring(1)) : addCommas(raw);
    }

    public static String pretty(String s) {
        StringBuilder out = new StringBuilder();
        StringBuilder num = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                num.append(c);
                continue;
            }
            if (num.length() > 0) {
                out.append(addCommas(num.toString()));
                num.setLength(0);
            }
            boolean unary = c == '−' && (i == 0 || s.charAt(i - 1) == '(' || isOp(s.charAt(i - 1)));
            if ((c == '+' || c == '−' || c == '×' || c == '÷') && !unary) {
                out.append(' ').append(c).append(' ');
            } else {
                out.append(c);
            }
        }
        if (num.length() > 0) {
            out.append(addCommas(num.toString()));
        }
        return out.toString();
    }

    public static String addCommas(String n) {
        int dot = n.indexOf('.');
        String whole = dot < 0 ? n : n.substring(0, dot);
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < whole.length(); i++) {
            if (i > 0 && (whole.length() - i) % 3 == 0) b.append(',');
            b.append(whole.charAt(i));
        }
        return dot < 0 ? b.toString() : b.append(n.substring(dot)).toString();
    }

    private double parse() {
        double v = addSub();
        if (p < s.length()) throw new IllegalArgumentException();
        return v;
    }

    private double addSub() {
        double x = mulDiv();
        while (p < s.length() && (s.charAt(p) == '+' || s.charAt(p) == '-')) {
            x = s.charAt(p++) == '+' ? x + mulDiv() : x - mulDiv();
        }
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
                x *= power();
            } else {
                break;
            }
        }
        return x;
    }

    private double power() {
        double x = unary();
        if (p < s.length() && s.charAt(p) == '^') {
            p++;
            x = Math.pow(x, power());
        }
        return x;
    }

    private double unary() {
        if (p < s.length() && (s.charAt(p) == '+' || s.charAt(p) == '-')) {
            return s.charAt(p++) == '-' ? -unary() : unary();
        }
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
                if (deg && (Math.abs((x % 180) - 90) < 1e-9 || Math.abs((x % 180) + 90) < 1e-9)) {
                    throw new ArithmeticException("Undefined");
                }
                return Math.tan(rad);
            case "ln":
                if (x <= 0) throw new ArithmeticException("Domain error");
                return Math.log(x);
            case "log":
                if (x <= 0) throw new ArithmeticException("Domain error");
                return Math.log10(x);
            default:
                throw new IllegalArgumentException();
        }
    }

    private static double factorial(double x) {
        if (x < 0 || x != Math.floor(x) || x > 170) throw new ArithmeticException("Domain error");
        double r = 1;
        for (int i = 2; i <= (int) x; i++) r *= i;
        return r;
    }
}
