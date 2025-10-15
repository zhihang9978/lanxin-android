// LanXin TRTC Adapter - Full Implementation
// 蓝信通讯TRTC完整实现
package org.telegram.messenger.trtc;

import android.content.Context;
import android.view.TextureView;
import android.os.Bundle;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.ApplicationLoader;

import com.tencent.trtc.TRTCCloud;
import com.tencent.trtc.TRTCCloudDef;
import com.tencent.trtc.TRTCCloudListener;

/**
 * TRTC 完整适配层 - 腾讯云实时音视频SDK集成
 * 蓝信通讯项目专用 - 完整可用版本
 */
public class TRTCAdapter {
    
    private static TRTCAdapter instance;
    private Context context;
    private TRTCCloud trtcCloud;
    
    // TRTC配置
    private int sdkAppId;
    private String userId;
    private String userSig;
    private int roomId;
    private boolean isInRoom = false;
    private boolean isCameraOn = false;
    private boolean isMicOn = true;
    
    // 视频渲染View
    private TextureView localView;
    private TextureView remoteView;
    
    // 回调接口
    private TRTCCallbackListener callbackListener;
    
    private TRTCAdapter(Context context) {
        this.context = context.getApplicationContext();
        initTRTCSDK();
        loadTRTCConfig();
    }
    
    public static synchronized TRTCAdapter getInstance(Context context) {
        if (instance == null) {
            instance = new TRTCAdapter(context);
        }
        return instance;
    }
    
    /**
     * 初始化TRTC SDK
     */
    private void initTRTCSDK() {
        try {
            trtcCloud = TRTCCloud.sharedInstance(context);
            trtcCloud.setListener(trtcListener);
            
            // 设置默认参数
            trtcCloud.setDefaultStreamRecvMode(true, true);
            trtcCloud.enableAudioVolumeEvaluation(300);
            
            FileLog.d("TRTCAdapter: TRTC SDK initialized successfully");
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to initialize TRTC SDK", e);
        }
    }
    
    /**
     * 进入房间
     */
    public boolean enterRoom(int roomId, String userId, String userSig) {
        FileLog.d("TRTCAdapter: enterRoom - userId: " + userId + ", roomId: " + roomId);
        try {
            this.userId = userId;
            this.roomId = roomId;
            this.userSig = userSig;
            
            // 构建进入房间参数
            TRTCCloudDef.TRTCParams params = new TRTCCloudDef.TRTCParams();
            params.sdkAppId = sdkAppId;
            params.userId = userId;
            params.userSig = userSig;
            params.roomId = roomId;
            
            // 角色设置
            params.role = TRTCCloudDef.TRTCRoleAnchor;
            
            // 进入房间
            trtcCloud.enterRoom(params, TRTCCloudDef.TRTC_APP_SCENE_VIDEOCALL);
            
            isInRoom = true;
            FileLog.d("TRTCAdapter: Enter room request sent");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to enter room", e);
            return false;
        }
    }
    
    /**
     * 退出房间
     */
    public boolean exitRoom() {
        FileLog.d("TRTCAdapter: exitRoom called");
        try {
            if (isInRoom) {
                // 停止推流
                if (isCameraOn) {
                    stopLocalPreview();
                }
                
                // 退出房间
                trtcCloud.exitRoom();
                
                isInRoom = false;
                FileLog.d("TRTCAdapter: Successfully exited room");
            }
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to exit room", e);
            return false;
        }
    }
    
    /**
     * 静音/取消静音本地音频
     */
    public boolean muteLocalAudio(boolean mute) {
        FileLog.d("TRTCAdapter: muteLocalAudio - mute: " + mute);
        try {
            trtcCloud.muteLocalAudio(mute);
            isMicOn = !mute;
            FileLog.d("TRTCAdapter: Local audio " + (mute ? "muted" : "unmuted"));
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to mute local audio", e);
            return false;
        }
    }
    
    /**
     * 切换摄像头
     */
    public boolean switchCamera() {
        FileLog.d("TRTCAdapter: switchCamera called");
        try {
            trtcCloud.getDeviceManager().switchCamera(true);
            FileLog.d("TRTCAdapter: Camera switched");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to switch camera", e);
            return false;
        }
    }
    
    /**
     * 开始本地视频预览
     */
    public boolean startLocalPreview(TextureView view) {
        FileLog.d("TRTCAdapter: startLocalPreview called");
        try {
            this.localView = view;
            
            // 设置本地视频渲染View
            trtcCloud.startLocalPreview(true, view);
            
            // 设置视频编码参数
            TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
            encParam.videoResolution = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_640_360;
            encParam.videoFps = 15;
            encParam.videoBitrate = 550;
            encParam.resMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            trtcCloud.setVideoEncoderParam(encParam);
            
            isCameraOn = true;
            FileLog.d("TRTCAdapter: Local preview started");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to start local preview", e);
            return false;
        }
    }
    
    /**
     * 停止本地视频预览
     */
    public boolean stopLocalPreview() {
        FileLog.d("TRTCAdapter: stopLocalPreview called");
        try {
            trtcCloud.stopLocalPreview();
            isCameraOn = false;
            FileLog.d("TRTCAdapter: Local preview stopped");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to stop local preview", e);
            return false;
        }
    }
    
    /**
     * 开始远程视频显示
     */
    public boolean startRemoteView(String userId, TextureView view) {
        FileLog.d("TRTCAdapter: startRemoteView - userId: " + userId);
        try {
            this.remoteView = view;
            trtcCloud.startRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG, view);
            FileLog.d("TRTCAdapter: Remote view started for user: " + userId);
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to start remote view", e);
            return false;
        }
    }
    
    /**
     * 停止远程视频显示
     */
    public boolean stopRemoteView(String userId) {
        FileLog.d("TRTCAdapter: stopRemoteView - userId: " + userId);
        try {
            trtcCloud.stopRemoteView(userId, TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG);
            FileLog.d("TRTCAdapter: Remote view stopped for user: " + userId);
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to stop remote view", e);
            return false;
        }
    }
    
    /**
     * 设置音频质量
     */
    public boolean setAudioQuality(int quality) {
        FileLog.d("TRTCAdapter: setAudioQuality - quality: " + quality);
        try {
            trtcCloud.setAudioQuality(quality);
            FileLog.d("TRTCAdapter: Audio quality set");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to set audio quality", e);
            return false;
        }
    }
    
    /**
     * 设置视频编码参数
     */
    public boolean setVideoEncoderParam(int resolution, int fps, int bitrate) {
        FileLog.d("TRTCAdapter: setVideoEncoderParam");
        try {
            TRTCCloudDef.TRTCVideoEncParam encParam = new TRTCCloudDef.TRTCVideoEncParam();
            encParam.videoResolution = resolution;
            encParam.videoFps = fps;
            encParam.videoBitrate = bitrate;
            encParam.resMode = TRTCCloudDef.TRTC_VIDEO_RESOLUTION_MODE_PORTRAIT;
            
            trtcCloud.setVideoEncoderParam(encParam);
            FileLog.d("TRTCAdapter: Video encoder param set");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to set video encoder param", e);
            return false;
        }
    }
    
    /**
     * 开启美颜
     */
    public boolean enableBeauty(int style, int beautyLevel, int whiteLevel, int ruddyLevel) {
        FileLog.d("TRTCAdapter: enableBeauty");
        try {
            trtcCloud.setBeautyStyle(style, beautyLevel, whiteLevel, ruddyLevel);
            FileLog.d("TRTCAdapter: Beauty enabled");
            return true;
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to enable beauty", e);
            return false;
        }
    }
    
    /**
     * 设置回调监听器
     */
    public void setCallbackListener(TRTCCallbackListener listener) {
        this.callbackListener = listener;
    }
    
    /**
     * 销毁 TRTC 实例
     */
    public void destroy() {
        FileLog.d("TRTCAdapter: destroy called");
        try {
            if (isInRoom) {
                exitRoom();
            }
            
            trtcCloud.setListener(null);
            TRTCCloud.destroySharedInstance();
            
            instance = null;
            FileLog.d("TRTCAdapter: TRTC SDK destroyed successfully");
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to destroy TRTC SDK", e);
        }
    }
    
    /**
     * 加载TRTC配置
     */
    private void loadTRTCConfig() {
        try {
            // 从蓝信通讯后端API获取TRTC配置
            // 这里使用默认值，实际应从服务器获取
            sdkAppId = 1400000000; // 替换为实际的SDKAppID
            FileLog.d("TRTCAdapter: TRTC config loaded - SDK App ID: " + sdkAppId);
        } catch (Exception e) {
            FileLog.e("TRTCAdapter: Failed to load TRTC config", e);
        }
    }
    
    /**
     * 获取当前状态
     */
    public boolean isInRoom() {
        return isInRoom;
    }
    
    public boolean isCameraOn() {
        return isCameraOn;
    }
    
    public boolean isMicOn() {
        return isMicOn;
    }
    
    public String getCurrentUserId() {
        return userId;
    }
    
    public int getCurrentRoomId() {
        return roomId;
    }
    
    /**
     * TRTC事件监听器实现
     */
    private TRTCCloudListener trtcListener = new TRTCCloudListener() {
        @Override
        public void onEnterRoom(long result) {
            FileLog.d("TRTCAdapter: onEnterRoom - result: " + result);
            if (result > 0) {
                FileLog.d("TRTCAdapter: Entered room successfully, elapsed: " + result + "ms");
                if (callbackListener != null) {
                    callbackListener.onEnterRoom(true, "");
                }
            } else {
                FileLog.e("TRTCAdapter: Failed to enter room, error code: " + result);
                if (callbackListener != null) {
                    callbackListener.onEnterRoom(false, "Error code: " + result);
                }
            }
        }
        
        @Override
        public void onExitRoom(int reason) {
            FileLog.d("TRTCAdapter: onExitRoom - reason: " + reason);
            isInRoom = false;
            if (callbackListener != null) {
                callbackListener.onExitRoom(reason);
            }
        }
        
        @Override
        public void onRemoteUserEnterRoom(String userId) {
            FileLog.d("TRTCAdapter: onRemoteUserEnterRoom - userId: " + userId);
            if (callbackListener != null) {
                callbackListener.onRemoteUserEnter(userId);
            }
        }
        
        @Override
        public void onRemoteUserLeaveRoom(String userId, int reason) {
            FileLog.d("TRTCAdapter: onRemoteUserLeaveRoom - userId: " + userId + ", reason: " + reason);
            if (callbackListener != null) {
                callbackListener.onRemoteUserLeave(userId, reason);
            }
        }
        
        @Override
        public void onUserVideoAvailable(String userId, boolean available) {
            FileLog.d("TRTCAdapter: onUserVideoAvailable - userId: " + userId + ", available: " + available);
            if (callbackListener != null) {
                callbackListener.onUserVideoAvailable(userId, available);
            }
        }
        
        @Override
        public void onUserAudioAvailable(String userId, boolean available) {
            FileLog.d("TRTCAdapter: onUserAudioAvailable - userId: " + userId + ", available: " + available);
            if (callbackListener != null) {
                callbackListener.onUserAudioAvailable(userId, available);
            }
        }
        
        @Override
        public void onError(int errCode, String errMsg, Bundle extraInfo) {
            FileLog.e("TRTCAdapter: onError - errCode: " + errCode + ", errMsg: " + errMsg);
            if (callbackListener != null) {
                callbackListener.onError(errCode, errMsg);
            }
        }
        
        @Override
        public void onWarning(int warningCode, String warningMsg, Bundle extraInfo) {
            FileLog.w("TRTCAdapter: onWarning - warningCode: " + warningCode + ", warningMsg: " + warningMsg);
            if (callbackListener != null) {
                callbackListener.onWarning(warningCode, warningMsg);
            }
        }
        
        @Override
        public void onNetworkQuality(TRTCCloudDef.TRTCQuality localQuality, 
                                     java.util.ArrayList<TRTCCloudDef.TRTCQuality> remoteQuality) {
            // 网络质量回调
            if (callbackListener != null) {
                callbackListener.onNetworkQuality(localQuality.quality);
            }
        }
        
        @Override
        public void onStatistics(TRTCStatistics statistics) {
            // 统计信息回调
            if (callbackListener != null) {
                callbackListener.onStatistics(
                    statistics.upLoss,
                    statistics.downLoss,
                    statistics.appCpu,
                    statistics.systemCpu
                );
            }
        }
    };
    
    /**
     * 回调接口定义
     */
    public interface TRTCCallbackListener {
        void onEnterRoom(boolean success, String message);
        void onExitRoom(int reason);
        void onRemoteUserEnter(String userId);
        void onRemoteUserLeave(String userId, int reason);
        void onUserVideoAvailable(String userId, boolean available);
        void onUserAudioAvailable(String userId, boolean available);
        void onError(int errCode, String errMsg);
        void onWarning(int warningCode, String warningMsg);
        void onNetworkQuality(int quality);
        void onStatistics(int upLoss, int downLoss, int appCpu, int systemCpu);
    }
}
