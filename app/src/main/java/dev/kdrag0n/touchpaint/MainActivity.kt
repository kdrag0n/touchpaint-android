package dev.kdrag0n.touchpaint

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem

class MainActivity : AppCompatActivity() {
    private lateinit var paintView: PaintView
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        paintView = findViewById(R.id.paint_view)
        prefs = getPreferences(Context.MODE_PRIVATE)

        paintView.apply {
            prefs.apply {
                mode = PaintMode.values()[getInt(getString(R.string.preference_mode), 0)]
                paintClearDelay = getLong(getString(R.string.preference_paint_clear_delay), 0)
                measureEventRate = getBoolean(getString(R.string.preference_measure_event_rate), false)

                val savedBrushSize = getFloat(getString(R.string.preference_brush_size), 2f)
                if (savedBrushSize == -1f)
                    setBrushSizePx(1f)
                else
                    setBrushSize(savedBrushSize)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.paint_menu, menu)

        paintView.apply {
            menu!!.apply {
                findItem(when (mode) {
                    PaintMode.PAINT -> R.id.paint_mode
                    PaintMode.FILL -> R.id.fill_mode
                    PaintMode.FOLLOW -> R.id.follow_mode
                    PaintMode.BLANK -> R.id.blank_mode
                }).isChecked = true

                findItem(when (getBrushSizeDp()) {
                    -1f -> R.id.brush_size_physical_1px
                    1f -> R.id.brush_size_1px
                    2f -> R.id.brush_size_2px
                    3f -> R.id.brush_size_3px
                    5f -> R.id.brush_size_5px
                    10f -> R.id.brush_size_10px
                    15f -> R.id.brush_size_15px
                    50f -> R.id.brush_size_50px
                    150f -> R.id.brush_size_150px
                    else -> R.id.brush_size_2px
                }).isChecked = true

                findItem(when (paintClearDelay) {
                    100L -> R.id.clear_delay_100ms
                    250L -> R.id.clear_delay_250ms
                    500L -> R.id.clear_delay_500ms
                    1000L -> R.id.clear_delay_1000ms
                    2000L -> R.id.clear_delay_2000ms
                    5000L -> R.id.clear_delay_5000ms
                    -1L -> R.id.clear_delay_never
                    0L -> R.id.clear_delay_next_stroke
                    else -> R.id.clear_delay_next_stroke
                }).isChecked = true

                findItem(R.id.event_rate_toggle).isChecked = measureEventRate
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.isCheckable) {
            item.isChecked = !item.isChecked
        }

        paintView.apply {
            when (item.itemId) {
                // Modes
                R.id.paint_mode -> mode = PaintMode.PAINT
                R.id.fill_mode -> mode = PaintMode.FILL
                R.id.follow_mode -> mode = PaintMode.FOLLOW
                R.id.blank_mode -> mode = PaintMode.BLANK

                // Brush sizes
                R.id.brush_size_physical_1px -> setBrushSizePx(1f)
                R.id.brush_size_1px -> setBrushSize(1f)
                R.id.brush_size_2px -> setBrushSize(2f)
                R.id.brush_size_3px -> setBrushSize(3f)
                R.id.brush_size_5px -> setBrushSize(5f)
                R.id.brush_size_10px -> setBrushSize(10f)
                R.id.brush_size_15px -> setBrushSize(15f)
                R.id.brush_size_50px -> setBrushSize(50f)
                R.id.brush_size_150px -> setBrushSize(150f)

                // Paint clear delays
                R.id.clear_delay_100ms -> paintClearDelay = 100
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

            prefs.edit().apply {
                putInt(getString(R.string.preference_mode), mode.ordinal)
                putFloat(getString(R.string.preference_brush_size), getBrushSizeDp())
                putLong(getString(R.string.preference_paint_clear_delay), paintClearDelay)
                putBoolean(getString(R.string.preference_measure_event_rate), measureEventRate)
                apply()
            }
        }

        return true
    }
}