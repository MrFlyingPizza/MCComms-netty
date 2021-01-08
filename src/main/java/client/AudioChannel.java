package client;

import common.message.audio.AudioMessage;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AudioChannel {

    private final UUID UUID;
    private final ConcurrentLinkedQueue<AudioMessage> MESSAGE_QUEUE = new ConcurrentLinkedQueue<>();
    private final SourceDataLine LINE;
    private final FloatControl GAIN_CONTROL;
    private final FloatControl PAN_CONTROL;

    private boolean running;

    public AudioChannel(UUID uuid) throws LineUnavailableException {
        UUID = uuid;
        LINE = (SourceDataLine) AudioSystem.getLine(Client.getSourceInfo());
        LINE.open();
        GAIN_CONTROL = (FloatControl) LINE.getControl(FloatControl.Type.MASTER_GAIN);
        PAN_CONTROL = (FloatControl) LINE.getControl(FloatControl.Type.PAN);

        start();
    }

    public UUID getUUID() {
        return UUID;
    }

    public void start() {
        running = true;

        /* PLAYBACK FROM QUEUE THREAD */

        Thread playbackThread = new Thread(() -> {

            System.out.println("Audio Channel Started For " + UUID);

            byte[] stereoSound = new byte[AudioMessage.SOUND_SIZE * 2];

            while (LINE.isOpen() && running) {

                AudioMessage inboundAudioMessage = MESSAGE_QUEUE.poll();

                if (inboundAudioMessage != null) {

                    float gain = inboundAudioMessage.getGain();
                    float pan = inboundAudioMessage.getPan();
                    byte[] sound = inboundAudioMessage.getSound();

                    for (int i = 0; i < sound.length; i += 2) {
                        stereoSound[i * 2] = sound[i];
                        stereoSound[i * 2 + 1] = sound[i + 1];
                        stereoSound[i * 2 + 2] = sound[i];
                        stereoSound[i * 2 + 3] = sound[i + 1];
                    }

                    GAIN_CONTROL.setValue(gain);
                    PAN_CONTROL.setValue(pan);

                    LINE.start();
                    LINE.write(stereoSound, 0, stereoSound.length);

                } else {
                    LINE.stop();
                }

            }

            System.out.println("Audio Channel Ended For " + UUID);
            MESSAGE_QUEUE.clear();

        });
        playbackThread.start();
    }

    public void stop() {
        System.out.println("Stopped called for " + UUID);
        running = false;
    }

    public void enqueue(AudioMessage message) {
            MESSAGE_QUEUE.add(message);
    }

}
