package cc.eumc.screenmirroring.model;

import cc.eumc.eusmapdisplay.manager.GlobalMapManager;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.screenmirroring.EusScreenMirroring;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.map.MapPalette;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Arrays;
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

        // Remove default eventhandler so the cursor won't move by being targeted
        Arrays.stream(mapDisplay.getEventHandlers()).forEach(mapDisplay::removeEventHandler);

        this.mapDisplay = mapDisplay;
        this.windowWidth = mapDisplay.getWindowWidth();
        this.windowHeight = mapDisplay.getWindowHeight();
        this.mapDisplayUUID = mapDisplay.getUniqueId().toString();

        drawDisconnectedScreen();
    }

    public void drawDisconnectedScreen() {
        screen.fill((byte) 34);
        mapDisplay.getDisplay().setCursorLocation(screen.getWidth(), screen.getHeight());

        try {
            InputStream in = EusScreenMirroring.getInstance().getResource("disconnected.jpg");
            assert in != null;
            BufferedImage icon = ImageIO.read(in);
            if (icon != null) {
                int iconLeft = screen.getWidth() / 2 - icon.getWidth() / 2;
                int iconTop = screen.getHeight() / 2 - icon.getHeight() / 2;
                for (int y = 0; y < icon.getHeight(); y++) {
                    for (int x = 0; x < icon.getWidth(); x++) {
                        Color pixelColor = new Color(icon.getRGB(x, y));
                        screen.setPixel(iconLeft + x, iconTop + y, MapPalette.matchColor(pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue()));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
