package net.vonforst.evmap.auto

import android.content.Intent
import android.graphics.Rect
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.view.Surface
import android.view.View
import com.google.android.libraries.car.app.*
import com.google.android.libraries.car.app.model.*
import com.google.android.libraries.car.app.model.Distance.UNIT_KILOMETERS
import com.google.android.libraries.car.app.navigation.model.PlaceListNavigationTemplate
import com.google.android.libraries.maps.GoogleMap
import com.google.android.libraries.maps.GoogleMapOptions
import com.google.android.libraries.maps.MapView


class CarAppService : com.google.android.libraries.car.app.CarAppService() {
    override fun onCreateScreen(intent: Intent): Screen {
        carContext.getCarService(AppManager::class.java)
            .setSurfaceListener(MapSurfaceListener(carContext))
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

class MapSurfaceListener(val carContext: CarContext) : SurfaceListener {
    private var surfaceContainer: SurfaceContainer? = null
    private var surface: Surface? = null
    private var mapView: MapView? = null
    private var map: GoogleMap? = null

    override fun onSurfaceAvailable(surfaceContainer: SurfaceContainer) {
        this.surfaceContainer = surfaceContainer
        this.surface = surfaceContainer.surface

        this.map = null
        val mapView = MapView(carContext, GoogleMapOptions().liteMode(true))
        mapView.onCreate(null)
        mapView.onStart()
        mapView.onResume()

        mapView.getMapAsync { map ->
            map.setOnMapLoadedCallback {
                this.map = map
                renderFrame()
            }
        }

        mapView.measure(
            View.MeasureSpec.makeMeasureSpec(surfaceContainer.width, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(surfaceContainer.height, View.MeasureSpec.EXACTLY)
        )
        mapView.layout(0, 0, surfaceContainer.width, surfaceContainer.height)

        this.mapView = mapView
    }

    override fun onStableAreaChanged(stableArea: Rect) {
        mapView?.setPadding(stableArea.left, stableArea.top, stableArea.right, stableArea.bottom)
        renderFrame()
    }

    override fun onVisibleAreaChanged(visibleArea: Rect) {
        renderFrame()
    }

    override fun onSurfaceDestroyed(surfaceContainer: SurfaceContainer) {
        surface = null
        mapView?.onPause()
        mapView?.onStop()
        mapView?.onDestroy()
    }

    private fun renderFrame() {
        val s = surface
        if (s == null || !s.isValid) {
            // Surface is not available, or has been destroyed, skip this frame.
            return;
        }

        map?.snapshot {
            val canvas = s.lockCanvas(null)
            canvas.drawBitmap(it, 0f, 0f, null)
            s.unlockCanvasAndPost(canvas);
        }
    }

}