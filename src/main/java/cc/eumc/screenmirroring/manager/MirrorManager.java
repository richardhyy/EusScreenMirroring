package cc.eumc.screenmirroring.manager;

import cc.eumc.screenmirroring.EusScreenMirroring;
import cc.eumc.screenmirroring.model.Mirror;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class MirrorManager {
    private EusScreenMirroring plugin;
    private Map<Short, Mirror> mirrorMap = new HashMap<>();

    public MirrorManager(EusScreenMirroring plugin) {
        this.plugin = plugin;

        File[] metafiles = plugin.getMirrorFolder().listFiles((dir, name) -> name.endsWith(".json"));
        assert metafiles != null;
        for (File metafile : metafiles) {
            try {
                Mirror mirror = Mirror.loadFromMetafile(metafile);
                short id = Short.parseShort(metafile.getName().substring(0, metafile.getName().indexOf(".")));
                mirrorMap.put(id, mirror);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public @Nullable Mirror getMirror(short id) {
        return mirrorMap.get(id);
    }

    public Collection<Mirror> getMirrors() {
        return mirrorMap.values();
    }

    public Map<Short, Mirror> getMirrorMap() {
        return mirrorMap;
    }

    public Mirror createMirror(UUID owner, int windowWidth, int windowHeight) {
        AtomicReference<Short> refID = new AtomicReference<>();
        Mirror mirror = new Mirror(owner, windowWidth, windowHeight, plugin.getMirrorFolder(), refID);
        mirrorMap.put(refID.get(), mirror);
        return mirror;
    }

    public void removeMirror(short id) {
        Mirror mirror = getMirror(id);
        if (mirror != null) {
            mirrorMap.remove(id);
            mirror.getMirrorMetafile().delete();
        }
    }

    public void saveAll() {
        mirrorMap.forEach((id, mirror) -> {
            try {
                mirror.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}
