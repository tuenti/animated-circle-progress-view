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
package com.tuenti.animatedcircleprogressview.sample;

import android.app.Activity;
import android.os.Handler;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.tuenti.widget.AnimatedCircleProgressView;

public class MainActivity extends Activity {

	private AnimatedCircleProgressView mCircledAnimatedProgressView;
	private Handler mHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mCircledAnimatedProgressView = (AnimatedCircleProgressView) findViewById(R.id.progress);
		mHandler = new Handler();
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				mCircledAnimatedProgressView.setProgress(0.3f);
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						mCircledAnimatedProgressView.setIndeterminate();
					}
				}, 2000);
			}
		}, 2000);

		findViewById(R.id.increment).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mCircledAnimatedProgressView.setProgress(mCircledAnimatedProgressView.getProgress() + 0.2f);
			}
		});
		findViewById(R.id.decrement).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				float value = mCircledAnimatedProgressView.getProgress() - 0.2f;
				if (value < 0) {
					mCircledAnimatedProgressView.setIndeterminate();
				} else {
					mCircledAnimatedProgressView.setProgress(value);
				}
			}
		});
	}
}
