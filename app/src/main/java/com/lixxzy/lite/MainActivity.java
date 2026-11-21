package com.lixxzy.lite;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import java.io.*;
import rikka.shizuku.Shizuku;

public class MainActivity extends Activity {
    private static final int REQ = 41;
    private SharedPreferences prefs;
    private Switch toggle;
    private TextView status;
    private boolean updating = false;

    private final Shizuku.OnRequestPermissionResultListener permissionListener =
        (requestCode, grantResult) -> {
            if (requestCode == REQ) {
                if (grantResult == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    applyLite(true);
                } else {
                    setStatus("Izin Shizuku ditolak. DPI/Hz tidak diubah.");
                    toggle.setChecked(false);
                }
            }
        };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        prefs = getSharedPreferences("lixxzy_lite", MODE_PRIVATE);
        Shizuku.addRequestPermissionResultListener(permissionListener);
        buildUi();
        toggle.setChecked(prefs.getBoolean("enabled", false));
        if (toggle.isChecked()) setStatus("Lixxzy Lite aktif • DPI 567 • target 90 Hz");
        else setStatus("Sistem siap");
    }

    private GradientDrawable bg(int color, int stroke) {
        GradientDrawable d = new GradientDrawable();
        d.setColor(color);
        d.setCornerRadius(dp(16));
        d.setStroke(dp(1), stroke);
        return d;
    }
    private int dp(float v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    private void buildUi() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout page = new LinearLayout(this);
        page.setOrientation(LinearLayout.VERTICAL);
        page.setPadding(dp(22), dp(28), dp(22), dp(24));
        page.setGravity(Gravity.CENTER_HORIZONTAL);
        page.setBackgroundColor(Color.rgb(7,7,10));
        scroll.addView(page);
        setContentView(scroll);

        ImageView logo = new ImageView(this);
        logo.setImageResource(R.drawable.lixxzy_logo);
        logo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(155));
        lp.bottomMargin = dp(20);
        page.addView(logo, lp);

        TextView title = new TextView(this);
        title.setText("LIXXZY LITE");
        title.setTextColor(Color.WHITE);
        title.setTextSize(27);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        page.addView(title);

        TextView sub = new TextView(this);
        sub.setText("NATIVE PERFORMANCE PANEL");
        sub.setTextColor(Color.rgb(255,55,83));
        sub.setTextSize(11);
        sub.setLetterSpacing(0.18f);
        sub.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
        sp.topMargin = dp(5); sp.bottomMargin = dp(26);
        page.addView(sub, sp);

        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(18), dp(18), dp(18), dp(18));
        panel.setBackground(bg(Color.rgb(19,19,25), Color.rgb(115,25,43)));
        page.addView(panel, new LinearLayout.LayoutParams(-1, -2));

        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = new TextView(this);
        name.setText("LIXXZY LITE");
        name.setTextColor(Color.WHITE);
        name.setTextSize(18);
        name.setTypeface(null, Typeface.BOLD);
        line.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        toggle = new Switch(this);
        toggle.setButtonTintList(null);
        line.addView(toggle);
        panel.addView(line);

        TextView details = new TextView(this);
        details.setText("DPI TARGET       567\\nREFRESH TARGET   90 Hz");
        details.setTextColor(Color.LTGRAY);
        details.setTextSize(13);
        details.setLineSpacing(dp(7), 1f);
        LinearLayout.LayoutParams dt = new LinearLayout.LayoutParams(-1, -2);
        dt.topMargin = dp(15);
        panel.addView(details, dt);

        status = new TextView(this);
        status.setTextColor(Color.rgb(255,85,105));
        status.setTextSize(12);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams st = new LinearLayout.LayoutParams(-1, -2);
        st.topMargin = dp(16);
        page.addView(status, st);

        TextView info = new TextView(this);
        info.setText("Shizuku harus aktif dan mendapat izin. 90 Hz hanya berlaku jika didukung perangkat.");
        info.setTextColor(Color.GRAY);
        info.setTextSize(11);
        info.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(-1, -2);
        ip.topMargin = dp(8); ip.bottomMargin = dp(25);
        page.addView(info, ip);

        Button ff = makeButton("START FREE FIRE");
        page.addView(ff, buttonParams());
        ff.setOnClickListener(v -> launchGame("com.dts.freefireth", "Free Fire"));

        Button max = makeButton("START FREE FIRE MAX");
        LinearLayout.LayoutParams mp = buttonParams(); mp.topMargin = dp(12);
        page.addView(max, mp);
        max.setOnClickListener(v -> launchGame("com.dts.freefiremax", "Free Fire MAX"));

        toggle.setOnCheckedChangeListener((b, checked) -> {
            if (updating) return;
            if (checked) requestAndApply();
            else applyLite(false);
        });
    }

    private Button makeButton(String text) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextColor(Color.WHITE);
        b.setTextSize(14);
        b.setTypeface(null, Typeface.BOLD);
        b.setAllCaps(false);
        b.setBackground(bg(Color.rgb(180,15,43), Color.rgb(255,60,85)));
        return b;
    }
    private LinearLayout.LayoutParams buttonParams() {
        return new LinearLayout.LayoutParams(-1, dp(54));
    }

    private void requestAndApply() {
        if (!Shizuku.pingBinder()) {
            setStatus("Shizuku belum aktif. Jalankan Shizuku dahulu.");
            resetToggle();
            return;
        }
        if (Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            Shizuku.requestPermission(REQ);
        } else applyLite(true);
    }

    private void applyLite(boolean on) {
        if (!Shizuku.pingBinder() || Shizuku.checkSelfPermission() != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            setStatus("Akses Shizuku tidak tersedia.");
            if (on) resetToggle();
            return;
        }
        setStatus(on ? "Menerapkan DPI 567 dan target 90 Hz..." : "Mengembalikan pengaturan sebelumnya...");
        new Thread(() -> {
            try {
                String command;
                if (on) {
                    if (!prefs.getBoolean("saved", false)) {
                        String snapshot = runShell("D=$(wm density); O=$(echo \"$D\" | sed -n 's/.*Override density: //p'); P=$(echo \"$D\" | sed -n 's/.*Physical density: //p'); [ -z \"$P\" ] && P=$(echo \"$D\" | sed -n 's/.*Physical density: //p'); A=$(settings get system peak_refresh_rate); B=$(settings get system min_refresh_rate); echo \"$O|$P|$A|$B\"");
                        String[] p = snapshot.trim().split("\\|", -1);
                        if (p.length >= 4) {
                            prefs.edit().putString("override", p[0]).putString("physical", p[1])
                                .putString("peak", p[2]).putString("min", p[3])
                                .putBoolean("saved", true).apply();
                        }
                    }
                    command = "set -e; wm density 567; settings put system peak_refresh_rate 90.0; settings put system min_refresh_rate 90.0; wm density";
                } else {
                    if (!prefs.getBoolean("saved", false)) {
                        runOnUiThread(() -> { setStatus("Tidak ada pengaturan awal yang tersimpan."); resetToggle(); });
                        return;
                    }
                    String ov = prefs.getString("override", "");
                    String peak = prefs.getString("peak", "null");
                    String min = prefs.getString("min", "null");
                    String densityCmd = ov == null || ov.isEmpty() ? "wm density reset" : "wm density " + ov;
                    String peakCmd = "null".equals(peak) ? "settings delete system peak_refresh_rate" : "settings put system peak_refresh_rate " + quote(peak);
                    String minCmd = "null".equals(min) ? "settings delete system min_refresh_rate" : "settings put system min_refresh_rate " + quote(min);
                    command = "set -e; " + densityCmd + "; " + peakCmd + "; " + minCmd + "; wm density";
                }
                String result = runShell(command);
                if (on && !result.contains("Override density: 567")) {
                    throw new IOException("DPI 567 tidak terverifikasi. Hasil wm density: " + result);
                }
                if (!on) {
                    String expected = prefs.getString("override", "");
                    if (expected != null && !expected.isEmpty()
                            && !result.contains("Override density: " + expected)) {
                        throw new IOException("DPI awal belum berhasil dipulihkan. Hasil: " + result);
                    }
                    if (expected == null || expected.isEmpty()) {
                        if (result.contains("Override density:")) {
                            throw new IOException("Override density masih aktif setelah OFF. Hasil: " + result);
                        }
                    }
                    prefs.edit().putBoolean("saved", false).apply();
                }
                runOnUiThread(() -> {
                    prefs.edit().putBoolean("enabled", on).apply();
                    setStatus(on ? "Lixxzy Lite ON • DPI 567 terverifikasi • target 90 Hz diminta"
                                 : "Lixxzy Lite OFF • DPI awal dipulihkan");
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    setStatus("Gagal menjalankan perintah sistem. Periksa izin Shizuku.");
                    if (on) resetToggle();
                });
            }
        }).start();
    }

    private String quote(String s) { return "'" + s.replace("'", "'\\''") + "'"; }

    private String runShell(String cmd) throws Exception {
        java.lang.reflect.Method method = Shizuku.class.getDeclaredMethod(
            "newProcess", String[].class, String[].class, String.class
        );
        method.setAccessible(true);
        java.lang.Process p;
        try {
            p = (java.lang.Process) method.invoke(
                null, new String[]{"sh", "-c", cmd + " 2>&1"}, null, null
            );
        } catch (java.lang.reflect.InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception) throw (Exception) cause;
            throw new RuntimeException(cause);
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder output = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            output.append(line).append('\n');
        }
        int code = p.waitFor();
        if (code != 0) {
            throw new IOException("Shell exit " + code + ": " + output);
        }
        return output.toString();
    }

    private void launchGame(String pkg, String label) {
        Intent i = getPackageManager().getLaunchIntentForPackage(pkg);
        if (i == null) {
            Toast.makeText(this, label + " belum terpasang.", Toast.LENGTH_SHORT).show();
        } else {
            try { startActivity(i); }
            catch (Exception e) { Toast.makeText(this, "Tidak bisa membuka " + label, Toast.LENGTH_SHORT).show(); }
        }
    }
    private void setStatus(String s) { status.setText(s); }
    private void resetToggle() {
        updating = true;
        toggle.setChecked(false);
        updating = false;
        prefs.edit().putBoolean("enabled", false).apply();
    }
    @Override protected void onDestroy() {
        Shizuku.removeRequestPermissionResultListener(permissionListener);
        super.onDestroy();
    }
    }
    
