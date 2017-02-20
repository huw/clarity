package nu.huw.clarity.ui.adapter;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class DetailViewPager extends ViewPager {

  private PagerAdapter pagerAdapter;

  public DetailViewPager(Context context) {
    super(context);
  }

  public DetailViewPager(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (pagerAdapter != null) {
      super.setAdapter(pagerAdapter);
    }
  }

  @Override
  public void setAdapter(PagerAdapter adapter) {
  }

  public void storeAdapter(PagerAdapter pagerAdapter) {
    this.pagerAdapter = pagerAdapter;
  }
}
