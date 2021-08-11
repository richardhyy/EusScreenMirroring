package cc.eumc.screenmirroringclient.playback;

import cc.eumc.screenmirroringclient.util.NumericUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class Playback extends Thread {
    int currentFrame = 0;
    long initialFrameTimestamp;
    long playbackRelativeStartTimestamp = -1;
    long pauseTimestamp = -1;
    Frame[] frames;
    boolean playing = false;
    boolean pause = false;

    Consumer<byte[]> nextFrameCallback;
    Consumer<Integer> playbackEndCallback;

    public Playback(File recordFile, Consumer<byte[]> nextFrameCallback, Consumer<Integer> playbackEndCallback) throws Exception {
        this.frames = readFrames(new FileInputStream(recordFile));
        if (frames.length == 0) {
            throw new Exception("Empty record");
        }

        this.initialFrameTimestamp = frames[0].timestamp();
        this.nextFrameCallback = nextFrameCallback;
        this.playbackEndCallback = playbackEndCallback;
    }

    @Override
    public void run() {
        while (playing) {
            if (isEnded() || pause) {
                continue;
            }

            if (frames[currentFrame].timestamp() - initialFrameTimestamp <= System.currentTimeMillis() - playbackRelativeStartTimestamp) {
//                System.out.println("Frame " + currentFrame);
                nextFrameCallback.accept(frames[currentFrame].packet());
                currentFrame++;

                if (isEnded()) {
                    playbackEndCallback.accept(currentFrame);
                }
            }
        }
    }

    public void pause() {
        pause = true;
        pauseTimestamp = System.currentTimeMillis();
    }

    @Override
    public synchronized void start() {
        this.playing = true;
        this.pause = false;
        if (pauseTimestamp > 0) {
            this.playbackRelativeStartTimestamp += System.currentTimeMillis() - pauseTimestamp;
            this.pauseTimestamp = -1;
        } else {
            this.playbackRelativeStartTimestamp = System.currentTimeMillis();
            super.start();
        }
    }


    public void setCurrentFrame(int currentFrame) {
        this.currentFrame = currentFrame;
        this.playbackRelativeStartTimestamp = System.currentTimeMillis() - frames[currentFrame].timestamp() + initialFrameTimestamp;
    }

    public boolean isPlaying() {
        return playing && !pause;
    }

    public boolean isEnded() {
        return currentFrame >= frames.length;
    }

    public static Frame[] readFrames(FileInputStream fileInputStream) throws IOException {
        List<Frame> frameList = new ArrayList<>();
        while (fileInputStream.available() > 10) {
            byte[] meta = new byte[10];
            if (fileInputStream.read(meta) < 10) {
                System.out.printf("Read meta length is less than expected %d%n", meta.length);
                break;
            }

            long timestamp = NumericUtil.bytesToLong(Arrays.copyOfRange(meta, 0, 8));
            short packetLength = NumericUtil.bytesToShort(Arrays.copyOfRange(meta, 8, 10));
//            System.out.println(String.format("CT: %d T: %d  L: %d", System.currentTimeMillis(), timestamp, packetLength));

            // read packet data
            byte[] packet = new byte[packetLength];
            if (fileInputStream.read(packet) < packetLength) {
                System.out.printf("Read packet length is less than expected %d%n", packetLength);
                break;
            }
            frameList.add(new Frame(timestamp, packet));
        }
        return frameList.toArray(new Frame[0]);
    }
}
