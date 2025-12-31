package com.nubiq.timemanagerapp.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var entries: List<Pair<String, Float>> = emptyList()
    private val colors = listOf(
        Color.parseColor("#FF6B6B"),
        Color.parseColor("#4ECDC4"),
        Color.parseColor("#FFD166"),
        Color.parseColor("#06D6A0"),
        Color.parseColor("#118AB2"),
        Color.parseColor("#EF476F"),
        Color.parseColor("#073B4C"),
        Color.parseColor("#7209B7")
    )
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val centerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rectF = RectF()

    init {
        paint.style = Paint.Style.FILL
        textPaint.color = Color.WHITE
        textPaint.textSize = 24f
        centerTextPaint.color = Color.parseColor("#2196F3")
        centerTextPaint.textSize = 32f
        centerTextPaint.textAlign = Paint.Align.CENTER
    }

    fun setData(data: List<Pair<String, Float>>) {
        entries = data.filter { it.second > 0 }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (entries.isEmpty()) {
            drawNoData(canvas)
            return
        }

        val total = entries.sumOf { it.second.toDouble() }.toFloat()
        if (total == 0f) {
            drawNoData(canvas)
            return
        }

        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.8f

        rectF.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)

        var startAngle = 0f

        // Draw pie slices
        entries.forEachIndexed { index, (_, value) ->
            val sweepAngle = (value / total) * 360f
            paint.color = colors[index % colors.size]
            canvas.drawArc(rectF, startAngle, sweepAngle, true, paint)
            startAngle += sweepAngle
        }

        // Draw center text (total time)
        val totalMinutes = entries.sumOf { it.second.toInt() }
        val totalText = formatDuration(totalMinutes)
        canvas.drawText(totalText, centerX, centerY, centerTextPaint)
    }

    private fun drawNoData(canvas: Canvas) {
        paint.color = Color.LTGRAY
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = min(centerX, centerY) * 0.8f
        canvas.drawCircle(centerX, centerY, radius, paint)

        centerTextPaint.color = Color.DKGRAY
        canvas.drawText("No Data", centerX, centerY, centerTextPaint)
    }

    private fun formatDuration(minutes: Int): String {
        return if (minutes < 60) {
            "${minutes}m"
        } else {
            val hours = minutes / 60
            val mins = minutes % 60
            "${hours}h\n${mins}m"
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = 300
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(desiredSize, widthSize)
            else -> desiredSize
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(desiredSize, heightSize)
            else -> desiredSize
        }

        setMeasuredDimension(width, height)
    }
}