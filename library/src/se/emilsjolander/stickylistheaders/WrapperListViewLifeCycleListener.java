package se.emilsjolander.stickylistheaders;

import android.graphics.Canvas;

public interface WrapperListViewLifeCycleListener {

    public abstract void onDispatchDrawOccurred(Canvas canvas);

}