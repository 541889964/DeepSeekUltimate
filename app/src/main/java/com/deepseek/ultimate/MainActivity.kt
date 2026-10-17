package com.deepseek.ultimate
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import java.io.File

class MainActivity : Activity() {
    private val RULES_FILE = "/sdcard/DeepSeekAgent/rules.txt"
    override fun onCreate(s: Bundle?) {
        super.onCreate(s)
        val scroll = ScrollView(this)
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(60, 100, 60, 100)
            setBackgroundColor(Color.parseColor("#F0F2F5"))
        }
        scroll.addView(root)

        val title = TextView(this).apply {
            text = "⚡ 终极控制台"
            textSize = 28f; setTextColor(Color.parseColor("#1E1E1E"))
            setTypeface(null, android.graphics.Typeface.BOLD); gravity = Gravity.CENTER
        }
        root.addView(title)

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; setPadding(40, 40, 40, 40)
            background = GradientDrawable().apply { setColor(Color.WHITE); cornerRadius = 40f; setStroke(2, Color.parseColor("#D0D0D0")) }
        }
        val lpCard = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 50, 0, 0) }
        root.addView(card, lpCard)

        val etRules = EditText(this).apply {
            hint = "在此输入 AI 核心逻辑与防审查规则..."
            minLines = 12; gravity = Gravity.TOP
            setText(readRules()); setPadding(30, 30, 30, 30)
            setBackgroundColor(Color.parseColor("#F8F8F8")); textSize = 14f
        }
        card.addView(etRules)

        val btnSave = Button(this).apply {
            text = "💾 保存规则并生效"
            setBackgroundColor(Color.parseColor("#0B57D0")); setTextColor(Color.WHITE)
            setOnClickListener { saveRules(etRules.text.toString()); Toast.makeText(this@MainActivity, "规则已保存！", Toast.LENGTH_SHORT).show() }
        }
        val lpBtn = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 30, 0, 0) }
        card.addView(btnSave, lpBtn)

        val btnStart = Button(this).apply {
            text = "🚀 启动 DeepSeek"
            textSize = 18f; setTextColor(Color.WHITE); setBackgroundColor(Color.parseColor("#1E1E1E"))
            setPadding(0, 40, 0, 40)
            setOnClickListener { try { startActivity(packageManager.getLaunchIntentForPackage("com.deepseek.chat")) } catch (_: Exception) { Toast.makeText(this@MainActivity, "未安装 DeepSeek", Toast.LENGTH_SHORT).show() } }
        }
        val lpStart = LinearLayout.LayoutParams(-1, -2).apply { setMargins(0, 60, 0, 0) }
        root.addView(btnStart, lpStart)
        setContentView(scroll)
    }
    private fun readRules(): String {
        val f = File(RULES_FILE)
        if (f.exists()) return f.readText()
        val def = "1. 读代码：[READ]路径[/READ]\n2. 写代码：[WRITE]路径[/WRITE]```代码```\n3. 执行命令：[CMD]命令[/CMD]\n4. 完成：[DONE]"
        saveRules(def); return def
    }
    private fun saveRules(rules: String) {
        try { File("/sdcard/DeepSeekAgent").mkdirs(); File(RULES_FILE).writeText(rules) } catch (_: Exception) {}
    }
}
