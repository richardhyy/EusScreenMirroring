package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.RemoteMirror;
import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.timer.MouseTrackTimer;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.function.BiConsumer;

public class SlidesClient {
    private UdpClient client;
    private Timer timer;
    private DataSender dataSender;

    private MouseTrackTimer mouseTrackTimer;

    private Screen screen;

    private File slidesFile;
    private XMLSlideShow slideShow;
    private int currentPage;
    private byte[][] renderedPages;

    public SlidesClient(String address, int port, Screen screen, short id, String password, File slidesFile, long mouseCoordinateRefreshInterval) {
        try {
            this.client = new UdpClient(InetAddress.getByName(address), port);
            this.timer = new Timer();
            this.dataSender = new DataSender(client, new RemoteMirror(id, password));
            dataSender.start();

            this.screen = screen;
            this.mouseTrackTimer = new MouseTrackTimer(screen, new BiConsumer<Screen, int[]>() {
                @Override
                public void accept(Screen screen, int[] onRemoteScreenCoordinates) {
                    dataSender.sendMouseCoordinates((short)onRemoteScreenCoordinates[0], (short)onRemoteScreenCoordinates[1]);
                }
            });
            timer.schedule(mouseTrackTimer, 1, mouseCoordinateRefreshInterval);

        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            print(String.format("Loading %s, this may take a while.", slidesFile.getName()));
            loadDocument(slidesFile);
            sendPage(0);
        } catch (IOException e) {
            print(String.format("ERROR: %s", e.getMessage()));
            return;
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
                case "next", "n" -> {
                    nextPage();
                }

                case "previous", "v" -> {
                    previousPage();
                }

                case "goto", "g" -> {
                    if (args.length != 2) {
                        print("Usage: goto <page number (start from 1)>");
                        break;
                    }
                    try {
                        sendPage(Integer.parseInt(args[1]) - 1);
                    } catch (Exception e) {
                        print(String.format("ERROR: %s", e.getMessage()));
                    }
                }

                case "pause", "p" -> {
                    mouseTrackTimer.setPaused(!mouseTrackTimer.isPaused());

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
                    print("next(n)    :  Next slide");
                    print("previous(v):  Previous slide");
                    print("goto(g)    :  Goto page");
                    print("pause(p)   :  Show/hide disconnect screen");
                    print("quit(q)    :  Request server showing disconnect screen and then exit");
                }
            }
        }
    }

    public void loadDocument(File file) throws IOException {
        this.slidesFile = file;
        this.slideShow = new XMLSlideShow(new FileInputStream(slidesFile));
        this.currentPage = 0;
        this.renderedPages = new byte[getTotalPages()][];

        Dimension dimension = slideShow.getPageSize();
        List<XSLFSlide> slides = slideShow.getSlides();
        BufferedImage img = new BufferedImage(dimension.width, dimension.height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        for (int i = slides.size() - 1; i >= 0; i--) {
            slides.get(i).draw(graphics);
            screen.setScreenshot(img);
            renderedPages[i] = screen.getFlattenedPixels();
        }
    }

    public void sendPage(int page) {
        if (slideShow == null) {
            return;
        }

        if (page < 0 || page >= renderedPages.length) {
            // out of bound
            return;
        }

        screen.setFlattenedPixels(renderedPages[page]);
        dataSender.sendScreen(screen);
    }

    public void nextPage() {
        if (slideShow == null) {
            return;
        }

        if (currentPage < getTotalPages() - 1) {
            currentPage++;
            sendPage(currentPage);
        }
    }

    public void previousPage() {
        if (slideShow == null) {
            return;
        }

        if (currentPage > 0) {
            currentPage--;
            sendPage(currentPage);
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


    // Getters
    public Screen getScreen() {
        return screen;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getTotalPages() {
        return slideShow.getSlides().size();
    }
}
