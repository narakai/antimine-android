package dev.lucasnlm.antimine.gdx

import android.content.Context
import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.input.GestureDetector
import com.badlogic.gdx.math.Vector2
import dev.lucasnlm.antimine.core.isAndroidTv
import dev.lucasnlm.antimine.core.isPortrait
import dev.lucasnlm.antimine.core.models.Area
import dev.lucasnlm.antimine.core.repository.IDimensionRepository
import dev.lucasnlm.antimine.gdx.actors.AreaForm
import dev.lucasnlm.antimine.gdx.models.GameTextures
import dev.lucasnlm.antimine.gdx.models.InternalPadding
import dev.lucasnlm.antimine.gdx.models.RenderSettings
import dev.lucasnlm.antimine.gdx.screens.MinefieldScreen
import dev.lucasnlm.antimine.gdx.shaders.BlurShader
import dev.lucasnlm.antimine.preferences.IPreferencesRepository
import dev.lucasnlm.antimine.preferences.models.Minefield
import dev.lucasnlm.antimine.ui.ext.blue
import dev.lucasnlm.antimine.ui.ext.green
import dev.lucasnlm.antimine.ui.ext.red
import dev.lucasnlm.antimine.ui.model.AppTheme

class LevelApplicationListener(
    private val context: Context,
    private val preferencesRepository: IPreferencesRepository,
    private val dimensionRepository: IDimensionRepository,
    private val theme: AppTheme,
    private val onSingleTouch: (Area) -> Unit,
    private val onLongTouch: (Area) -> Unit,
    private val crashLogger: (String) -> Unit,
    private val forceFreeScroll: Boolean,
) : ApplicationAdapter(), GestureDetector.GestureListener {

    private val assetManager = AssetManager()

    private val isPortrait = context.isPortrait()
    private var minefieldScreen: MinefieldScreen? = null
    private var boundAreas: List<Area> = listOf()
    private var boundMinefield: Minefield? = null

    private lateinit var batch: SpriteBatch
    private lateinit var mainFrameBuffer: FrameBuffer
    private lateinit var blurFrameBuffer: FrameBuffer
    private lateinit var blurShader: ShaderProgram

    private val renderSettings = RenderSettings(
        theme = theme,
        internalPadding = getInternalPadding(),
        areaSize = dimensionRepository.areaSize(),
        navigationBarHeight = dimensionRepository.navigationBarHeight().toFloat(),
        appBarWithStatusHeight = dimensionRepository.actionBarSizeWithStatus().toFloat(),
        appBarHeight = dimensionRepository.actionBarSize().toFloat(),
    )

    override fun create() {
        super.create()

        val width = Gdx.graphics.width
        val height = Gdx.graphics.height

        batch = SpriteBatch()
        mainFrameBuffer = FrameBuffer(Pixmap.Format.RGB888, width, height, false)
        blurFrameBuffer = FrameBuffer(Pixmap.Format.RGBA8888, width, height, false)

        blurShader = ShaderProgram(BlurShader.vert(), BlurShader.frag()).apply {
            bind()
            if (log.isNotBlank()) {
                crashLogger("Fail to compile shader. Error: $log")
            }

            setUniformf(BlurShader.resolution, width.toFloat())
        }

        assetManager.load(TextureConstants.atlasName, TextureAtlas::class.java)
        assetManager.finishLoading()

        minefieldScreen = MinefieldScreen(
            renderSettings = renderSettings,
            onSingleTouch = onSingleTouch,
            onLongTouch = onLongTouch,
            forceFreeScroll = forceFreeScroll,
        ).apply {
            bindField(boundAreas)
            bindSize(boundMinefield)
        }

        GdxLocal.run {
            val expectedSize = dimensionRepository.areaSize()
            val radiusLevel = preferencesRepository.squareRadius()
            val atlas = assetManager.get<TextureAtlas>(TextureConstants.atlasName)

            textureAtlas = atlas
            gameTextures = GameTextures(
                areaCovered = listOf(0, 1, 2).map {
                    AreaAssetBuilder.getAreaTexture(
                        expectedSize = expectedSize,
                        radiusLevel = radiusLevel,
                        qualityLevel = it,
                        color = theme.palette.covered,
                    )
                },
                areaCoveredOdd = listOf(0, 1, 2).map {
                    AreaAssetBuilder.getAreaTexture(
                        expectedSize = expectedSize,
                        radiusLevel = radiusLevel,
                        qualityLevel = it,
                        color = theme.palette.coveredOdd,
                    )
                },
                areaUncovered = listOf(0, 1, 2).map {
                    AreaAssetBuilder.getAreaTexture(
                        expectedSize = expectedSize,
                        radiusLevel = 1,
                        qualityLevel = it,
                        color = theme.palette.uncovered,
                    )
                },
                areaUncoveredOdd = listOf(0, 1, 2).map {
                    AreaAssetBuilder.getAreaTexture(
                        expectedSize = expectedSize,
                        radiusLevel = 1,
                        qualityLevel = it,
                        color = theme.palette.uncoveredOdd,
                    )
                },
                aroundMines = listOf(
                    atlas.findRegion(TextureConstants.around1),
                    atlas.findRegion(TextureConstants.around2),
                    atlas.findRegion(TextureConstants.around3),
                    atlas.findRegion(TextureConstants.around4),
                    atlas.findRegion(TextureConstants.around5),
                    atlas.findRegion(TextureConstants.around6),
                    atlas.findRegion(TextureConstants.around7),
                    atlas.findRegion(TextureConstants.around8),
                ),
                mine = atlas.findRegion(TextureConstants.mine),
                flag = atlas.findRegion(TextureConstants.flag),
                question = atlas.findRegion(TextureConstants.question),
                detailedArea = AreaAssetBuilder.getAreaTexture(
                    expectedSize = expectedSize,
                    radiusLevel = radiusLevel,
                    qualityLevel = 0,
                    color = theme.palette.covered,
                ),
                detailedAreaOdd = AreaAssetBuilder.getAreaTexture(
                    expectedSize = expectedSize,
                    radiusLevel = radiusLevel,
                    qualityLevel = 0,
                    color = theme.palette.coveredOdd,
                ),
                areaTextures = AreaForm.values().map {
                    it to AreaAssetBuilder.getAreaTextureForm(
                        areaForm = it,
                        expectedSize = expectedSize,
                        radiusLevel = radiusLevel,
                        qualityLevel = 0,
                        color = theme.palette.covered
                    )
                }.toMap(),
            )
        }

        Gdx.input.inputProcessor = InputMultiplexer(GestureDetector(this), minefieldScreen)
        Gdx.graphics.isContinuousRendering = false
    }

    override fun dispose() {
        super.dispose()
        blurShader.dispose()
        mainFrameBuffer.dispose()
        blurFrameBuffer.dispose()
        batch.dispose()

        GdxLocal.run {
            qualityZoomLevel = 0
            focusResizeLevel = 1.15f
            gameTextures?.run {
                detailedArea.dispose()
                detailedAreaOdd.dispose()
                areaCovered.forEach { it.dispose() }
                areaCoveredOdd.forEach { it.dispose() }
                areaUncovered.forEach { it.dispose() }
                areaUncoveredOdd.forEach { it.dispose() }
                areaTextures.forEach { (_, texture) -> texture.dispose() }
            }
            textureAtlas?.dispose()
            textureAtlas = null
        }

        Gdx.input.inputProcessor = null
        boundMinefield = null
        assetManager.dispose()
    }

    override fun render() {
        super.render()

        val width = Gdx.graphics.width
        val height = Gdx.graphics.height

        mainFrameBuffer.begin()
        minefieldScreen?.run {
            theme.palette.background.run {
                Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            }

            act()
            draw()
        }
        mainFrameBuffer.end()

        batch.run {
            begin()

            theme.palette.background.run {
                Gdx.gl.glClearColor(red(), green(), blue(), 1f)
                Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            }

            shader = blurShader.apply {
                setUniformf(BlurShader.direction, 1.0f, 1.0f)
                setUniformf(BlurShader.radius, 2.0f)

                if (isPortrait) {
                    setUniformf(BlurShader.blurTop, (1.0f - (renderSettings.appBarWithStatusHeight / height)))
                    setUniformf(BlurShader.blurBottom, (renderSettings.navigationBarHeight / height))
                    setUniformf(BlurShader.blurStart, 0.0f)
                    setUniformf(BlurShader.blurEnd, 1.0f)
                } else {
                    setUniformf(BlurShader.blurTop, 1.0f)
                    setUniformf(BlurShader.blurBottom, 0.0f)
                    setUniformf(BlurShader.blurStart, (renderSettings.appBarHeight / width))
                    setUniformf(BlurShader.blurEnd, 1.0f - (renderSettings.navigationBarHeight / width))
                }
            }

            draw(
                mainFrameBuffer.colorBufferTexture,
                0.0f,
                0.0f,
                0.0f,
                0.0f,
                width.toFloat(),
                height.toFloat(),
                1.0f,
                1.0f,
                0.0f,
                0,
                0,
                width,
                height,
                false,
                true
            )

            flush()
            end()
        }
    }

    private fun getInternalPadding(): InternalPadding {
        val padding = dimensionRepository.areaSize()
        return when {
            context.isAndroidTv() -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
            context.isPortrait() -> {
                InternalPadding(
                    start = padding,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
            else -> {
                InternalPadding(
                    start = 0f,
                    end = padding,
                    bottom = padding,
                    top = padding,
                )
            }
        }
    }

    fun bindMinefield(minefield: Minefield) {
        boundMinefield = minefield
        minefieldScreen?.bindSize(minefield)
        Gdx.graphics.requestRendering()
    }

    fun bindField(field: List<Area>) {
        boundAreas = field
        minefieldScreen?.bindField(field)
        Gdx.graphics.requestRendering()
    }

    fun setActionsEnabled(enabled: Boolean) {
    }

    override fun touchDown(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun tap(x: Float, y: Float, count: Int, button: Int): Boolean {
        return false
    }

    override fun longPress(x: Float, y: Float): Boolean {
        return false
    }

    override fun fling(velocityX: Float, velocityY: Float, button: Int): Boolean {
        return false
    }

    override fun pan(x: Float, y: Float, deltaX: Float, deltaY: Float): Boolean {
        return false
    }

    override fun panStop(x: Float, y: Float, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun zoom(initialDistance: Float, distance: Float): Boolean {
        GdxLocal.pressedArea = GdxLocal.pressedArea?.copy(consumed = true)
        minefieldScreen?.changeZoom(initialDistance / distance)
        return true
    }

    override fun pinch(
        initialPointer1: Vector2?,
        initialPointer2: Vector2?,
        pointer1: Vector2?,
        pointer2: Vector2?
    ): Boolean {
        if (pointer1 != null && pointer2 != null && initialPointer1 != null && initialPointer2 != null) {
            minefieldScreen?.let {
                val mid1 = pointer1.cpy().add(pointer2).scl(0.5f)
                val mid2 = initialPointer1.cpy().add(initialPointer2).scl(0.5f)
                val delta = mid1.sub(mid2)

                it.camera.position.set(mid1.x, mid1.y, 0f)
            }
        }
        return false
    }

    override fun pinchStop() {
        // Empty
    }
}
