package cc.eumc.screenmirroring.model;

import cc.eumc.eusmapdisplay.manager.GlobalMapManager;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.screenmirroring.EusScreenMirroring;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class Mirror {
    private static final int PasswordLength = 6;

    private String ownerUUID;
    private String mapDisplayUUID;
    private String password;
    private int windowWidth;
    private int windowHeight;

    private transient MapDisplay mapDisplay;
    private transient File mirrorMetafile;
    private transient Screen screen;

    public Mirror(UUID owner, int windowWidth, int windowHeight, File mirrorFolder, AtomicReference<Short> refID) {
        this.ownerUUID = owner.toString();
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.setupScreen();
        this.setMapDisplay(EusScreenMirroring.getInstance().getEusMapDisplayPlugin().getMapManager().createMap(windowWidth, windowHeight, Bukkit.getWorlds().get(0)));
        this.createPassword();
        short id = nextID(mirrorFolder);
        refID.set(id);
        this.mirrorMetafile = new File(mirrorFolder, id + ".json");

        //System.out.printf("New mirror (%s)%n", mirrorMetafile.getName());

        try {
            save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static short nextID(File mirrorFolder) {
        short newId = 0;
        while (true) {
            if (new File(mirrorFolder, newId + ".json").exists()) {
                newId++;
            } else {
                break;
            }
        }
        return newId;
    }

    public static Mirror loadFromMetafile(File metafile) throws Exception {
        Mirror mirror = EusScreenMirroring.getInstance().getGson().fromJson(new JsonReader(new FileReader(metafile)), Mirror.class);
        mirror.mirrorMetafile = metafile;

        if (mirror.getPassword() == null || mirror.getPassword().length() != PasswordLength) {
            mirror.createPassword();
        }
        mirror.setupScreen();

        GlobalMapManager mapManager = EusScreenMirroring.getInstance().getEusMapDisplayPlugin().getMapManager();
        try {
            mirror.setMapDisplay(mapManager.getMapDisplay(UUID.fromString(mirror.mapDisplayUUID)));
        } catch (Exception ex) {
            ex.printStackTrace();
            // failed binding w/ an existing MapDisplay
//            mirror.setMapDisplay(mapManager.createMap(mirror.getWindowWidth(), mirror.getWindowHeight(), Bukkit.getWorlds().get(0)));
        }
        return mirror;
    }

    public void save() throws IOException {
        Files.writeString(mirrorMetafile.toPath(), EusScreenMirroring.getInstance().getGson().toJson(this));
    }

    public void setupScreen() {
        this.screen = new Screen(windowWidth * 128, windowHeight * 128);
    }

    public void setMapDisplay(MapDisplay mapDisplay) {
//        System.out.printf("Bound w/ %s%n", mapDisplay.getUniqueId());

        this.mapDisplay = mapDisplay;
        this.windowWidth = mapDisplay.getWindowWidth();
        this.windowHeight = mapDisplay.getWindowHeight();
        this.mapDisplayUUID = mapDisplay.getUniqueId().toString();
    }


    /**
     * Reset the Mirror's password to a new six-digit one
     * @return password string whose length = 6
     */
    public String createPassword() {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < PasswordLength; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        this.password = stringBuilder.toString();
        return this.password;
    }

    public String getOwnerUUID() {
        return ownerUUID;
    }

    public String getPassword() {
        return password;
    }

    public int getWindowWidth() {
        return windowWidth;
    }

    public int getWindowHeight() {
        return windowHeight;
    }

    public MapDisplay getMapDisplay() {
        return mapDisplay;
    }

    public Screen getScreen() {
        return screen;
    }

    public File getMirrorMetafile() {
        return mirrorMetafile;
    }
}
