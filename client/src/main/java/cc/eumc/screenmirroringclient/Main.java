package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.util.ArgumentParser;

import java.io.File;
import java.util.Scanner;

public class Main {
    final static String HelpText = """
            --address | -a : Server address (without port)
            --port | -p    : EusScreenMirroring port (EusScreenMirroring default: 17211)
            --width | -w   : Display window width
            --height | -h  : Display window height
            --id | -i      : Mirror ID (Get by using /screenmirroring list)
            --password | -d: Mirror password (Get by using /screenmirroring list)
            
            Slideshow:
            [Specify any of the following argument will enable slideshow mode (without streaming your screen)]
            --pptx         : Path to Office Open XML file
            
            Playback:
            --playback     : Path to EusScreenMirroring .rec file
            
            Optional arguments:
            --screenshotRefreshInterval      : Time interval between screenshot refreshes (in milliseconds) (default: 250)
            --mouseCoordinateRefreshInterval : Time interval between mouse location refreshes (in milliseconds) (default: 50)
            --threads                        : Threads used for processing screenshots (default: -1, utilizing all available CPU threads)
            """;

    @SuppressWarnings("ConstantConditions")
    public static void main(String[] args) {
        String address;
        int port;
        int windowWidth;
        int windowHeight;
        short id;
        String password;
        long screenshotRefreshInterval = 250;
        long mouseCoordinateRefreshInterval = 50;
        int threads = -1;
        String pptx = null;
        File playbackRecordFile = null;

        if (args.length > 0) {
            // with arguments
            ArgumentParser argumentParser = new ArgumentParser(args);
            if (argumentParser.checkSwitch("--help")) {
                System.out.println(Main.HelpText);
                return;
            }

            String playbackRecordPathString = argumentParser.parse("--playback");
            if (playbackRecordPathString != null) {
                playbackRecordFile = new File(playbackRecordPathString);
                if (!playbackRecordFile.exists()) {
                    System.err.printf("%s not found.%n", playbackRecordPathString);
                    return;
                }
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
                String threadString = argumentParser.parse("--threads");
                if (screenshotRefreshIntervalString != null) {
                    screenshotRefreshInterval = Long.parseLong(screenshotRefreshIntervalString);
                }
                if (mouseCoordinateRefreshIntervalString != null) {
                    mouseCoordinateRefreshInterval = Long.parseLong(mouseCoordinateRefreshIntervalString);
                }
                if (threadString != null) {
                    threads = Integer.parseInt(threadString);
                }

                // slideshow mode
                pptx = argumentParser.parse("--pptx");

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


        Screen screen = new Screen(windowWidth * 128, windowHeight * 128, threads < 0 ? Runtime.getRuntime().availableProcessors() : threads);

        if (pptx != null) {
            new SlidesClient(address, port, screen, id, password, new File(pptx), mouseCoordinateRefreshInterval);
        } else if (playbackRecordFile != null) {
            new PlaybackClient(address, port, id, password, playbackRecordFile);
        }
        else {
            new ScreenStreamingClient(address, port, screen, id, password, screenshotRefreshInterval, mouseCoordinateRefreshInterval);
        }
    }
}
