package com.emilsjolander.components.stickylistheaders;

import android.content.Context;
import android.widget.Checkable;

/**
 *
 * @author Emil Sjölander
 *
 *
 *         Copyright 2012 Emil Sjölander
 *
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 *
 */
class CheckableWrapperView extends WrapperView implements Checkable {

	public CheckableWrapperView(final Context context) {
		super(context);
	}

	@Override
	public boolean isChecked() {
		return ((Checkable) item).isChecked();
	}

	@Override
	public void setChecked(final boolean checked) {
		((Checkable) item).setChecked(checked);
	}

	@Override
	public void toggle() {
		setChecked(!isChecked());
	}
}
