package ru.cherryperry.instavideo.presentation.editor

import android.content.Intent
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.TextureView
import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.arellomobile.mvp.presenter.InjectPresenter
import com.arellomobile.mvp.presenter.ProvidePresenter
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ClippingMediaSource
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import ru.cherryperry.instavideo.core.illegalArgument
import ru.cherryperry.instavideo.presentation.base.BaseFragment
import ru.cherryperry.instavideo.presentation.util.TimeSelectorView
import ru.cherryperry.instavideo.presentation.util.ViewDelegate
import ru.cherryperry.instavideo.presentation.util.dp
import ru.cherryperry.instavideo.presentation.util.saf.StorageAccessFramework
import ru.cherryperry.instavideo.presentation.util.scaledBottom
import ru.cherryperry.instavideo.presentation.util.scaledLeft
import ru.cherryperry.instavideo.presentation.util.scaledRight
import ru.cherryperry.instavideo.presentation.util.scaledTop
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Provider
import kotlin.math.max
import kotlin.math.min

/**
 * Fragment with video editor.
 * Supports translate, zoom and cut.
 */
class EditorFragment : BaseFragment(), EditorView {

    companion object {

        private const val VIDEO_URI = "VideoUri"
        private const val ZOOM_MIN = 1f
        private const val ZOOM_MAX = 4f

        fun newBundle(uri: Uri): Bundle = Bundle().apply {
            putParcelable(VIDEO_URI, uri)
        }

        fun newInstance(uri: Uri): EditorFragment = EditorFragment().apply {
            arguments = newBundle(uri)
        }

        fun videoUri(bundle: Bundle): Uri = bundle.getParcelable(VIDEO_URI)!!
    }

    @Inject
    lateinit var storageAccessFramework: StorageAccessFramework
    @Inject
    lateinit var presenterProvider: Provider<EditorPresenter>
    @InjectPresenter
    lateinit var presenter: EditorPresenter

    private val constraintLayout by ViewDelegate<ConstraintLayout>(R.id.root, viewDelegateReset)
    private val limitView by ViewDelegate<View>(R.id.limitView, viewDelegateReset)
    private val textureView by ViewDelegate<TextureView>(R.id.textureView, viewDelegateReset)
    private val errorGroup by ViewDelegate<View>(R.id.errorGroup)
    private val loadingView by ViewDelegate<View>(R.id.loadingView)
    private val selectorView by ViewDelegate<TimeSelectorView>(R.id.timeSelector, viewDelegateReset)
    private val applyView by ViewDelegate<View>(R.id.apply, viewDelegateReset)

    private lateinit var player: SimpleExoPlayer

    override val layoutId: Int = R.layout.editor
    override val toolbarTitle: CharSequence?
        get() = getString(R.string.editor_title)

    @ProvidePresenter
    fun providePresenter() = presenterProvider.get()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initPlayer()
        initGestures()
        applyView.setOnClickListener { onApplyClick() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        storageAccessFramework.onActivityResultCreate(requestCode, resultCode, data)?.let {
            onOutputSelected(it)
        }
    }

    override fun onResume() {
        super.onResume()
        player.playWhenReady = true
    }

    override fun onPause() {
        super.onPause()
        player.playWhenReady = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        player.release()
    }

    override fun setVideoRatio(width: Long, height: Long) {
        (width <= 0) illegalArgument "Width can't be negative or zero"
        (height <= 0) illegalArgument "Height can't be negative or zero"
        val set = ConstraintSet()
        set.clone(constraintLayout)
        set.setDimensionRatio(textureView.id, "$width:$height")
        set.applyTo(constraintLayout)
    }

    override fun showVideo(uri: Uri, fromUs: Long, toUs: Long) {
        (fromUs == C.TIME_UNSET && toUs != C.TIME_UNSET ||
            fromUs != C.TIME_UNSET && toUs == C.TIME_UNSET) illegalArgument "Must be both TIME_UNSET"
        (fromUs != C.TIME_UNSET && fromUs < 0) illegalArgument "Start can't be negative"
        (toUs != C.TIME_UNSET && toUs < 0) illegalArgument "End can't be negative"
        (fromUs > toUs) illegalArgument "Start can't be later than end"
        val dataSourceFactory = DefaultDataSourceFactory(context, "No-Agent")
        val originalMediaSource = ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        val mediaSource = if (fromUs != C.TIME_UNSET) {
            ClippingMediaSource(originalMediaSource, fromUs, toUs)
        } else {
            originalMediaSource
        }
        player.prepare(mediaSource)
    }

    override fun showState(state: EditorView.State) {
        val set = ConstraintSet()
        set.clone(constraintLayout)
        when (state) {
            EditorView.State.LOADING -> {
                set.setVisibility(errorGroup.id, View.GONE)
                set.setVisibility(loadingView.id, View.VISIBLE)
                selectorView.isEnabled = false
                applyView.isEnabled = false
            }
            EditorView.State.NORMAL -> {
                set.setVisibility(errorGroup.id, View.GONE)
                set.setVisibility(loadingView.id, View.GONE)
                selectorView.isEnabled = true
                applyView.isEnabled = true
            }
            EditorView.State.ERROR -> {
                set.setVisibility(errorGroup.id, View.VISIBLE)
                set.setVisibility(loadingView.id, View.GONE)
                selectorView.isEnabled = false
                applyView.isEnabled = false
            }
        }
        set.applyTo(constraintLayout)
    }

    override fun limitSelection(range: Float) {
        selectorView.setLimit(range)
    }

    private fun initPlayer() {
        player = ExoPlayerFactory.newSimpleInstance(textureView.context)
        player.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException) {
                presenter.onPlayVideoError()
            }
        })
        player.setVideoTextureView(textureView)
        player.repeatMode = Player.REPEAT_MODE_ALL
        player.videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
        selectorView.selectionStartListener = { start, left, right ->
            if (start) {
                player.playWhenReady = false
            } else {
                player.playWhenReady = true
                presenter.onSelectionChanged(left, right)
            }
        }
    }

    private fun initGestures() {
        val scaleCallback = ScaleGestureCallback()
        val scaleGestureDetector = ScaleGestureDetector(context!!, scaleCallback)
        val gestureCallback = GestureCallback()
        val gestureDetector = GestureDetector(context!!, gestureCallback)
        limitView.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (!scaleCallback.scaling) {
                gestureDetector.onTouchEvent(event)
            }
            true
        }
    }

    private fun onApplyClick() {
        storageAccessFramework.create(this)
    }

    private fun onOutputSelected(uri: Uri) {
        val hitRect = Rect()
        textureView.getHitRect(hitRect)
        val textureScaledRectF = RectF(hitRect)
        textureScaledRectF.offset(0f, -limitView.top.toFloat())
        textureScaledRectF.left /= limitView.width
        textureScaledRectF.top /= limitView.height
        textureScaledRectF.right /= limitView.width
        textureScaledRectF.bottom /= limitView.height
        presenter.onOutputSelected(uri, textureScaledRectF)
    }

    /** Should be removed! Bad practice. */
    @VisibleForTesting
    fun checkPlayer() = player

    /** Callback for [ScaleGestureDetector] to scale up and down [textureView]. Limits it scale size. */
    private inner class ScaleGestureCallback : ScaleGestureDetector.SimpleOnScaleGestureListener() {

        private var scaleFactor = 1f
        private val scale = AtomicBoolean(false)

        val scaling
            get() = scale.get()

        override fun onScaleBegin(detector: ScaleGestureDetector?): Boolean {
            scale.set(true)
            return super.onScaleBegin(detector)
        }

        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = max(ZOOM_MIN, min(ZOOM_MAX, scaleFactor))
            textureView.scaleX = scaleFactor
            textureView.scaleY = scaleFactor
            return true
        }

        override fun onScaleEnd(detector: ScaleGestureDetector?) {
            super.onScaleEnd(detector)
            scale.set(false)
        }
    }

    /** Callback for [GestureDetector] to move [textureView]. Limits it's position to be not out of view bounds. */
    private inner class GestureCallback : GestureDetector.SimpleOnGestureListener() {

        private val safeZone = 1 dp context!!

        override fun onScroll(event1: MotionEvent, event2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {

            var translationX = textureView.translationX - distanceX
            if (textureView.scaledRight + translationX < limitView.left + safeZone) {
                translationX = limitView.left - textureView.scaledRight + safeZone
            } else if (textureView.scaledLeft + translationX > limitView.right - safeZone) {
                translationX = limitView.right - textureView.scaledLeft - safeZone
            }

            var translationY = textureView.translationY - distanceY
            if (textureView.scaledBottom + translationY < limitView.top + safeZone) {
                translationY = limitView.top - textureView.scaledBottom + safeZone
            } else if (textureView.scaledTop + translationY > limitView.bottom - safeZone) {
                translationY = limitView.bottom - textureView.scaledTop - safeZone
            }

            textureView.translationX = translationX
            textureView.translationY = translationY
            return true
        }
    }
}
