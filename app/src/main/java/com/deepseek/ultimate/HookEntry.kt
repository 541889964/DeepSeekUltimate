package com.deepseek.ultimate
import android.app.Activity
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.MotionEvent
import android.widget.*
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class HookEntry : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "com.deepseek.chat") return
        try {
            XposedHelpers.findAndHookMethod("android.app.Activity", lpparam.classLoader, "onResume", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    val a = param.thisObject as? Activity
                    if (a != null) a.runOnUiThread { a.window?.decorView?.postDelayed({ injectUI(a) }, 500) }
                }
            })
        } catch (_: Throwable) {}
    }
    private fun injectUI(activity: Activity) {
        try {
            val rootView = activity.window?.decorView as? FrameLayout ?: return
            if (rootView.findViewWithTag<Button>("ultimate_btn") != null) return
            val fab = Button(activity).apply {
                tag = "ultimate_btn"; text = "⚡"; textSize = 26f; setTextColor(Color.WHITE)
                background = GradientDrawable().apply { shape = GradientDrawable.OVAL; setColor(Color.parseColor("#1E1E1E")); setStroke(4, Color.parseColor("#4CAF50")) }
                elevation = 30f; setOnClickListener { showDialog(activity) }
            }
            val lp = FrameLayout.LayoutParams(150, 150).apply { gravity = Gravity.BOTTOM or Gravity.END; setMargins(0, 0, 50, 350) }
            rootView.addView(fab, lp)
            var initialX = 0; var initialY = 0; var initialTouchX = 0f; var initialTouchY = 0f
            fab.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> { initialX = lp.leftMargin; initialY = lp.topMargin; initialTouchX = event.rawX; initialTouchY = event.rawY; false }
                    MotionEvent.ACTION_MOVE -> {
                        lp.leftMargin = initialX + (event.rawX - initialTouchX).toInt()
                        lp.topMargin = initialY + (event.rawY - initialTouchY).toInt()
                        if (lp.leftMargin < 0) lp.leftMargin = 0; if (lp.topMargin < 0) lp.topMargin = 0
                        fab.layoutParams = lp; true
                    }
                    else -> false
                }
            }
        } catch (_: Throwable) {}
    }
    private fun showDialog(activity: Activity) {
        val layout = LinearLayout(activity).apply { orientation = LinearLayout.VERTICAL; setPadding(50, 50, 50, 50) }
        val input = EditText(activity).apply { hint = "输入任务指令 (支持 @云端 URL)" }
        layout.addView(input)
        val output = TextView(activity).apply { text = "中枢就绪...\n可长按 ⚡ 拖动位置"; textSize = 12f; setTextColor(Color.GREEN); setBackgroundColor(Color.BLACK); setPadding(25, 25, 25, 25) }
        val scroll = ScrollView(activity).apply { addView(output); layoutParams = LinearLayout.LayoutParams(-1, 600) }
        layout.addView(scroll)
        val dialog = AlertDialog.Builder(activity).setTitle("⚡ 终极中枢").setView(layout).setPositiveButton("执行", null).setNegativeButton("关闭", null).create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val t = input.text.toString().trim()
                if (t.isNotEmpty()) {
                    output.text = ">>> 任务启动...\n"
                    Thread { UltimateEngine.execute(t) { log -> activity.runOnUiThread { output.append("$log\n"); scroll.post { scroll.fullScroll(ScrollView.FOCUS_DOWN) } } } }.start()
                }
            }
        }
        dialog.show()
    }
}
