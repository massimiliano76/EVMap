package net.vonforst.evmap.auto

import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.Surface
import com.google.android.libraries.car.app.*
import com.google.android.libraries.car.app.model.*
import com.google.android.libraries.car.app.model.Distance.UNIT_KILOMETERS
import com.google.android.libraries.car.app.navigation.model.PlaceListNavigationTemplate


class CarAppService : com.google.android.libraries.car.app.CarAppService() {
    override fun onCreateScreen(intent: Intent): Screen {
        carContext.getCarService(AppManager::class.java).setSurfaceListener(MapSurfaceListener())
        return MapScreen(carContext)
    }
}

class MapScreen(ctx: CarContext) : Screen(ctx) {
    override fun getTemplate(): Template {
        val distance = SpannableStringBuilder()
            .append(
                "distance",
                DistanceSpan.create(Distance.create(1.0, UNIT_KILOMETERS)),
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
        val item = Row.builder()
            .setTitle("Example Charger 1")
            .addText(distance)
            .build()
        return PlaceListNavigationTemplate.builder()
            .setTitle("EVMap")
            .setItemList(
                ItemList.builder()
                    .addItem(item)
                    .build()
            )
            .build()
    }
}

class MapSurfaceListener : SurfaceListener {
    private var surfaceContainer: SurfaceContainer? = null
    private var surface: Surface? = null

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        this.surfaceContainer = surfaceContainer
        this.surface = surfaceContainer.surface
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        renderFrame()
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        renderFrame()
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        surface = null
    }

    private fun renderFrame() {
        val s = surface
        if (s == null || !s.isValid()) {
            // Surface is not available, or has been destroyed, skip this frame.
            return;
        }

        val canvas = s.lockCanvas(null)
        canvas.drawColor(Color.LTGRAY)
        s.unlockCanvasAndPost(canvas);
    }

}