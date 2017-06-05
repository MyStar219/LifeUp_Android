package com.smartgateway.app.weiju;

import android.content.Context;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evideo.weiju.callback.InfoCallback;
import com.evideo.weiju.info.CommandError;
import com.evideo.weiju.info.Info;
import com.evideo.weiju.info.unlock.CreateUnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfo;
import com.evideo.weiju.info.unlock.UnlockWaveInfoList;
import com.smartgateway.app.WeijuHelper;

import java.util.List;

/**
 * - for each list as wave
 - - if wave.type == owner && wave.expired_time > now()
 - - - play wave.audio_url
 - - - return;
 - - else if (wave.type == owner && wave.expired_time <= now()
 - - - delete wave
 - if completed traversing list and no Owner audio exists
 - - Create new Wave
 - - - if (isSuccess)
 - - - - retrieve Wave object from SDK and play wave.audio_url
 */


public class WaveUnlockingHelper {

    public void fetchUnlockingWave(final Context appContext, final WaveUnlockingCallback callback) {
        try {
            WeijuHelper weijuHelper = new WeijuHelper();
            weijuHelper.fetchOwnerUnlockWaveList(appContext, new InfoCallback<UnlockWaveInfoList>() {
                @Override
                public void onSuccess(UnlockWaveInfoList unlockWaveInfoList) {
                    if (unlockWaveInfoList == null) {
                        callback.onFailure(null);
                        return;
                    }

                    UnlockWaveInfo unlockWaveInfo = findOwnerWave(appContext, unlockWaveInfoList.getList());

                    if (unlockWaveInfo != null) {
                        // check expire time, if expired - we should delete this wave
                        if (unlockWaveInfo.getExpired_time() > (System.currentTimeMillis() / 1000) + 30) {
                            Log.d("WaveHelper", "correct owner wave found: " + unlockWaveInfo.getAudio_url());
                            callback.onUrlFetched(unlockWaveInfo.getAudio_url());
                        } else {
                            // we need to delete expired first
                            Log.d("WaveHelper",
                                    "owner wave is expired, going to delete it");
                            deleteWave(appContext, unlockWaveInfo, new InfoCallback<Info>() {
                                @Override
                                public void onSuccess(Info info) {
                                    // delete happened, and we need to create new one
                                    Log.d("WaveHelper", "wave is deleted");
                                    createUnlockWave(appContext, callback);
                                }

                                @Override
                                public void onFailure(CommandError error) {
                                    Log.e("WaveHelper", "wave is not deleted");
                                    callback.onFailure(error);
                                }
                            });
                        }
                    } else {
                        Log.d("WaveHelper", "no any owner wave found in the list, we just need create new one");
                        createUnlockWave(appContext, callback);
                    }
                }

                @Override
                public void onFailure(CommandError error) {
                    callback.onFailure(error);
                }
            });
        } catch (Throwable throwable) {
            if (callback != null) {
                callback.onFailure(null);
            }
        }
    }

    private void createUnlockWave(Context appContext, final WaveUnlockingCallback callback) {
        Log.d("WaveHelper", "start creating owner wave");
        WeijuHelper weijuHelper = new WeijuHelper();
        weijuHelper.createUnlockWave(appContext, 0, new InfoCallback<CreateUnlockWaveInfo>() {
            @Override
            public void onSuccess(CreateUnlockWaveInfo unlockWaveInfo) {
                callback.onUrlFetched(unlockWaveInfo.getAudio_url());
            }

            @Override
            public void onFailure(CommandError error) {
                callback.onFailure(error);
            }
        });
    }

    @Nullable
    private UnlockWaveInfo findOwnerWave(Context appContext, List<UnlockWaveInfo> list) {
        if (list == null || list.isEmpty()) {
            Log.d("WaveHelper", "unlock wave list is empty or null");
            return null;
        }

        for (UnlockWaveInfo unlockWaveInfo : list) {
            // skip guest waves
            if (unlockWaveInfo == null || unlockWaveInfo.getType() == 1) {
                continue;
            }

            Log.d("WaveHelper", "wave from list time: " + unlockWaveInfo.getExpired_time() +
                                " : " +
                                System.currentTimeMillis() / 1000);

            return unlockWaveInfo;
        }

        return null;
    }

    private void deleteWave(Context appContext, UnlockWaveInfo unlockWaveInfo, InfoCallback<Info> callback) {
        WeijuHelper weijuHelper = new WeijuHelper();
        weijuHelper.deleteWave(appContext, unlockWaveInfo, callback);
    }

    public interface WaveUnlockingCallback {
        void onUrlFetched(String url);
        void onFailure(CommandError error);
    }
}
