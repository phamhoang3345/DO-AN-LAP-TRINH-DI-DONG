package com.example.expensemanager.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

data class DonutSliceData(
    val categoryName: String,
    val value: Float,
    val color: Int,
)

class DonutChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val slices = mutableListOf<DonutSliceData>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val rectF = RectF()
    private var strokeWidthPx = 40f
    private val gapAngle = 3f

    fun setData(newSlices: List<DonutSliceData>) {
        slices.clear()
        slices.addAll(newSlices)
        invalidate()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val minSize = min(w, h).toFloat()
        val padding = minSize * 0.12f
        strokeWidthPx = minSize * 0.18f
        paint.strokeWidth = strokeWidthPx

        val halfStroke = strokeWidthPx / 2f
        rectF.set(
            padding + halfStroke,
            padding + halfStroke,
            w - padding - halfStroke,
            h - padding - halfStroke,
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (slices.isEmpty()) return

        val totalValue = slices.sumOf { it.value.toDouble() }.toFloat()
        if (totalValue <= 0f) return

        var startAngle = -90f

        for (slice in slices) {
            val sweepAngle = (slice.value / totalValue) * 360f
            if (sweepAngle <= 0f) continue

            paint.color = slice.color

            val actualSweep = if (slices.size > 1) (sweepAngle - gapAngle).coerceAtLeast(1f) else sweepAngle
            canvas.drawArc(rectF, startAngle, actualSweep, false, paint)

            startAngle += sweepAngle
        }
    }
}
