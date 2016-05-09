/*
 * Copyright (c) Tuenti Technologies S.L. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tuenti.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.View;

public abstract class AnimatedView extends View {

	private static final int MILLISECONDS_IN_NANOSECOND = 1000000;

	private long mPreviousDrawTimeInMillis = getCurrentTimeInMillis();

	public AnimatedView(Context context) {
		super(context);
	}

	public AnimatedView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public AnimatedView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		long currentTimeInMillis = getCurrentTimeInMillis();
		long elapsedTimeInMillis = currentTimeInMillis - mPreviousDrawTimeInMillis;
		mPreviousDrawTimeInMillis = currentTimeInMillis;

		onUpdate(elapsedTimeInMillis);
		onRender(canvas);

		if (isAnimationInProgress()) {
			invalidate();
		}
	}

	private long getCurrentTimeInMillis() {
		return System.nanoTime() / MILLISECONDS_IN_NANOSECOND;
	}

	/**
	 * Update current view draw state
	 *
	 * @param elapsedTimeInMillis time since the last frame
	 */
	protected abstract void onUpdate(long elapsedTimeInMillis);

	/**
	 * Render the view in its current state
	 *
	 * @param canvas the canvas on which the view will be drawn
	 */
	protected abstract void onRender(Canvas canvas);

	/**
	 * Indicates if the view is in a animation state, so the view needs to be redraw constantly
	 *
	 * @return true if an animation is in progress or false otherwise
	 */
	protected abstract boolean isAnimationInProgress();

	/**
	 * Will notify the view so there's a change that needs to start/stop an animation
	 */
	protected void setStateChanged() {
		mPreviousDrawTimeInMillis = getCurrentTimeInMillis();
		invalidate();
	}

}