package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.timer.MouseTrackTimer;
import cc.eumc.screenmirroringclient.timer.ScreenShotTimer;
import cc.eumc.screenmirroringclient.util.ArgumentParser;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {
    final static String HelpText = """
            --address | -a : Server address (without port)
            --port | -p    : EusScreenMirroring port (EusScreenMirroring default: 17211)
            --width | -w   : Display window width
            --height | -h  : Display window height
            --id | -i      : Mirror ID (Get by using /screenmirroring list)
            --password | -d: Mirror password (Get by using /screenmirroring list)
            
            Optional arguments:
            --screenshotRefreshInterval      : Time interval between screenshot refreshes (in milliseconds)
            --mouseCoordinateRefreshInterval : Time interval between mouse location refreshes (in milliseconds)
            """;

    UdpClient client;
    Timer timer;
    DataSender dataSender;

    MouseTrackTimer mouseTrackTimer;
    ScreenShotTimer screenShotTimer;

    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) {
        String address;
        int port;
        int windowWidth;
        int windowHeight;
        short id;
        String password;
        long screenshotRefreshInterval = 500;
        long mouseCoordinateRefreshInterval = 50;

        if (args.length > 0) {
            // with arguments
            ArgumentParser argumentParser = new ArgumentParser(args);
            if (argumentParser.checkSwitch("--help")) {
                System.out.println(Main.HelpText);
                return;
            }
            try {
                address = argumentParser.parse("--address", "-a");
                port = Integer.parseInt(argumentParser.parse("--port", "-p"));
                windowWidth = Integer.parseInt(argumentParser.parse("--width", "-w"));
                windowHeight = Integer.parseInt(argumentParser.parse("--height", "-h"));
                id = Short.parseShort(argumentParser.parse("--id", "-i"));
                password = argumentParser.parse("--password", "-d");

                // optionals
                String screenshotRefreshIntervalString = argumentParser.parse("--screenshotRefreshInterval");
                String mouseCoordinateRefreshIntervalString = argumentParser.parse("--mouseCoordinateRefreshInterval");
                if (screenshotRefreshIntervalString != null) {
                    screenshotRefreshInterval = Long.parseLong(screenshotRefreshIntervalString);
                }
                if (mouseCoordinateRefreshIntervalString != null) {
                    mouseCoordinateRefreshInterval = Long.parseLong(mouseCoordinateRefreshIntervalString);
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.err.printf("** Error parsing arguments, caused by %s **%n", e.getMessage());
                return;
            }
        } else {
            // without arguments
            System.out.println("** No arguments specified. You can use --help for argument list. **");

            Scanner scanner = new Scanner(System.in);
            System.out.print("Address: ");
            address = scanner.next();

            System.out.print("Port (default: 17211): ");
            port = scanner.nextInt();


            System.out.print("Mirror width (default: 4): ");
            windowWidth = scanner.nextInt();

            System.out.print("Mirror width (default: 3): ");
            windowHeight = scanner.nextInt();

            System.out.print("Mirror ID: ");
            id = (short) scanner.nextInt();

            System.out.print("Password: ");
            password = System.console() == null ? scanner.next() : String.valueOf(System.console().readPassword());
        }

        Screen screen = new Screen(windowWidth * 128, windowHeight * 128);
        new Main(address, port, screen, id, password, screenshotRefreshInterval, mouseCoordinateRefreshInterval);
    }

    Main(String address, int port, Screen screen, short id, String password, long screenshotRefreshInterval, long mouseCoordinateRefreshInterval) {
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
