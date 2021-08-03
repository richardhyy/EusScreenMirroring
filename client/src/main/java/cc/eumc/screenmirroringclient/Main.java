package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.timer.MouseTrackTimer;
import cc.eumc.screenmirroringclient.timer.ScreenShotTimer;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {
    UdpClient client;
    Timer timer;
    DataSender dataSender;

    MouseTrackTimer mouseTrackTimer;
    ScreenShotTimer screenShotTimer;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Address: ");
        String address = scanner.next();

        System.out.print("Port (default: 17211): ");
        int port = scanner.nextInt();


        System.out.print("Mirror width (default: 4): ");
        int windowWidth = scanner.nextInt();

        System.out.print("Mirror width (default: 3): ");
        int windowHeight = scanner.nextInt();

        Screen screen = new Screen(windowWidth * 128, windowHeight * 128);


        System.out.print("Mirror ID: ");
        short id = (short)scanner.nextInt();

        System.out.print("Password: ");
        String password = System.console() == null ? scanner.next() : String.valueOf(System.console().readPassword());

        new Main(address, port, screen, id, password);
    }

    Main(String address, int port, Screen screen, short id, String password) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.dataSender = new DataSender(client, new RemoteMirror(id, password));
            dataSender.start();

            this.screenShotTimer = new ScreenShotTimer(screen, new Consumer<Screen>() {
                @Override
                public void accept(Screen screen) {
                    dataSender.sendScreen(screen);
                }
            });
            timer.schedule(screenShotTimer, 1, 500);

            this.mouseTrackTimer = new MouseTrackTimer(screen, new BiConsumer<Screen, int[]>() {
                @Override
                public void accept(Screen screen, int[] onRemoteScreenCoordinates) {
                    dataSender.sendMouseCoordinates((short)onRemoteScreenCoordinates[0], (short)onRemoteScreenCoordinates[1]);
                }
            });
            timer.schedule(mouseTrackTimer, 1, 50);

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

                case "quit", "stop", "q" -> {
                    timer.cancel();

                    dataSender.clearPending();
                    dataSender.sendShowDisconnectScreen();

                    System.exit(0);
                }

                case "help", "?" -> {
                    print("pause(p): (Un)pause screen mirroring");
                    print("quit(q):  Request server showing disconnect screen and then exit");
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
