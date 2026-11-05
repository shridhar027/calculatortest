package com.example.quickcalc;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class MainActivity extends Activity {
    private TextView expressionView, resultView;
    private String expression = "";
    private boolean justEvaluated = false;
    private final int bg = Color.rgb(13, 18, 30);
    private final int panel = Color.rgb(25, 32, 48);
    private final int accent = Color.rgb(110, 160, 255);
    private final int white = Color.rgb(242, 245, 250);

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        getWindow().getDecorView().setSystemUiVisibility(0);
        buildUi();
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(22), dp(20), dp(18));
        root.setBackgroundColor(bg);

        TextView brand = new TextView(this);
        brand.setText("QUICKCALC");
        brand.setTextColor(accent);
        brand.setTextSize(13);
        brand.setLetterSpacing(.16f);
        brand.setTypeface(null, Typeface.BOLD);
        root.addView(brand, new LinearLayout.LayoutParams(-1, dp(34)));

        LinearLayout display = new LinearLayout(this);
        display.setOrientation(LinearLayout.VERTICAL);
        display.setGravity(Gravity.BOTTOM | Gravity.RIGHT);
        display.setPadding(dp(8), dp(12), dp(8), dp(24));
        expressionView = new TextView(this);
        expressionView.setTextColor(Color.rgb(145, 157, 179));
        expressionView.setTextSize(22);
        expressionView.setGravity(Gravity.RIGHT);
        resultView = new TextView(this);
        resultView.setText("0");
        resultView.setTextColor(white);
        resultView.setTextSize(48);
        resultView.setTypeface(null, Typeface.BOLD);
        resultView.setGravity(Gravity.RIGHT);
        display.addView(expressionView, new LinearLayout.LayoutParams(-1, -2));
        display.addView(resultView, new LinearLayout.LayoutParams(-1, -2));
        root.addView(display, new LinearLayout.LayoutParams(-1, 0, 1));

        String[][] keys = {
            {"AC", "DEL", "%", "÷"},
            {"7", "8", "9", "×"},
            {"4", "5", "6", "−"},
            {"1", "2", "3", "+"},
            {"±", "0", ".", "="}
        };
        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(4);
        grid.setRowCount(5);
        for (String[] row : keys) for (String key : row) {
            Button b = new Button(this);
            b.setText(key);
            b.setTextSize(21);
            b.setAllCaps(false);
            b.setTypeface(null, Typeface.BOLD);
            b.setTextColor(key.equals("=") ? Color.WHITE :
                    (isOperator(key) || key.equals("AC") || key.equals("DEL") || key.equals("%") ? accent : white));
            b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    key.equals("=") ? accent : panel));
            b.setElevation(0);
            GridLayout.LayoutParams p = new GridLayout.LayoutParams();
            p.width = 0; p.height = dp(68);
            p.columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f);
            p.setMargins(dp(5), dp(5), dp(5), dp(5));
            grid.addView(b, p);
            b.setOnClickListener(v -> press(key));
        }
        root.addView(grid, new LinearLayout.LayoutParams(-1, -2));
        setContentView(root);
    }

    private boolean isOperator(String s) {
        return s.equals("÷") || s.equals("×") || s.equals("−") || s.equals("+");
    }
    private int dp(int n) { return (int)(n * getResources().getDisplayMetrics().density + .5f); }

    private void press(String key) {
        if (key.equals("AC")) { expression = ""; justEvaluated = false; show("0"); return; }
        if (key.equals("DEL")) {
            if (!expression.isEmpty()) expression = expression.substring(0, expression.length()-1);
            show(expression.isEmpty() ? "0" : expression); return;
        }
        if (key.equals("=")) {
            try {
                String value = calculate(expression);
                expressionView.setText(expression + " =");
                resultView.setText(value);
                expression = value;
                justEvaluated = true;
            } catch (Exception e) {
                resultView.setText("Error");
                Toast.makeText(this, "Invalid expression", Toast.LENGTH_SHORT).show();
            }
            return;
        }
        if (key.equals("±")) {
            if (!expression.isEmpty() && expression.matches("-?\\d+(\\.\\d+)?")) {
                expression = expression.startsWith("-") ? expression.substring(1) : "-" + expression;
            }
            show(expression.isEmpty() ? "0" : expression); return;
        }
        if (justEvaluated && !isOperator(key) && !key.equals("%")) expression = "";
        justEvaluated = false;
        if (key.equals("%")) expression += "%";
        else if (isOperator(key)) expression += key;
        else {
            if (key.equals(".") && currentNumberHasDot()) return;
            expression += key;
        }
        show(expression);
    }

    private boolean currentNumberHasDot() {
        for (int i=expression.length()-1; i>=0; i--) {
            char c=expression.charAt(i);
            if (c=='.') return true;
            if (!Character.isDigit(c)) return false;
        }
        return false;
    }

    private void show(String s) {
        expressionView.setText(s);
        resultView.setText(s.isEmpty() ? "0" : s);
    }

    // Recursive-descent parser: + - × ÷, unary signs, parentheses-free %, decimals.
    private String calculate(String s) {
        if (s == null || s.trim().isEmpty()) throw new IllegalArgumentException();
        Parser p = new Parser(s.replace("×","*").replace("÷","/").replace("−","-"));
        double v = p.parseExpression();
        if (!p.atEnd() || Double.isNaN(v) || Double.isInfinite(v)) throw new IllegalArgumentException();
        BigDecimal bd = BigDecimal.valueOf(v).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return bd.toPlainString();
    }

    private static class Parser {
        final String s; int pos=0;
        Parser(String s) { this.s=s.replace(" ",""); }
        boolean atEnd() { return pos>=s.length(); }
        double parseExpression() {
            double v=parseTerm();
            while (!atEnd()) {
                char c=s.charAt(pos);
                if (c!='+' && c!='-') break;
                pos++; double r=parseTerm(); v=(c=='+')?v+r:v-r;
            }
            return v;
        }
        double parseTerm() {
            double v=parseFactor();
            while (!atEnd()) {
                char c=s.charAt(pos);
                if (c!='*' && c!='/') break;
                pos++; double r=parseFactor();
                if (c=='/' && r==0) throw new ArithmeticException();
                v=(c=='*')?v*r:v/r;
            }
            return v;
        }
        double parseFactor() {
            if (atEnd()) throw new IllegalArgumentException();
            if (s.charAt(pos)=='+') { pos++; return parseFactor(); }
            if (s.charAt(pos)=='-') { pos++; return -parseFactor(); }
            int start=pos;
            while (!atEnd() && (Character.isDigit(s.charAt(pos)) || s.charAt(pos)=='.')) pos++;
            if (start==pos) throw new IllegalArgumentException();
            double v=Double.parseDouble(s.substring(start,pos));
            if (!atEnd() && s.charAt(pos)=='%') { pos++; v/=100.0; }
            return v;
        }
    }
}
