package com.aptoide.uploader.apps.view;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

// from https://stackoverflow.com/a/28533234/1718068
public class GridDividerItemDecoration extends RecyclerView.ItemDecoration {

  private final int space;

  public GridDividerItemDecoration(int space) {
    this.space = space;
  }

  @Override public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
      RecyclerView.State state) {
    outRect.left = space;
    outRect.right = space;
    outRect.bottom = space;
    outRect.top = space;
  }
}
