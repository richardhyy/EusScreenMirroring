package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.playback.Playback;
import cc.eumc.screenmirroringclient.util.NumericUtil;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.Timer;

public class PlaybackClient {

    private UdpClient client;
    private Timer timer;
    private DataSender dataSender;
    private Playback playback;

    public PlaybackClient(String address, int port, short id, String password, File recordFile) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.dataSender = new DataSender(client, new RemoteMirror(id, password), false);
            dataSender.start();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        print(String.format("Loading %s...", recordFile.getName()));
        try {
            playback = new Playback(recordFile, bytes -> {
                byte[] data = new byte[8 + bytes.length];
                System.arraycopy(NumericUtil.shortToBytes(id), 0, data, 0, 2);
                System.arraycopy(password.getBytes(StandardCharsets.US_ASCII), 0, data, 2, 6);
                System.arraycopy(bytes, 0, data, 8, bytes.length);
                dataSender.queueDataPacket(data);
            }, totalFrames -> {
                print(String.format("Playback ended. Total frames: %d", totalFrames));
                printPrompt();
            });
            playback.start();
        } catch (Exception e) {
            print(String.format("ERROR: %s", e.getMessage()));
            return;
        }

        print("Playback started. Type `help` for command help.");

        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            printPrompt();

            command = scanner.nextLine();
            String[] args = command.split(" ");
            if (args.length == 0) {
                continue;
            }

            switch (args[0].toLowerCase()) {
                case "reset", "r" -> {
                    playback.setCurrentFrame(0);
                    if (!playback.isPlaying()) {
                        playback.start();
                    }
                    print("Playback reset.");
                }

                case "pause", "p" -> {
                    if (playback.isPlaying()) {
                        playback.pause();
                        print("Playback was paused, type `pause` again to unpause.");
                    } else {
                        playback.start();
                        print("Unpause.");
                    }
                }

                case "quit", "stop", "q" -> {
                    timer.cancel();

                    dataSender.clearPending();
                    dataSender.sendShowDisconnectScreen();

                    System.exit(0);
                }

                case "help", "?" -> {
                    print("pause(p)   :  (Un)pause playback");
                    print("reset(r)   :  Reset playback");
                    print("quit(q)    :  Request server showing disconnect screen and then exit");
                }
            }
        }
    }

    public void print(String text, boolean newLine) {
        System.out.print(text);
        if (newLine) {
            System.out.print('\n');
        }
    }

    public void print(String text) {
        print(text, true);
    }

    public void printPrompt() {
        print("> ", false);
    }
}
