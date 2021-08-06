package cc.eumc.screenmirroringclient;

import cc.eumc.screenmirroringclient.model.Screen;
import cc.eumc.screenmirroringclient.util.ArgumentParser;

import java.util.Scanner;

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
        long screenshotRefreshInterval = 500;
        long mouseCoordinateRefreshInterval = 50;
        int threads = -1;

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
        new ScreenStreamingClient(address, port, screen, id, password, screenshotRefreshInterval, mouseCoordinateRefreshInterval);
    }
}
