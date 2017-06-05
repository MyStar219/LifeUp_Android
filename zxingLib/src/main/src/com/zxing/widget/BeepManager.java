/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zxing.widget;

import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.zxing.activity.CaptureActivity;
import com.zxinglib.R;


/**
 * Manages beeps and vibrations for {@link CaptureActivity}.
 */
public final class BeepManager implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

  private static final String TAG = BeepManager.class.getSimpleName();

  private static final float BEEP_VOLUME = 0.10f;

  private final Activity activity;
  private MediaPlayer mediaPlayer;
  private boolean playBeep;

  public BeepManager(Activity activity) {
    this.activity = activity;
    this.mediaPlayer = null;
    playBeep = true;
    updatePrefs();
  }

  public synchronized void updatePrefs() {
    if (playBeep && mediaPlayer == null) {
      // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
      // so we now play on the music stream.
      activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
      mediaPlayer = buildMediaPlayer(activity);
    }
  }

  public synchronized void playBeepSoundAndVibrate() {
    if (playBeep && mediaPlayer != null) {
      mediaPlayer.start();
    }
  }

  private MediaPlayer buildMediaPlayer(Context activity) {
    MediaPlayer mediaPlayer = new MediaPlayer();
    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    mediaPlayer.setOnCompletionListener(this);
    mediaPlayer.setOnErrorListener(this);

    AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.beep);
    try {
      mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
      file.close();
      mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
      mediaPlayer.prepare();
    } catch (IOException ioe) {
      Log.w(TAG, ioe);
      mediaPlayer = null;
    }
    return mediaPlayer;
  }

  public void onCompletion(MediaPlayer mp) {
    // When the beep has finished playing, rewind to queue up another one.      
    mp.seekTo(0);
  }

  public synchronized boolean onError(MediaPlayer mp, int what, int extra) {
    if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
      // we are finished, so put up an appropriate error toast if required and finish
      activity.finish();
    } else {
      // possibly media player error, so release and recreate
      mp.release();
      mediaPlayer = null;
      updatePrefs();
    }
    return true;
  }

}
