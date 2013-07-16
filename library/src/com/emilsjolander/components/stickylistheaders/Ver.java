package com.emilsjolander.components.stickylistheaders;

import android.os.Build;

public final class Ver {
	
	public static boolean froyo(){
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO);
	}
	
	public static boolean honeycomb(){
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB);
	}
	
	private Ver(){}

}
