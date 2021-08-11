package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.playback.PacketRecorder;
import cc.eumc.screenmirroringclient.timer.MouseTrackTimer;
import cc.eumc.screenmirroringclient.timer.ScreenShotTimer;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ScreenStreamingClient {
    UdpClient client;
    Timer timer;
    DataSender dataSender;

    MouseTrackTimer mouseTrackTimer;
    ScreenShotTimer screenShotTimer;

    public ScreenStreamingClient(String address, int port, Screen screen, short id, String password, long screenshotRefreshInterval, long mouseCoordinateRefreshInterval) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.dataSender = new DataSender(client, new RemoteMirror(id, password), true);
            dataSender.start();

            this.screenShotTimer = new ScreenShotTimer(screen, new Consumer<Screen>() {
                @Override
                public void accept(Screen screen) {
                    dataSender.sendScreen(screen);
                }
            });
            timer.schedule(screenShotTimer, 1, screenshotRefreshInterval);

            this.mouseTrackTimer = new MouseTrackTimer(screen, new BiConsumer<Screen, int[]>() {
                @Override
                public void accept(Screen screen, int[] onRemoteScreenCoordinates) {
                    dataSender.sendMouseCoordinates((short)onRemoteScreenCoordinates[0], (short)onRemoteScreenCoordinates[1]);
                }
            });
            timer.schedule(mouseTrackTimer, 1, mouseCoordinateRefreshInterval);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }

        print("Done! Type `help` for command help.");

        Scanner scanner = new Scanner(System.in);
        String command;
        while (true) {
            print("> ", false);

            command = scanner.nextLine();
            String[] args = command.split(" ");
            if (args.length == 0) {
                continue;
            }

            switch (args[0].toLowerCase()) {
                case "pause", "p" -> {
                    mouseTrackTimer.setPaused(!mouseTrackTimer.isPaused());
                    screenShotTimer.setPaused(mouseTrackTimer.isPaused()); // sync their status

                    if (mouseTrackTimer.isPaused()) {
                        dataSender.clearPending();
                        dataSender.sendShowDisconnectScreen();

                        print("Screen mirroring was paused, type `pause` again to unpause.");
                    } else {
                        print("Casting your screen.");
                    }
                }

                case "record", "r" -> {
                    if (dataSender.getPacketRecorder() != null) {
                        try {
                            dataSender.getPacketRecorder().close();
                            print(String.format("Recording stopped and saved to %s", dataSender.getPacketRecorder().getRecordFile().toPath()));
                            dataSender.setPacketRecorder(null);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            dataSender.setPacketRecorder(PacketRecorder.createPacketRecorder(new File("ScreenRecordings")));
                            print("Packet recorder started. Type `record` again to stop and save.");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                case "quit", "stop", "q" -> {
                    print("Stopping...");

                    timer.cancel();

                    dataSender.clearPending();

                    try {
                        Thread.sleep(500);
                        dataSender.sendShowDisconnectScreen();
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.exit(0);
                }

                case "help", "?" -> {
                    print("pause(p) : (Un)pause screen mirroring");
                    print("record(r): Start/stop recording screen mirroring");
                    print("quit(q)  : Request server showing disconnect screen and then exit");
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
}
