package com.emilsjolander.components.stickylistheaders;

import android.view.View;

public class ViewOperation {
	
	public ViewOperation(View view, boolean remove) {
		this.view = view;
		this.remove = remove;
	}
	
	View view;
	boolean remove;

}
