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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

public class AnimatedCircleProgressView extends AnimatedView {

	private static final int STATE_INDETERMINATE = 0;
	private static final int STATE_TRANSITION_FROM_INDETERMINATE_TO_PROGRESS = 1;
	private static final int STATE_ANIM_PROGRESS = 2;
	private static final int STATE_PROGRESS = 3;
	private static final int STATE_TRANSITION_FROM_PROGRESS_TO_INDETERMINATE = 4;

	private static final float INDETERMINATE_ANIMATION_DURATION = 500;
	private static final float UPDATE_PROGRESS_ANIMATION_DURATION = 1500;
	private static final float REDUCE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION = 500;
	private static final float MOVE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION = 500;

	private static final int STATE_PHASE_0 = 0;
	private static final int STATE_PHASE_1 = 1;
	private static final int STATE_PHASE_2 = 2;
	private static final int STATE_PHASE_3 = 3;
	private static final int STATE_PHASE_4 = 4;

	private static final float NEAR_ZERO = 0.0001f;

	private static final float MIN_PROGRESS = 0;
	private static final float MAX_PROGRESS = 2.0f;

	private static final int DEFAULT_INDETERMINATE_COLOR = 0xFF00FF00;
	private static final int DEFAULT_PROGRESS_PENDING_COLOR = 0x6000FF00;
	private static final int DEFAULT_PROGRESS_FIRST_PHASE_COLOR = 0xFF0000FF;
	private static final int DEFAULT_PROGRESS_SECOND_PHASE_COLOR = 0xFFFF0000;
	private static final float DEFAULT_PROGRESS_VALUE = 0.0f;
	private static final float DEFAULT_PROGRESS_STROKE_WIDTH = 16;

	private static final float ARC_OFFSET_DEGREES = -90;
	private static final float ARC_INITIAL_DEGREES = 0;
	private static final float ARC_TARGET_DEGREES = 360;

	private int mState = STATE_INDETERMINATE;
	private int mStatePhase = STATE_PHASE_0;

	private float mCenterX;
	private float mCenterY;

	//Indeterminate circle model
	private float mIndeterminateCircleInitialRadius;
	private float mIndeterminateCircleCurrentRadius;
	private float mIndeterminateCircleTargetRadius;
	private float mIndeterminateCircleInitialY;
	private float mIndeterminateCircleCurrentY;
	private float mIndeterminateCircleMinimumRadius;
	private float mIndeterminateCircleDeltaRadius;
	private int mIndeterminateCircleDirection = 1;

	//Pending progress arc model
	private float mPendingArcInitial;
	private float mPendingArcCurrent;

	//Progress model
	private float mProgress;

	//Progress arc model
	private RectF mProgressArcBounds;
	private float mProgressArcInitialAngle;
	private float mProgressArcCurrentAngle;

	//Animation mState
	private long mAnimationAbsoluteTime;
	private long mProgressAnimationAbsoluteTime;
	private float mRelativeProgress;

	//Resource
	private int mIndeterminateColor;
	private int mProgressPendingColor;
	private int mProgressFirstPhaseColor;
	private int mProgressSecondPhaseColor;

	//Paint
	private Paint mIndeterminatePaint;
	private Paint mProgressFirstPhasePaint;
	private Paint mProgressSecondPhasePaint;
	private Paint mProgressPendingPaint;

	//Dimen
	private float mProgressStrokeWidth;

	//Interpolator
	private DecelerateInterpolator mAccelerateInterpolator;
	private AccelerateDecelerateInterpolator mAccelerateDecelerateInterpolator;

	//Listeners
	private OnAnimationEndListener mOnAnimationEndListener;

	public AnimatedCircleProgressView(Context context) {
		super(context);
		initialize();
	}

	public AnimatedCircleProgressView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initAttributes(context, attrs);
		initialize();
	}

	public AnimatedCircleProgressView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		initAttributes(context, attrs);
		initialize();
	}

	@TargetApi(VERSION_CODES.LOLLIPOP)
	public AnimatedCircleProgressView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		initAttributes(context, attrs);
		initialize();
	}

	private void initAttributes(Context context, AttributeSet attrs) {
		if (attrs != null) {
			TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AnimatedCircleProgressView, 0, 0);
			try {
				initColorAttributes(a);
				initValueAttributes(a);
				initDimenAttributes(a);
			} finally {
				a.recycle();
			}
		}
	}

	private void initColorAttributes(TypedArray a) {
		mIndeterminateColor = a.getColor(R.styleable.AnimatedCircleProgressView_indeterminate_color, DEFAULT_INDETERMINATE_COLOR);
		mProgressPendingColor = a.getColor(R.styleable.AnimatedCircleProgressView_progress_pending_color, DEFAULT_PROGRESS_PENDING_COLOR);
		mProgressFirstPhaseColor = a.getColor(R.styleable.AnimatedCircleProgressView_progress_first_phase_color, DEFAULT_PROGRESS_FIRST_PHASE_COLOR);
		mProgressSecondPhaseColor = a.getColor(R.styleable.AnimatedCircleProgressView_progress_second_phase_color, DEFAULT_PROGRESS_SECOND_PHASE_COLOR);
	}

	private void initValueAttributes(TypedArray a) {
		mProgress = a.getFloat(R.styleable.AnimatedCircleProgressView_progress, DEFAULT_PROGRESS_VALUE);
	}

	private void initDimenAttributes(TypedArray a) {
		mProgressStrokeWidth = a.getDimension(R.styleable.AnimatedCircleProgressView_progress_stroke_width, DEFAULT_PROGRESS_STROKE_WIDTH);
	}

	private void initialize() {
		mIndeterminatePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mIndeterminatePaint.setColor(mIndeterminateColor);
		mIndeterminatePaint.setStyle(Style.FILL);

		mProgressPendingPaint = buildProgressPaint(mProgressPendingColor);
		mProgressFirstPhasePaint = buildProgressPaint(mProgressFirstPhaseColor);
		mProgressSecondPhasePaint = buildProgressPaint(mProgressSecondPhaseColor);

		mAccelerateInterpolator = new DecelerateInterpolator();
		mAccelerateDecelerateInterpolator = new AccelerateDecelerateInterpolator();

		mProgressArcBounds = new RectF();
	}

	public Paint buildProgressPaint(int color) {
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setColor(color);
		paint.setStyle(Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setStrokeWidth(mProgressStrokeWidth);
		return paint;
	}

	public void setIndeterminate() {
		setState(STATE_INDETERMINATE);

		setStateChanged();
	}

	public float getProgress() {
		return mProgress;
	}

	public void setProgress(float progress) {
		setState(STATE_ANIM_PROGRESS);
		mProgressArcInitialAngle = mProgressArcCurrentAngle > NEAR_ZERO ? mProgressArcCurrentAngle : ARC_INITIAL_DEGREES;

		if (progress > MAX_PROGRESS) {
			mProgress = MAX_PROGRESS;
		} else if (progress < MIN_PROGRESS) {
			mProgress = MIN_PROGRESS;
		} else {
			mProgress = progress;
		}

		setStateChanged();
	}

	@Override
	protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
		mCenterX = width * 0.5f;
		mCenterY = height * 0.5f;

		float radius = Math.min(mCenterX, mCenterY);
		mIndeterminateCircleMinimumRadius = radius * 0.8f;
		mIndeterminateCircleDeltaRadius = radius - mIndeterminateCircleMinimumRadius;

		mProgressArcBounds.left = mCenterX - radius + mProgressStrokeWidth;
		mProgressArcBounds.right = mCenterX + radius - mProgressStrokeWidth;
		mProgressArcBounds.top = mCenterY - radius + mProgressStrokeWidth;
		mProgressArcBounds.bottom = mCenterY + radius - mProgressStrokeWidth;
		mIndeterminateCircleCurrentY = mCenterY;

		mIndeterminateCircleTargetRadius = (mProgressStrokeWidth * 0.5f);
	}

	@Override
	protected void onUpdate(long elapsedTimeInMillis) {
		mAnimationAbsoluteTime += elapsedTimeInMillis;
		switch (mState) {
			case STATE_INDETERMINATE:
				updateIndeterminateAnim();
				break;
			case STATE_TRANSITION_FROM_INDETERMINATE_TO_PROGRESS:
				updateTransitionFromIndeterminateToProgress(elapsedTimeInMillis);
				break;
			case STATE_ANIM_PROGRESS:
				updateProgress(elapsedTimeInMillis);
				break;
			case STATE_TRANSITION_FROM_PROGRESS_TO_INDETERMINATE:
				updateTransitionFromProgressToIndeterminate(elapsedTimeInMillis);
				break;
		}
	}

	private void updateIndeterminateAnim() {
		if (mAnimationAbsoluteTime > INDETERMINATE_ANIMATION_DURATION) {
			mIndeterminateCircleDirection = -mIndeterminateCircleDirection;
			mAnimationAbsoluteTime -= INDETERMINATE_ANIMATION_DURATION;
		}

		float initialValue = mIndeterminateCircleMinimumRadius;
		float finalValue = mIndeterminateCircleMinimumRadius;
		if (mIndeterminateCircleDirection > 0) {
			finalValue += mIndeterminateCircleDeltaRadius;
		} else {
			initialValue += mIndeterminateCircleDeltaRadius;
		}
		mIndeterminateCircleCurrentRadius = interpolateAccelerateAnimation(INDETERMINATE_ANIMATION_DURATION,
				initialValue,
				finalValue);
	}

	private void updateTransitionFromIndeterminateToProgress(long elapsedTimeInMillis) {
		switch (mStatePhase) {
			case STATE_PHASE_0:
				initAnimationStateToTransitionFromIndeterminateToProgress();
			case STATE_PHASE_1:
				reduceRadiusToProgressCircleWidth(elapsedTimeInMillis);
				break;
			case STATE_PHASE_2:
				moveCircleToArcInitialPoint(elapsedTimeInMillis);
				break;
			case STATE_PHASE_3:
				expandPendingAndCurrentProgressArcs(elapsedTimeInMillis);
				break;
			case STATE_PHASE_4:
				transitionToProgressState(elapsedTimeInMillis);
				break;
		}
	}

	private void initAnimationStateToTransitionFromIndeterminateToProgress() {
		mIndeterminateCircleInitialRadius = mIndeterminateCircleCurrentRadius;
		mPendingArcInitial = ARC_INITIAL_DEGREES;
		mStatePhase = STATE_PHASE_1;
	}

	private void reduceRadiusToProgressCircleWidth(long elapsedTimeInMillis) {
		mIndeterminateCircleCurrentRadius = interpolateAccelerateDecelerateAnimation(REDUCE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mIndeterminateCircleInitialRadius,
				mIndeterminateCircleTargetRadius);

		if (isAnimationPhaseFinished()) {
			mIndeterminateCircleCurrentRadius = mIndeterminateCircleTargetRadius;
			mIndeterminateCircleInitialY = mIndeterminateCircleCurrentY;
			mStatePhase = STATE_PHASE_2;
			mAnimationAbsoluteTime = 0;
			updateTransitionFromIndeterminateToProgress(elapsedTimeInMillis);
		}
	}

	private void moveCircleToArcInitialPoint(long elapsedTimeInMillis) {
		mIndeterminateCircleCurrentY = interpolateAccelerateDecelerateAnimation(MOVE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mIndeterminateCircleInitialY,
				mProgressArcBounds.top);

		if (isAnimationPhaseFinished()) {
			mIndeterminateCircleCurrentY = mProgressArcBounds.top;
			mStatePhase = STATE_PHASE_3;
			mAnimationAbsoluteTime = 0;
			updateTransitionFromIndeterminateToProgress(elapsedTimeInMillis);
		}
	}

	private void expandPendingAndCurrentProgressArcs(long elapsedTimeInMillis) {
		mPendingArcCurrent = interpolateAccelerateDecelerateAnimation(MOVE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mPendingArcInitial,
				ARC_TARGET_DEGREES);
		updateProgress(elapsedTimeInMillis);

		if (isAnimationPhaseFinished()) {
			mPendingArcCurrent = ARC_TARGET_DEGREES;
			mStatePhase = STATE_PHASE_4;
			mAnimationAbsoluteTime = 0;
			updateTransitionFromIndeterminateToProgress(elapsedTimeInMillis);
		}
	}

	private void transitionToProgressState(long elapsedTimeInMillis) {
		if (mOnAnimationEndListener != null) {
			mOnAnimationEndListener.onTransitionToProgressEnd();
		}
		setState(STATE_ANIM_PROGRESS);
		mIndeterminateCircleCurrentRadius = -1;
		updateProgress(elapsedTimeInMillis);
	}

	private void updateProgress(long elapsedTimeInMillis) {
		mProgressAnimationAbsoluteTime += elapsedTimeInMillis;
		float animationRelativeProgress = mProgressAnimationAbsoluteTime / UPDATE_PROGRESS_ANIMATION_DURATION;
		if (Math.abs(mProgressArcCurrentAngle - mProgress) < NEAR_ZERO && mState == STATE_ANIM_PROGRESS) {
			if (mOnAnimationEndListener != null) {
				mOnAnimationEndListener.onProgressEnd();
			}
			setState(STATE_PROGRESS);
			return;
		}

		float progressToAnimate = mAccelerateInterpolator.getInterpolation(animationRelativeProgress);
		mProgressArcCurrentAngle = ((mProgress - mProgressArcInitialAngle) * progressToAnimate) + mProgressArcInitialAngle;
		if (Math.abs(mProgressArcCurrentAngle - mProgress) < NEAR_ZERO) {
			if (mProgressArcInitialAngle < mProgress) {
				mProgressArcCurrentAngle = mProgress;
			} else if (mProgress < mProgressArcInitialAngle) {
				mProgressArcCurrentAngle = mProgress;
			}
		}
	}

	private void updateTransitionFromProgressToIndeterminate(long elapsedTimeInMillis) {
		switch (mStatePhase) {
			case STATE_PHASE_0:
				initAnimationStateToTransitionFromProgressToIndeterminate();
			case STATE_PHASE_1:
				reducePendingAndProgressArc(elapsedTimeInMillis);
				break;
			case STATE_PHASE_2:
				moveIndeterminateCircleToCenter(elapsedTimeInMillis);
				break;
			case STATE_PHASE_3:
				expandIndeterminateCircle(elapsedTimeInMillis);
				break;
			case STATE_PHASE_4:
				transitionToIndeterminateState();
				break;
		}
	}

	private void initAnimationStateToTransitionFromProgressToIndeterminate() {
		mProgress = 0;
		mProgressArcInitialAngle = ARC_INITIAL_DEGREES;
		mPendingArcInitial = mPendingArcCurrent;
		mStatePhase = STATE_PHASE_1;
	}

	private void reducePendingAndProgressArc(long elapsedTimeInMillis) {
		mPendingArcCurrent = interpolateAccelerateDecelerateAnimation(MOVE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mPendingArcInitial,
				ARC_INITIAL_DEGREES);
		updateProgress(elapsedTimeInMillis);

		if (isAnimationPhaseFinished()) {
			mPendingArcCurrent = ARC_INITIAL_DEGREES;
			mStatePhase = STATE_PHASE_2;
			mAnimationAbsoluteTime = 0;
			mIndeterminateCircleCurrentRadius = mIndeterminateCircleTargetRadius;
			updateTransitionFromProgressToIndeterminate(elapsedTimeInMillis);
		}
	}

	private void moveIndeterminateCircleToCenter(long elapsedTimeInMillis) {
		mIndeterminateCircleCurrentY = interpolateAccelerateDecelerateAnimation(MOVE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mProgressArcBounds.top,
				mCenterY);

		if (isAnimationPhaseFinished()) {
			mIndeterminateCircleCurrentY = mCenterY;
			mStatePhase = STATE_PHASE_3;
			mAnimationAbsoluteTime = 0;
			updateTransitionFromProgressToIndeterminate(elapsedTimeInMillis);
		}
	}

	private void expandIndeterminateCircle(long elapsedTimeInMillis) {
		mIndeterminateCircleCurrentRadius = interpolateAccelerateDecelerateAnimation(REDUCE_INDETERMINATE_CIRCLE_TO_PROGRESS_DURATION,
				mIndeterminateCircleTargetRadius,
				mIndeterminateCircleMinimumRadius);

		if (isAnimationPhaseFinished()) {
			mIndeterminateCircleCurrentRadius = mIndeterminateCircleMinimumRadius;
			mIndeterminateCircleInitialY = mCenterY;
			mStatePhase = STATE_PHASE_4;
			mAnimationAbsoluteTime = 0;
			updateTransitionFromProgressToIndeterminate(elapsedTimeInMillis);
		}
	}

	private void transitionToIndeterminateState() {
		if (mOnAnimationEndListener != null) {
			mOnAnimationEndListener.onTransitionToIndeterminateEnd();
		}
		setState(STATE_INDETERMINATE);
		updateIndeterminateAnim();
	}

	private boolean isAnimationPhaseFinished() {
		return mRelativeProgress >= 1.0f;
	}

	private float interpolateAccelerateDecelerateAnimation(float animationDuration,
			float initialValue,
			float finalValue) {
		return interpolateAnimation(animationDuration, initialValue, finalValue, mAccelerateDecelerateInterpolator);
	}

	private float interpolateAccelerateAnimation(float animationDuration,
			float initialValue,
			float finalValue) {
		return interpolateAnimation(animationDuration, initialValue, finalValue, mAccelerateInterpolator);
	}

	private float interpolateAnimation(float animationDuration, float initialValue, float finalValue, Interpolator interpolator) {
		mRelativeProgress = mAnimationAbsoluteTime / animationDuration;
		float interpolatedProgress = interpolator.getInterpolation(mRelativeProgress);
		float totalMovementNeeded = finalValue - initialValue;
		return initialValue + (interpolatedProgress * totalMovementNeeded);
	}

	private void setState(int newState) {
		switch (mState) {
			case STATE_PROGRESS:
			case STATE_ANIM_PROGRESS:
				switch (newState) {
					case STATE_INDETERMINATE:
						newState = STATE_TRANSITION_FROM_PROGRESS_TO_INDETERMINATE;
						mStatePhase = STATE_PHASE_0;
						mAnimationAbsoluteTime = 0;
						break;
				}
				mProgressAnimationAbsoluteTime = 0;
				break;
			case STATE_INDETERMINATE:
				switch (newState) {
					case STATE_INDETERMINATE:
						return;
					case STATE_ANIM_PROGRESS:
					case STATE_PROGRESS:
						newState = STATE_TRANSITION_FROM_INDETERMINATE_TO_PROGRESS;
						mStatePhase = STATE_PHASE_0;
						mAnimationAbsoluteTime = 0;
						break;
				}
				break;
			case STATE_TRANSITION_FROM_INDETERMINATE_TO_PROGRESS:
			case STATE_TRANSITION_FROM_PROGRESS_TO_INDETERMINATE:
				if (!isAnimationPhaseFinished()) {
					return;
				}
				mAnimationAbsoluteTime = 0;
				break;
		}

		mState = newState;
	}

	@Override
	protected void onRender(Canvas canvas) {
		renderInactive(canvas);
		renderProgress(canvas);
	}

	private void renderInactive(Canvas canvas) {
		if (mIndeterminateCircleCurrentRadius > 0) {
			canvas.drawCircle(mCenterX, mIndeterminateCircleCurrentY, mIndeterminateCircleCurrentRadius, mIndeterminatePaint);
		}
	}

	private void renderProgress(Canvas canvas) {
		float firstPhase, secondPhase;

		if (mProgressArcCurrentAngle >= 1.0f) {
			firstPhase = 2.0f - mProgressArcCurrentAngle;
			secondPhase = mProgressArcCurrentAngle - 1.0f;
		} else {
			firstPhase = mProgressArcCurrentAngle;
			secondPhase = 0;
		}

		if (firstPhase < 1.0f) {
			canvas.drawArc(mProgressArcBounds, ARC_OFFSET_DEGREES + (firstPhase * ARC_TARGET_DEGREES), mPendingArcCurrent, false, mProgressPendingPaint);
		}
		if (firstPhase >= 0 && secondPhase < 1.0f) {
			float endDegrees = firstPhase * ARC_TARGET_DEGREES;
			if (isInStateWithProgress() && endDegrees < 0.1f) {
				endDegrees = 0.1f;
			}
			canvas.drawArc(mProgressArcBounds, ARC_OFFSET_DEGREES + (secondPhase * ARC_TARGET_DEGREES), endDegrees, false, mProgressFirstPhasePaint);
		}
		if (secondPhase > 0) {
			float endDegress = secondPhase * ARC_TARGET_DEGREES;
			if (isInStateWithProgress() && endDegress < 0.1f) {
				endDegress = 0.1f;
			}
			canvas.drawArc(mProgressArcBounds, ARC_OFFSET_DEGREES, endDegress, false, mProgressSecondPhasePaint);
		}
	}

	private boolean isInStateWithProgress() {
		return mState == STATE_ANIM_PROGRESS || mState == STATE_PROGRESS;
	}

	@Override
	protected boolean isAnimationInProgress() {
		return mState != STATE_PROGRESS;
	}

	public void setOnAnimationEndListener(OnAnimationEndListener listener) {
		mOnAnimationEndListener = listener;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		return new SavedState(superState, mProgress, mState);
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		super.onRestoreInstanceState(state);

		if (!(state instanceof SavedState)) {
			return;
		}

		SavedState savedState = (SavedState) state;
		mProgress = savedState.mProgress;
		this.mState = savedState.mState;
	}

	static class SavedState extends BaseSavedState {

		private float mProgress;
		private int mState;

		private SavedState(Parcelable superState, float progress, int state) {
			super(superState);
			mProgress = progress;
			mState = state;
		}

		private SavedState(Parcel in) {
			super(in);
			mProgress = in.readFloat();
			mState = in.readInt();
		}

		@Override
		public void writeToParcel(Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeFloat(mProgress);
			out.writeInt(mState);
		}

		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}

					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};
	}

	public interface OnAnimationEndListener {
		void onTransitionToIndeterminateEnd();

		void onTransitionToProgressEnd();

		void onProgressEnd();
	}


	public static class OnAnimationEndListenerAdapter implements OnAnimationEndListener {

		@Override
		public void onTransitionToIndeterminateEnd() {

		}

		@Override
		public void onTransitionToProgressEnd() {

		}

		@Override
		public void onProgressEnd() {

		}

	}

}