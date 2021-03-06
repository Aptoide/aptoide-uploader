package cm.aptoide.aptoideviews.recyclerview

import android.content.Context
import androidx.annotation.Dimension
import androidx.annotation.Px
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.GridLayoutAnimationController
import com.aptoide.uploader.recyclerView.safeLet

/**
 * An extension of a RecyclerView to make sure that grid animations work correctly and to
 * smartly layout the items according to the screen size.
 */
class GridRecyclerView : androidx.recyclerview.widget.RecyclerView {

  private data class Size(val width: Int, val height: Int) {
    fun getRatio(): Double {
      return width / height.toDouble()
    }
  }

  private var adaptStrategy: AdaptStrategy? = null
  private var intendedItemSize: Size? = null
  private var spacingItemDecorator = GridItemSpacingDecorator()

  constructor(context: Context) : this(context, null)
  constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs,
      defStyleAttr)

  override fun setLayoutManager(layout: LayoutManager?) {
    if (layout is androidx.recyclerview.widget.GridLayoutManager) {
      super.setLayoutManager(layout)
    } else {
      throw ClassCastException(
          "This GridRecyclerView should only be used with a CustomGridLayoutManager")
    }
  }

  override fun attachLayoutAnimationParameters(child: View?, params: ViewGroup.LayoutParams,
                                               index: Int, count: Int) {
    val layoutManager = layoutManager
    if (adapter != null && layoutManager is androidx.recyclerview.widget.GridLayoutManager) {

      var animationParams: GridLayoutAnimationController.AnimationParameters? =
          params.layoutAnimationParameters as? GridLayoutAnimationController.AnimationParameters

      if (animationParams == null) {
        animationParams = GridLayoutAnimationController.AnimationParameters()
        params.layoutAnimationParameters = animationParams
      }

      val columns = layoutManager.spanCount

      animationParams.count = count
      animationParams.index = index
      animationParams.columnsCount = columns
      animationParams.rowsCount = count / columns

      val invertedIndex = count - 1 - index
      animationParams.column = columns - 1 - invertedIndex % columns
      animationParams.row = animationParams.rowsCount - 1 - invertedIndex / columns

    } else {
      super.attachLayoutAnimationParameters(child, params, index, count)
    }
  }

  override fun onChildAttachedToWindow(child: View) {
    // If we have adaptive layout enabled, modify the item size according to the span count & spacing set
    safeLet(intendedItemSize, adaptStrategy) { itemSize, adaptStrategy ->
      val childLayoutParams = child.layoutParams
      if (adaptStrategy == AdaptStrategy.SCALE_WIDTH_ONLY || adaptStrategy == AdaptStrategy.SCALE_KEEP_ASPECT_RATIO || adaptStrategy == AdaptStrategy.ADJUST_PADDING) {
        // MATCH_PARENT is equivalent to getItemMeasuredWidth(), but the later does not set correctly
        // for some reason
        childLayoutParams.width = MATCH_PARENT
      }
      if (adaptStrategy == AdaptStrategy.SCALE_KEEP_ASPECT_RATIO) {
        childLayoutParams.height = (getItemMeasuredWidth() * (1.0 / itemSize.getRatio())).toInt()
      }
    } ?: super.onChildAttachedToWindow(child)
  }

  override fun onMeasure(widthSpec: Int, heightSpec: Int) {
    super.onMeasure(widthSpec, heightSpec)
    safeLet(intendedItemSize, adaptStrategy) { itemSize, adaptStrategy ->
      // If we have adaptive layout enabled, let's attempt to get the appropriate span count
      val manager = (layoutManager as androidx.recyclerview.widget.GridLayoutManager)
      val itemSpacing = spacingItemDecorator.spacingPx
      manager.spanCount =
          ((getTotalWidth() - getTotalHorizontalPadding() + itemSpacing) / (itemSize.width + itemSpacing.toDouble())).toInt()

      if (adaptStrategy == AdaptStrategy.ADJUST_PADDING) {
        val padding = getAdaptiveMeasuredPadding(itemSize.width) / 2
        setPadding(padding, paddingTop, padding, paddingBottom)
      }
    }
  }

  fun setAdaptiveLayout(@Dimension(unit = Dimension.DP) intendedItemWidth: Int,
                        @Dimension(unit = Dimension.DP) intendedItemHeight: Int,
                        strategy: AdaptStrategy) {
    intendedItemSize = Size(dpToPx(intendedItemWidth), dpToPx(intendedItemHeight))
    adaptStrategy = strategy
  }

  enum class AdaptStrategy {
    /**
     * Scales items width to fit the screen only
     */
    SCALE_WIDTH_ONLY,
    /**
     * Scales items width and height (according to their aspect ratio) to fit the screen
     */
    SCALE_KEEP_ASPECT_RATIO,
    /**
     * Does not scale items but rather adjusts outer padding (left and right)
     * while maintaining the original spacing to fit the screen
     */
    ADJUST_PADDING
  }

  fun setIntendedItemSpacing(@Dimension(unit = Dimension.DP) spacing: Int) {
    removeItemDecoration(spacingItemDecorator)
    spacingItemDecorator.spacingPx = dpToPx(spacing)

    if (spacing > 0) {
      addItemDecoration(spacingItemDecorator)
    }
  }

  /**
   * Returns true if the user has scrolled to the start of the [thresholdRow]-last row.
   * E.g. if [thresholdRow] is set to 3, it returns true if the user has reached the third-last row.
   */
  fun isEndReached(thresholdRow: Int): Boolean {
    val lManager = layoutManager as androidx.recyclerview.widget.GridLayoutManager
    return (lManager.itemCount - (lManager.spanCount * thresholdRow)) <= lManager.findLastCompletelyVisibleItemPosition()
  }

  @Px
  private fun getItemMeasuredWidth(): Int {
    val spanCount = (layoutManager as androidx.recyclerview.widget.GridLayoutManager).spanCount
    val itemSpacing = spacingItemDecorator.spacingPx
    return (((getTotalWidth() - getTotalHorizontalPadding() + itemSpacing) / spanCount.toDouble()) - itemSpacing).toInt()
  }

  @Px
  private fun getAdaptiveMeasuredPadding(itemWidth: Int): Int {
    val spanCount = (layoutManager as androidx.recyclerview.widget.GridLayoutManager).spanCount
    val itemSpacing = spacingItemDecorator.spacingPx
    return (getTotalWidth() - ((itemWidth + itemSpacing) * spanCount.toDouble()) + itemSpacing).toInt()
  }

  @Px
  private fun getTotalHorizontalPadding(): Int {
    return paddingLeft + paddingRight
  }

  @Px
  private fun getTotalWidth(): Int {
    return if (width > 0) width else context.resources.displayMetrics.widthPixels
  }

  @Px
  private fun dpToPx(@Dimension(unit = Dimension.DP) dp: Int): Int {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(),
        resources.displayMetrics).toInt()
  }

}