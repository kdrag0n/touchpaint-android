package dev.kdrag0n.touchpaint

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    private lateinit var paintView: PaintView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        paintView = findViewById(R.id.paint_view)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.paint_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sample_rate_toggle -> {
                item.isChecked = !item.isChecked
                paintView.measureSampleRate = item.isChecked
                true
            }
            R.id.brush_size_1px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(1f)
                true
            }
            R.id.brush_size_3px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(3f)
                true
            }
            R.id.brush_size_5px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(5f)
                true
            }
            R.id.brush_size_10px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(10f)
                true
            }
            R.id.brush_size_15px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(15f)
                true
            }
            R.id.brush_size_50px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(50f)
                true
            }
            R.id.brush_size_150px -> {
                item.isChecked = !item.isChecked
                paintView.setBrushSize(150f)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}