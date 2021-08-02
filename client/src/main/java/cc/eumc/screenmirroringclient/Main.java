package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.timer.ScreenShotTimer;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Timer;
import java.util.function.Consumer;

public class Main {
    UdpClient client;
    Timer timer;
    ScreenSender screenSender;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Address: ");
        String address = scanner.next();
        new Main(address, 17211);
    }

    Main(String address, int port) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.screenSender = new ScreenSender(client, new RemoteMirror((short) 0, "867740"));
            screenSender.start();

            timer.schedule(new ScreenShotTimer(4 * 128, 3 * 128, new Consumer<Screen>() {
                @Override
                public void accept(Screen screen) {
                    screenSender.sendScreen(screen);
                }
            }), 1, 500);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
}
