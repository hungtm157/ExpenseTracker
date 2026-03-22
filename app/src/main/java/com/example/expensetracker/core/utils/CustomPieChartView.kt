package com.example.expensetracker.core.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class CustomPieChartView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        color = Color.WHITE
        strokeWidth = dpToPx(2f)
    }

    private val rectF = RectF()
    private var data: List<Pair<Float, Int>> = emptyList() // Pair<Percentage (0-100), Color>

    fun setData(newData: List<Pair<Float, Int>>) {
        var total = 0f
        val filteredData = mutableListOf<Pair<Float, Int>>()
        for (item in newData) {
            val amount = item.first
            if (amount > 0f) {
                total += amount
                filteredData.add(item)
            }
        }
        
        // Convert to sweep angles
        if (total > 0f) {
            this.data = filteredData.map { Pair(it.first / total * 360f, it.second) }
        } else {
            this.data = emptyList()
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val radius = Math.min(width, height) / 2f
        val strokeOffset = strokePaint.strokeWidth / 2f

        rectF.set(
            width / 2f - radius + strokeOffset,
            height / 2f - radius + strokeOffset,
            width / 2f + radius - strokeOffset,
            height / 2f + radius - strokeOffset
        )

        if (data.isEmpty()) {
            paint.color = Color.parseColor("#E0E0E0")
            canvas.drawArc(rectF, 0f, 360f, true, paint)
            return
        }

        var startAngle = -90f // Start from top
        data.forEach { (sweepAngle, color) ->
            paint.color = color
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
            // Draw separator line
            if (data.size > 1) {
                canvas.drawArc(rectF, startAngle, sweepAngle, true, strokePaint)
            }
            startAngle += sweepAngle
        }
    }

    private fun dpToPx(dp: Float): Float {
        return dp * context.resources.displayMetrics.density
    }
}
