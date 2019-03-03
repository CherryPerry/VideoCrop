package ru.cherryperry.instavideo.presentation.util

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.FloatRange
import androidx.core.content.ContextCompat
import androidx.core.math.MathUtils
import ru.cherryperry.instavideo.R
import ru.cherryperry.instavideo.core.illegalArgument

class TimeSelectorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val ALPHA_SELECTED = 255
        private const val ALPHA_UNSELECTED_ = (255 * 0.38f).toInt()

        private val STATE_PRESSED = intArrayOf(android.R.attr.state_pressed, android.R.attr.state_enabled)
        private val STATE_ENABLED = intArrayOf(android.R.attr.state_enabled)
        private val STATE_DISABLED = intArrayOf()
    }

    private val leftThumbDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.icon_time_selector)!!
        .mutate()
        .let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            it.callback = this
            it
        }
    private val rightThumbDrawable: Drawable = ContextCompat.getDrawable(context, R.drawable.icon_time_selector)!!
        .mutate()
        .let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            it.callback = this
            it
        }
    private val progressLineEnabledColor = ContextCompat.getColor(context, R.color.colorAccent)
    private val progressLineDisabledColor = ContextCompat.getColor(context, R.color.disabled)
    private val progressLinePaint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 2 dp context
    }
    private val halfSlop = leftThumbDrawable.intrinsicWidth / 2f

    /** Left pointer position is in range [0..1] of available width. */
    @FloatRange(from = 0.0, to = 1.0)
    private var leftPointerPosition: Float = 0f
    /** Right pointer position is in range [0..1] of available width. */
    @FloatRange(from = 0.0, to = 1.0)
    private var rightPointerPosition: Float = 1f
    /** Limit of pointer's positions. */
    @FloatRange(from = 0.0)
    private var limit: Float = 1f

    /** Left pointer position in pixels from left of view. */
    private val realLeftPointerPosition: Float
        get() = paddingLeft + leftPointerPosition * (width - paddingRight - paddingLeft)
    /** Right pointer position in pixels from left of view. */
    private val realRightPointerPosition: Float
        get() = paddingLeft + rightPointerPosition * (width - paddingRight - paddingLeft)

    /** [onTouchEvent] implementation's variables. */
    private var oldX: Float = 0f
    private var oldY: Float = 0f
    private var touched: Boolean = false
    private var touchedRight: Boolean = false

    /** Listeners. **/
    var selectionStartListener: ((Boolean, Float, Float) -> Unit)? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val newHeightMeasureSpec = if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            MeasureSpec.makeMeasureSpec(
                leftThumbDrawable.intrinsicHeight + paddingTop + paddingBottom, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, newHeightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val lineWidth = width - paddingRight - paddingLeft
        val drawLeft = paddingLeft.toFloat()
        val drawRight = (width - paddingRight).toFloat()
        val lineY = height / 2f
        progressLinePaint.color = if (isEnabled) progressLineEnabledColor else progressLineDisabledColor
        progressLinePaint.alpha = ALPHA_UNSELECTED_
        canvas.drawLine(drawLeft, lineY, drawRight, lineY, progressLinePaint)
        val leftBorder = drawLeft + lineWidth * leftPointerPosition
        val rightBorder = drawLeft + lineWidth * rightPointerPosition
        progressLinePaint.alpha = ALPHA_SELECTED
        canvas.drawLine(leftBorder, lineY, rightBorder, lineY, progressLinePaint)
        canvas.save()
        canvas.translate(
            leftBorder - leftThumbDrawable.intrinsicWidth / 2,
            lineY - rightThumbDrawable.intrinsicHeight / 2
        )
        leftThumbDrawable.draw(canvas)
        canvas.restore()
        canvas.save()
        canvas.translate(
            rightBorder - rightThumbDrawable.intrinsicWidth / 2,
            lineY - rightThumbDrawable.intrinsicHeight / 2
        )
        rightThumbDrawable.draw(canvas)
        canvas.restore()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        // ClickableViewAccessibility - super.onTouchEvent called.
        if (isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.x
                    oldY = event.y
                    if (event.x > realRightPointerPosition - halfSlop &&
                        event.x < realRightPointerPosition + halfSlop) {
                        touched = true
                        touchedRight = true
                    } else if (event.x > realLeftPointerPosition - halfSlop &&
                        event.x < realLeftPointerPosition + halfSlop) {
                        touched = true
                        touchedRight = false
                    }
                    if (touched) {
                        refreshDrawableState()
                        notifyStartChanged(true)
                        invalidate()
                        super.onTouchEvent(event)
                        return true
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (touched) {
                        changePosition(event)
                        oldX = event.x
                        oldY = event.y
                        invalidate()
                        super.onTouchEvent(event)
                        return true
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (touched) {
                        touched = false
                        refreshDrawableState()
                        notifyStartChanged(false)
                        invalidate()
                        super.onTouchEvent(event)
                        return true
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun drawableStateChanged() {
        super.drawableStateChanged()
        var changed = false
        if (isEnabled) {
            changed = changed or leftThumbDrawable.setState(
                if (touched && !touchedRight) STATE_PRESSED else STATE_ENABLED)
            changed = changed or rightThumbDrawable.setState(
                if (touched && touchedRight) STATE_PRESSED else STATE_ENABLED)
        } else {
            changed = changed or leftThumbDrawable.setState(STATE_DISABLED)
            changed = changed or rightThumbDrawable.setState(STATE_DISABLED)
        }
        if (changed) {
            invalidate()
        }
    }

    /**
     * Set limit for selection.
     * User can't move pointer such way, that pointer's positions delta is more than [range].
     * Default value is ```1```.
     * @throws IllegalArgumentException if range less than 0.
     */
    fun setLimit(@FloatRange(from = 0.0) range: Float) {
        (range < 0f) illegalArgument "Limit can't be less than 0"
        limit = range
        validateRightPosition()
        validateLeftPosition()
    }

    private fun changePosition(event: MotionEvent) {
        val deltaX = (event.x - oldX) / (width - paddingLeft - paddingRight)
        if (touchedRight) {
            rightPointerPosition += deltaX
            validateRightPosition()
        } else {
            leftPointerPosition += deltaX
            validateLeftPosition()
        }
    }

    private fun validateRightPosition() {
        rightPointerPosition = MathUtils.clamp(rightPointerPosition, 0f, 1f)
        // it is not possible to swap pointers
        if (rightPointerPosition < leftPointerPosition) {
            leftPointerPosition = rightPointerPosition
        }
        // it is not possible to move pointer above limit
        if (rightPointerPosition - leftPointerPosition > limit) {
            rightPointerPosition = leftPointerPosition + limit
        }
    }

    private fun validateLeftPosition() {
        leftPointerPosition = MathUtils.clamp(leftPointerPosition, 0f, 1f)
        // it is not possible to swap pointers
        if (leftPointerPosition > rightPointerPosition) {
            rightPointerPosition = leftPointerPosition
        }
        // it is not possible to move pointer above limit
        if (rightPointerPosition - leftPointerPosition > limit) {
            leftPointerPosition = rightPointerPosition - limit
        }
    }

    private fun notifyStartChanged(started: Boolean) {
        selectionStartListener?.apply { invoke(started, leftPointerPosition, rightPointerPosition) }
    }
}
