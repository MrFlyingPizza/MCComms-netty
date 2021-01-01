package common.message.audio;

import common.message.Message;

import java.io.Serializable;
import java.util.UUID;

public class FloatControlAudioMessage extends Message<FloatControlAudioMessage.AudioData> {

    private static final long serialVersionUID = 1L;

    public static class AudioData implements Serializable {

        private static final long serialVersionUID = 1L;

        private float pan;
        private float gain;
        private byte[] sound;

        public AudioData(float pan, float gain, byte[] sound) {
            this.pan = pan;
            this.gain = gain;
            this.sound = sound;
        }

        public float getPan() {
            return pan;
        }
        public void setPan(float pan) {
            this.pan = pan;
        }

        public float getGain() {
            return gain;
        }
        public void setGain(float gain) {
            this.gain = gain;
        }

        public byte[] getSound() {
            return sound;
        }
        public void setSound(byte[] sound) {
            this.sound = sound;
        }
    }

    public FloatControlAudioMessage(UUID uuid, AudioData audioData) {
        super(uuid, audioData);
    }

    public AudioData getAudioData() {
        return getContent();
    }

    public void setAudioData(AudioData audioData) {
        setContent(audioData);
    }

}
