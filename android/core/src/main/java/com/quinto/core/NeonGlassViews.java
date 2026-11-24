package com.quinto.core;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class NeonGlassViews {
    public static final int CANVAS_BG = Color.rgb(14, 16, 24);
    public static final int TEXT_WHITE = Color.rgb(248, 248, 255);
    public static final int TEXT_MUTED = Color.rgb(176, 180, 202);
    public static final int TEXT_SCI = Color.rgb(218, 214, 248);
    public static final int TEXT_ACCENT = Color.rgb(186, 140, 255);

    public enum BtnStyle { DIGIT, SCI, NEON }

    public static int dp(Context ctx, float value) {
        return Math.round(TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, ctx.getResources().getDisplayMetrics()
        ));
    }

    public static float dpf(Context ctx, float value) {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, ctx.getResources().getDisplayMetrics()
        );
    }

    // Small purple rounded calculator icon matching the desktop Neon Glass header
    public static class CalculatorIconView extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();

        public CalculatorIconView(Context context) {
            super(context);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            float r = dpf(getContext(), 7);

            paint.setShader(new LinearGradient(
                0, 0, w, h,
                Color.rgb(224, 92, 245), Color.rgb(156, 62, 240),
                Shader.TileMode.CLAMP
            ));
            rect.set(0, 0, w, h);
            canvas.drawRoundRect(rect, r, r, paint);

            paint.setShader(null);
            paint.setColor(Color.rgb(28, 16, 44));
            float p = w * 0.18f;
            rect.set(p, h * 0.17f, w - p, h * 0.37f);
            canvas.drawRoundRect(rect, dpf(getContext(), 2), dpf(getContext(), 2), paint);

            float cellW = (w - p * 2f) / 3f;
            float dotW = cellW * 0.65f;
            float startY = h * 0.48f;
            float rowStep = h * 0.21f;
            for (int row = 0; row < 2; row++) {
                for (int col = 0; col < 3; col++) {
                    float left = p + col * cellW + (cellW - dotW) / 2f;
                    float top = startY + row * rowStep;
                    rect.set(left, top, left + dotW, top + dotW);
                    canvas.drawRoundRect(rect, dpf(getContext(), 1.5f), dpf(getContext(), 1.5f), paint);
                }
            }
        }
    }

    // Main Neon Glass container with glowing cyan-to-magenta gradient border and ambient glows
    public static class NeonGlassLayout extends LinearLayout {
        private static final int[] BORDER_COLORS = {
            Color.rgb(64, 212, 255),
            Color.rgb(186, 85, 255),
            Color.rgb(224, 92, 240),
            Color.rgb(108, 92, 255),
            Color.rgb(64, 184, 255)
        };
        private static final float[] BORDER_STOPS = {0f, 0.28f, 0.55f, 0.80f, 1f};

        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint glowPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF cardRect = new RectF();
        private final Path clipPath = new Path();
        private final Path ribbonPath = new Path();

        public NeonGlassLayout(Context context) {
            super(context);
            setOrientation(VERTICAL);
            setWillNotDraw(false);
            borderPaint.setStyle(Paint.Style.STROKE);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;

            float inset = dpf(getContext(), 4);
            float radius = dpf(getContext(), 28);
            cardRect.set(inset, inset, w - inset, h - inset);

            // Base dark glass fill
            fillPaint.setShader(new LinearGradient(
                0, 0, w, h,
                Color.rgb(16, 17, 28), Color.rgb(11, 13, 22),
                Shader.TileMode.CLAMP
            ));
            canvas.drawRoundRect(cardRect, radius, radius, fillPaint);

            // Ambient light streaks clipped inside card
            int save = canvas.save();
            clipPath.reset();
            clipPath.addRoundRect(cardRect, radius, radius, Path.Direction.CW);
            canvas.clipPath(clipPath);

            ribbonPath.reset();
            ribbonPath.moveTo(0, h * 0.16f);
            ribbonPath.quadTo(w * 0.22f, h * 0.08f, w * 0.52f, h * 0.02f);
            ribbonPath.lineTo(w * 0.52f, h * 0.09f);
            ribbonPath.quadTo(w * 0.20f, h * 0.16f, 0, h * 0.26f);
            ribbonPath.close();

            glowPaint.setShader(new LinearGradient(
                0, h * 0.22f, w * 0.45f, h * 0.06f,
                Color.argb(120, 185, 55, 215), Color.argb(0, 90, 40, 160),
                Shader.TileMode.CLAMP
            ));
            canvas.drawPath(ribbonPath, glowPaint);

            glowPaint.setShader(new RadialGradient(
                w * 0.10f, h * 0.14f, w * 0.42f,
                Color.argb(62, 165, 58, 225), Color.argb(0, 165, 58, 225),
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, 0, w, h * 0.5f, glowPaint);

            glowPaint.setShader(new RadialGradient(
                w * 0.88f, h * 0.94f, w * 0.52f,
                Color.argb(80, 72, 108, 255), Color.argb(0, 72, 108, 255),
                Shader.TileMode.CLAMP
            ));
            canvas.drawRect(0, h * 0.5f, w, h, glowPaint);
            canvas.restoreToCount(save);

            // Multi-layer neon border glow
            LinearGradient borderGrad = new LinearGradient(
                0, 0, w, h, BORDER_COLORS, BORDER_STOPS, Shader.TileMode.CLAMP
            );
            borderPaint.setShader(borderGrad);

            float[] glowWidths = {dpf(getContext(), 7.5f), dpf(getContext(), 5.0f), dpf(getContext(), 3.2f)};
            borderPaint.setAlpha(36);
            for (float gw : glowWidths) {
                borderPaint.setStrokeWidth(gw);
                canvas.drawRoundRect(cardRect, radius, radius, borderPaint);
            }

            borderPaint.setAlpha(242);
            borderPaint.setStrokeWidth(dpf(getContext(), 2.0f));
            canvas.drawRoundRect(cardRect, radius, radius, borderPaint);

            super.onDraw(canvas);
        }
    }

    // Custom frosted glass & glowing purple neon button
    public static class GlassButton extends TextView {
        private final BtnStyle style;
        private final int baseTop;
        private final int baseBot;
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint rimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint haloPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private boolean isDown = false;

        public GlassButton(Context context, String label, BtnStyle style, int row) {
            super(context);
            this.style = style;

            if (style == BtnStyle.NEON) {
                boolean mid = (row == 2 || row == 5);
                baseTop = mid ? Color.rgb(156, 72, 255) : Color.rgb(196, 84, 255);
                baseBot = mid ? Color.rgb(118, 52, 238) : Color.rgb(140, 60, 246);
            } else if (style == BtnStyle.SCI) {
                baseTop = Color.argb(215, 34, 34, 56);
                baseBot = Color.argb(230, 24, 24, 42);
            } else {
                baseTop = Color.argb(225, 46, 46, 72);
                baseBot = Color.argb(240, 32, 32, 52);
            }

            setText(label);
            setGravity(Gravity.CENTER);
            setClickable(true);
            setFocusable(true);
            setTextColor(style == BtnStyle.SCI ? TEXT_SCI : TEXT_WHITE);
            setTypeface(Typeface.SANS_SERIF, style == BtnStyle.NEON ? Typeface.BOLD : Typeface.NORMAL);
            setTextSize(TypedValue.COMPLEX_UNIT_SP,
                style == BtnStyle.NEON ? 24f : style == BtnStyle.SCI ? 15f : 21f);

            rimPaint.setStyle(Paint.Style.STROKE);
            rimPaint.setStrokeWidth(dpf(context, 1.1f));
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    isDown = true;
                    performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    isDown = false;
                    invalidate();
                    break;
            }
            return super.onTouchEvent(event);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float w = getWidth();
            float h = getHeight();
            if (w <= 0 || h <= 0) return;

            float pad = dpf(getContext(), style == BtnStyle.NEON ? 3f : 1f);
            float radius = dpf(getContext(), 18f);

            int topCol = baseTop;
            int botCol = baseBot;

            if (style == BtnStyle.NEON) {
                haloPaint.setColor(Color.argb(isDown ? 85 : 46, 172, 72, 255));
                for (int i = 2; i >= 1; i--) {
                    float hp = pad - dpf(getContext(), i * 1.2f);
                    rect.set(hp, hp, w - hp, h - hp);
                    canvas.drawRoundRect(rect, radius + dpf(getContext(), 2), radius + dpf(getContext(), 2), haloPaint);
                }
                if (isDown) {
                    topCol = darken(topCol, 0.82f);
                    botCol = darken(botCol, 0.82f);
                }
            } else if (isDown) {
                topCol = Color.argb(240, 72, 72, 110);
                botCol = Color.argb(245, 50, 50, 82);
            }

            rect.set(pad, pad, w - pad, h - pad);
            fillPaint.setShader(new LinearGradient(0, pad, 0, h - pad, topCol, botCol, Shader.TileMode.CLAMP));
            canvas.drawRoundRect(rect, radius, radius, fillPaint);

            if (style == BtnStyle.NEON) {
                rimPaint.setShader(null);
                rimPaint.setColor(Color.argb(120, 242, 210, 255));
            } else {
                rimPaint.setShader(new LinearGradient(
                    0, pad, 0, h - pad,
                    Color.argb(78, 175, 165, 225), Color.argb(36, 95, 95, 145),
                    Shader.TileMode.CLAMP
                ));
            }
            canvas.drawRoundRect(rect, radius, radius, rimPaint);

            super.onDraw(canvas);
        }

        private static int darken(int color, float factor) {
            return Color.argb(
                Color.alpha(color),
                Math.round(Color.red(color) * factor),
                Math.round(Color.green(color) * factor),
                Math.round(Color.blue(color) * factor)
            );
        }
    }
}

