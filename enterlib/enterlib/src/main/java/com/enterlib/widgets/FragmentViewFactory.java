package com.enterlib.widgets;

import android.os.Bundle;

public class FragmentViewFactory {
	  private final Class<?> clss;
      private final Bundle args;
      
      public FragmentViewFactory(Class<?> _class, Bundle _args) {
          clss = _class;
          args = _args;
      }

	public Class<?> getClss() {
		return clss;
	}

	public Bundle getArgs() {
		return args;
	}
      
      

}
