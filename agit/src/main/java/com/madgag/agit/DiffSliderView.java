/*
 * Copyright (c) 2011 Roberto Tyley
 *
 * This file is part of 'Agit' - an Android Git client.
 *
 * Agit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Agit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.madgag.agit;

import static android.content.Context.VIBRATOR_SERVICE;
import android.content.Context;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class DiffSliderView extends RelativeLayout {

	public static interface OnStateUpdateListener {
		void onStateChanged (DiffSliderView diffSliderView, float state);
	}
	
	private OnStateUpdateListener stateUpdateListener;
	
	public DiffSliderView(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.diff_seekbar_view, this);
		
		SeekBar seekBar = (SeekBar) findViewById(R.id.DiffPlayerSeekBar);
		DiffSeekBarChangeListener foo = new DiffSeekBarChangeListener((Vibrator) context.getSystemService(VIBRATOR_SERVICE));
		seekBar.setOnSeekBarChangeListener(foo);
		seekBar.setProgress(seekBar.getMax());
	}

	public void setStateUpdateListener(OnStateUpdateListener stateUpdateListener) {
		this.stateUpdateListener=stateUpdateListener;
	}
	

	class DiffSeekBarChangeListener implements OnSeekBarChangeListener {
		private final Vibrator vibrator;
		
		public DiffSeekBarChangeListener(Vibrator vibrator) {
			this.vibrator = vibrator;
		}
		
		public void onStopTrackingTouch(SeekBar seekBar) {
			// TODO - should we animate this movement?
			seekBar.setProgress(unitProgress(seekBar)<0.5?0:seekBar.getMax());
		}

		public void onStartTrackingTouch(SeekBar seekBar) {}

		public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
			if (progress==0 || seekBar.getMax()==progress) {
				vibrator.vibrate(17);
			}
			
			float unitProgress = unitProgress(seekBar);

			notifyTheOthers(unitProgress);
			
		}


		private float unitProgress(SeekBar seekBar) {
			return ((float)seekBar.getProgress())/seekBar.getMax();
		}
	}

	private void notifyTheOthers(float unitProgress) {
		if (stateUpdateListener!=null) {
			stateUpdateListener.onStateChanged(this, unitProgress);
		}
	}
}
