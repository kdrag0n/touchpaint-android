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
        item.isChecked = !item.isChecked

        paintView.apply {
            when (item.itemId) {
                // Modes
                R.id.paint_mode -> mode = PaintMode.PAINT
                R.id.fill_mode -> mode = PaintMode.FILL
                R.id.follow_mode -> mode = PaintMode.FOLLOW
                R.id.blank_mode -> mode = PaintMode.BLANK

                // Brush sizes
                R.id.brush_size_1px -> setBrushSize(1f)
                R.id.brush_size_3px -> setBrushSize(3f)
                R.id.brush_size_5px -> setBrushSize(5f)
                R.id.brush_size_10px -> setBrushSize(10f)
                R.id.brush_size_15px -> setBrushSize(15f)
                R.id.brush_size_50px -> setBrushSize(50f)
                R.id.brush_size_150px -> setBrushSize(150f)

                // Paint clear delays
                R.id.clear_delay_250ms -> paintClearDelay = 250
                R.id.clear_delay_500ms -> paintClearDelay = 500
                R.id.clear_delay_1000ms -> paintClearDelay = 1000
                R.id.clear_delay_2000ms -> paintClearDelay = 2000
                R.id.clear_delay_5000ms -> paintClearDelay = 5000
                R.id.clear_delay_never -> paintClearDelay = -1
                R.id.clear_delay_next_stroke -> paintClearDelay = 0

                    // Other toggles
                R.id.event_rate_toggle -> measureEventRate = item.isChecked

                // Submenus and other unhandled items
                else -> return super.onOptionsItemSelected(item)
            }
        }

        return true
    }
}