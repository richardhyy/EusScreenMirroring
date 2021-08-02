package cc.eumc.screenmirroring.manager;

import cc.eumc.screenmirroring.EusScreenMirroring;
import cc.eumc.screenmirroring.server.ScreenServer;

import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class ScreenServerManager {
    private EusScreenMirroring plugin;
    private List<ScreenServer> screenServerList = new ArrayList<>();

    public ScreenServerManager(EusScreenMirroring plugin) {
        this.plugin = plugin;

        int succeeded = startServers();
        System.out.printf("Listening on %d port(s).%n", succeeded);
    }

    public int startServers() {
        int succeeded = 0;
        for (int port : plugin.getConfig().getIntegerList("General.ScreenServerPorts")) {
            try {
                ScreenServer _server = new ScreenServer(plugin.getMirrorManager(), port);
                _server.start();
                screenServerList.add(_server);
                System.out.println("Listening on " + port);
                succeeded++;
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return succeeded;
    }

    public void stopServers() {
        screenServerList.forEach(ScreenServer::stopServer);
    }
}
