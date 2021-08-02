package cc.eumc.screenmirroringclient.model;

public class RemoteMirror {
    short id;
    String password;

    public RemoteMirror(short id, String password) {
        this.id = id;
        this.password = password;
    }

    public short getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }
}
