// LanXin Phone Call Adapter - Full Implementation
// 蓝信通讯电话适配器完整实现
package org.telegram.messenger.trtc;

import android.content.Context;
import android.view.TextureView;

import org.telegram.messenger.FileLog;

/**
 * 电话呼叫适配器 - 简化TRTC使用的高级封装
 * 蓝信通讯项目专用 - 完整可用版本
 */
public class PhoneCallAdapter {
    
    private TRTCAdapter trtcAdapter;
    private Context context;
    private String currentCallId;
    private boolean isCallActive = false;
    private boolean isVideoCall = false;
    
    // 通话状态
    public enum CallState {
        IDLE,           // 空闲
        CALLING,        // 正在呼叫
        RINGING,        // 响铃中
        CONNECTED,      // 已连接
        ENDED           // 已结束
    }
    
    private CallState callState = CallState.IDLE;
    private PhoneCallListener callListener;
    
    public PhoneCallAdapter(Context context) {
        this.context = context;
        this.trtcAdapter = TRTCAdapter.getInstance(context);
    }
    
    /**
     * 发起语音通话
     */
    public boolean makeVoiceCall(String callId, String userId, String userSig, int roomId) {
        FileLog.d("PhoneCallAdapter: makeVoiceCall - callId: " + callId + ", roomId: " + roomId);
        try {
            this.currentCallId = callId;
            this.isVideoCall = false;
            this.isCallActive = true;
            this.callState = CallState.CALLING;
            
            // 设置TRTC回调
            setupTRTCCallbacks();
            
            // 进入房间
            boolean success = trtcAdapter.enterRoom(roomId, userId, userSig);
            
            if (success) {
                // 开启麦克风
                trtcAdapter.muteLocalAudio(false);
                
                FileLog.d("PhoneCallAdapter: Voice call initiated");
                notifyCallStateChanged(CallState.CALLING);
                return true;
            } else {
                endCall();
                return false;
            }
        } catch (Exception e) {
            FileLog.e("PhoneCallAdapter: Failed to make voice call", e);
            endCall();
            return false;
        }
    }
    
    /**
     * 发起视频通话
     */
    public boolean makeVideoCall(String callId, String userId, String userSig, int roomId, 
                                 TextureView localView, TextureView remoteView) {
        FileLog.d("PhoneCallAdapter: makeVideoCall - callId: " + callId + ", roomId: " + roomId);
        try {
            this.currentCallId = callId;
            this.isVideoCall = true;
            this.isCallActive = true;
            this.callState = CallState.CALLING;
            
            // 设置TRTC回调
            setupTRTCCallbacks();
            
            // 进入房间
            boolean success = trtcAdapter.enterRoom(roomId, userId, userSig);
            
            if (success) {
                // 开启摄像头和麦克风
                trtcAdapter.startLocalPreview(localView);
                trtcAdapter.muteLocalAudio(false);
                
                FileLog.d("PhoneCallAdapter: Video call initiated");
                notifyCallStateChanged(CallState.CALLING);
                return true;
            } else {
                endCall();
                return false;
            }
        } catch (Exception e) {
            FileLog.e("PhoneCallAdapter: Failed to make video call", e);
            endCall();
            return false;
        }
    }
    
    /**
     * 接听通话
     */
    public boolean answerCall(String userId, String userSig, int roomId, boolean isVideo, 
                             TextureView localView, TextureView remoteView) {
        FileLog.d("PhoneCallAdapter: answerCall - roomId: " + roomId + ", isVideo: " + isVideo);
        try {
            this.isVideoCall = isVideo;
            this.isCallActive = true;
            this.callState = CallState.CONNECTED;
            
            // 设置TRTC回调
            setupTRTCCallbacks();
            
            // 进入房间
            boolean success = trtcAdapter.enterRoom(roomId, userId, userSig);
            
            if (success) {
                // 开启麦克风
                trtcAdapter.muteLocalAudio(false);
                
                // 如果是视频通话，开启摄像头
                if (isVideo && localView != null) {
                    trtcAdapter.startLocalPreview(localView);
                }
                
                FileLog.d("PhoneCallAdapter: Call answered");
                notifyCallStateChanged(CallState.CONNECTED);
                return true;
            } else {
                endCall();
                return false;
            }
        } catch (Exception e) {
            FileLog.e("PhoneCallAdapter: Failed to answer call", e);
            endCall();
            return false;
        }
    }
    
    /**
     * 挂断通话
     */
    public void hangUp() {
        FileLog.d("PhoneCallAdapter: hangUp called");
        endCall();
    }
    
    /**
     * 结束通话（内部方法）
     */
    private void endCall() {
        try {
            if (isCallActive) {
                // 退出房间
                trtcAdapter.exitRoom();
                
                isCallActive = false;
                callState = CallState.ENDED;
                currentCallId = null;
                
                FileLog.d("PhoneCallAdapter: Call ended");
                notifyCallStateChanged(CallState.ENDED);
            }
        } catch (Exception e) {
            FileLog.e("PhoneCallAdapter: Failed to end call", e);
        }
    }
    
    /**
     * 切换静音
     */
    public boolean toggleMute() {
        boolean newMuteState = !trtcAdapter.isMicOn();
        return trtcAdapter.muteLocalAudio(newMuteState);
    }
    
    /**
     * 切换摄像头
     */
    public boolean toggleCamera() {
        if (!isVideoCall) {
            return false;
        }
        return trtcAdapter.switchCamera();
    }
    
    /**
     * 切换为视频通话（从语音升级）
     */
    public boolean switchToVideoCall(TextureView localView) {
        FileLog.d("PhoneCallAdapter: switchToVideoCall");
        if (isVideoCall) {
            return true; // 已经是视频通话
        }
        
        try {
            isVideoCall = true;
            trtcAdapter.startLocalPreview(localView);
            FileLog.d("PhoneCallAdapter: Switched to video call");
            return true;
        } catch (Exception e) {
            FileLog.e("PhoneCallAdapter: Failed to switch to video call", e);
            return false;
        }
    }
    
    /**
     * 设置通话监听器
     */
    public void setPhoneCallListener(PhoneCallListener listener) {
        this.callListener = listener;
    }
    
    /**
     * 设置TRTC回调
     */
    private void setupTRTCCallbacks() {
        trtcAdapter.setCallbackListener(new TRTCAdapter.TRTCCallbackListener() {
            @Override
            public void onEnterRoom(boolean success, String message) {
                if (success) {
                    callState = CallState.CONNECTED;
                    notifyCallStateChanged(CallState.CONNECTED);
                    
                    if (callListener != null) {
                        callListener.onCallConnected(currentCallId);
                    }
                } else {
                    endCall();
                    
                    if (callListener != null) {
                        callListener.onCallFailed(currentCallId, message);
                    }
                }
            }
            
            @Override
            public void onExitRoom(int reason) {
                endCall();
                
                if (callListener != null) {
                    callListener.onCallEnded(currentCallId, reason);
                }
            }
            
            @Override
            public void onRemoteUserEnter(String userId) {
                FileLog.d("PhoneCallAdapter: Remote user entered: " + userId);
                
                if (callListener != null) {
                    callListener.onRemoteUserJoined(userId);
                }
            }
            
            @Override
            public void onRemoteUserLeave(String userId, int reason) {
                FileLog.d("PhoneCallAdapter: Remote user left: " + userId);
                
                if (callListener != null) {
                    callListener.onRemoteUserLeft(userId);
                }
            }
            
            @Override
            public void onUserVideoAvailable(String userId, boolean available) {
                if (callListener != null) {
                    callListener.onRemoteVideoAvailable(userId, available);
                }
            }
            
            @Override
            public void onUserAudioAvailable(String userId, boolean available) {
                if (callListener != null) {
                    callListener.onRemoteAudioAvailable(userId, available);
                }
            }
            
            @Override
            public void onError(int errCode, String errMsg) {
                FileLog.e("PhoneCallAdapter: TRTC Error - " + errCode + ": " + errMsg);
                
                if (callListener != null) {
                    callListener.onCallFailed(currentCallId, errMsg);
                }
                
                endCall();
            }
            
            @Override
            public void onWarning(int warningCode, String warningMsg) {
                FileLog.w("PhoneCallAdapter: TRTC Warning - " + warningCode + ": " + warningMsg);
            }
            
            @Override
            public void onNetworkQuality(int quality) {
                if (callListener != null) {
                    callListener.onNetworkQualityChanged(quality);
                }
            }
            
            @Override
            public void onStatistics(int upLoss, int downLoss, int appCpu, int systemCpu) {
                if (callListener != null) {
                    callListener.onCallStatistics(upLoss, downLoss, appCpu, systemCpu);
                }
            }
        });
    }
    
    /**
     * 通知通话状态变化
     */
    private void notifyCallStateChanged(CallState newState) {
        callState = newState;
        if (callListener != null) {
            callListener.onCallStateChanged(currentCallId, newState);
        }
    }
    
    /**
     * 获取当前通话状态
     */
    public CallState getCallState() {
        return callState;
    }
    
    public boolean isCallActive() {
        return isCallActive;
    }
    
    public String getCurrentCallId() {
        return currentCallId;
    }
    
    /**
     * 电话呼叫监听器接口
     */
    public interface PhoneCallListener {
        void onCallStateChanged(String callId, CallState state);
        void onCallConnected(String callId);
        void onCallEnded(String callId, int reason);
        void onCallFailed(String callId, String error);
        void onRemoteUserJoined(String userId);
        void onRemoteUserLeft(String userId);
        void onRemoteVideoAvailable(String userId, boolean available);
        void onRemoteAudioAvailable(String userId, boolean available);
        void onNetworkQualityChanged(int quality);
        void onCallStatistics(int upLoss, int downLoss, int appCpu, int systemCpu);
    }
}

