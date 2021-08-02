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

        new Main(address, port, windowWidth * 128, windowHeight * 128);
    }

    Main(String address, int port, int remoteScreenWidth, int remoteScreenHeight) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.dataSender = new DataSender(client, new RemoteMirror((short) 0, "867740"));
            dataSender.start();

            Screen screen = new Screen(remoteScreenWidth, remoteScreenHeight);

            timer.schedule(new ScreenShotTimer(screen, new Consumer<Screen>() {
                @Override
                public void accept(Screen screen) {
                    dataSender.sendScreen(screen);
                }
            }), 1, 500);

            timer.schedule(new MouseTrackTimer(screen, new BiConsumer<Screen, int[]>() {
                @Override
                public void accept(Screen screen, int[] onRemoteScreenCoordinates) {
                    dataSender.sendMouseCoordinates((short)onRemoteScreenCoordinates[0], (short)onRemoteScreenCoordinates[1]);
                }
            }), 1, 50);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
