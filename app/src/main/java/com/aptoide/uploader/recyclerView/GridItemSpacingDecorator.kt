package cm.aptoide.aptoideviews.recyclerview

import android.graphics.Rect
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.View

class GridItemSpacingDecorator(@Px var spacingPx: Int = 0) : androidx.recyclerview.widget.RecyclerView.ItemDecoration() {

  override fun getItemOffsets(outRect: Rect, view: View, parent: androidx.recyclerview.widget.RecyclerView,
                              state: androidx.recyclerview.widget.RecyclerView.State) {
    outRect.setEmpty()

    val layout = parent.layoutManager as androidx.recyclerview.widget.GridLayoutManager

    val position = parent.getChildAdapterPosition(view)
    val row: Int = position / layout.spanCount

    val marginLeft = if (position % layout.spanCount != 0) spacingPx else 0
    val marginTop = if (row != 0) spacingPx else 0

    outRect.set(marginLeft, marginTop, 0, 0)
  }
}