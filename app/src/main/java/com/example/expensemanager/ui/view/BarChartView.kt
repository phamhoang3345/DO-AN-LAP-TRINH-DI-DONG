package com.example.expensemanager.ui.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.expensemanager.model.BarGroupData
import kotlin.math.max

class BarChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private val barGroups = mutableListOf<BarGroupData>()

    private val incomePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#1565C0")
    }

    private val expensePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#2E7D32")
    }

    private val gridLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 2f
        style = Paint.Style.STROKE
        pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#757575")
        textSize = 30f
    }

    private val tooltipBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#323232")
    }

    private val tooltipTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
    }

    private var selectedIndex = -1
    private val cornerRadius = 12f

    private val incomeRect = RectF()
    private val expenseRect = RectF()
    private val tooltipRect = RectF()

    fun setData(dataList: List<BarGroupData>) {
        barGroups.clear()
        barGroups.addAll(dataList)
        selectedIndex = -1
        invalidate()
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN || event.action == MotionEvent.ACTION_MOVE) {
            if (barGroups.isEmpty()) return false

            val paddingLeft = 110f
            val paddingRight = 40f
            val availableWidth = width - paddingLeft - paddingRight
            val groupWidth = availableWidth / barGroups.size

            val touchX = event.x
            val index = ((touchX - paddingLeft) / groupWidth).toInt()
            if (index in barGroups.indices) {
                if (selectedIndex != index) {
                    selectedIndex = index
                    invalidate()
                }
                return true
            }
        } else if (event.action == MotionEvent.ACTION_UP) {
            performClick()
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (barGroups.isEmpty()) return

        val paddingLeft = 110f
        val paddingRight = 40f
        val paddingTop = 60f
        val paddingBottom = 80f

        val chartWidth = width - paddingLeft - paddingRight
        val chartHeight = height - paddingTop - paddingBottom

        if (chartWidth <= 0 || chartHeight <= 0) return

        val maxVal = barGroups.maxOf { max(it.income, it.expense) }.coerceAtLeast(100000.0)

        val gridCount = 4
        for (i in 0..gridCount) {
            val ratio = i.toFloat() / gridCount
            val y = paddingTop + chartHeight * (1f - ratio)

            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridLinePaint)

            val scaleVal = (maxVal * ratio)
            val formattedScale = formatShortAmount(scaleVal)
            labelPaint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formattedScale, paddingLeft - 15f, y + 10f, labelPaint)
        }

        val groupWidth = chartWidth / barGroups.size
        val barWidth = (groupWidth * 0.32f).coerceAtMost(50f)
        val barSpacing = 8f

        barGroups.forEachIndexed { index, group ->
            val groupCenterX = paddingLeft + groupWidth * index + groupWidth / 2f
            val incomeLeft = groupCenterX - barWidth - (barSpacing / 2f)
            val expenseLeft = groupCenterX + (barSpacing / 2f)

            val incomeHeight = ((group.income / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
            incomeRect.set(
                incomeLeft,
                paddingTop + chartHeight - incomeHeight,
                incomeLeft + barWidth,
                paddingTop + chartHeight,
            )
            canvas.drawRoundRect(incomeRect, cornerRadius, cornerRadius, incomePaint)

            val expenseHeight = ((group.expense / maxVal) * chartHeight).toFloat().coerceAtLeast(4f)
            expenseRect.set(
                expenseLeft,
                paddingTop + chartHeight - expenseHeight,
                expenseLeft + barWidth,
                paddingTop + chartHeight,
            )
            canvas.drawRoundRect(expenseRect, cornerRadius, cornerRadius, expensePaint)

            labelPaint.textAlign = Paint.Align.CENTER
            canvas.drawText(
                group.label,
                groupCenterX,
                height - paddingBottom + 45f,
                labelPaint,
            )
        }

        if (selectedIndex in barGroups.indices) {
            val group = barGroups[selectedIndex]
            val groupCenterX = paddingLeft + groupWidth * selectedIndex + groupWidth / 2f

            val tooltipWidth = 260f
            val tooltipHeight = 110f
            var tooltipX = groupCenterX - tooltipWidth / 2f
            tooltipX = tooltipX.coerceIn(paddingLeft, width - paddingRight - tooltipWidth)
            val tooltipY = paddingTop + 10f

            tooltipRect.set(tooltipX, tooltipY, tooltipX + tooltipWidth, tooltipY + tooltipHeight)
            canvas.drawRoundRect(tooltipRect, 16f, 16f, tooltipBgPaint)

            tooltipTextPaint.textAlign = Paint.Align.LEFT
            canvas.drawText(group.label, tooltipX + 16f, tooltipY + 32f, tooltipTextPaint)
            canvas.drawText("Thu: ${group.formattedIncome}", tooltipX + 16f, tooltipY + 64f, tooltipTextPaint)
            canvas.drawText("Chi: ${group.formattedExpense}", tooltipX + 16f, tooltipY + 96f, tooltipTextPaint)
        }
    }

    private fun formatShortAmount(amount: Double): String {
        return when {
            amount >= 1_000_000 -> "${(amount / 1_000_000).toInt()}Mđ"
            amount >= 1_000 -> "${(amount / 1_000).toInt()}kđ"
            else -> "${amount.toInt()}đ"
        }
    }
}
