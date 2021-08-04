package cc.eumc.screenmirroringclient.util;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class ArgumentParser {
    String[] args;

    public ArgumentParser(String[] args) {
        this.args = args;
    }

    public boolean checkSwitch(String token) {
        return checkSwitch(token, null);
    }

    public boolean checkSwitch(String token, String alias) {
        return Arrays.stream(args).anyMatch(s -> (s.equalsIgnoreCase(token) || s.equalsIgnoreCase(alias)));
    }


    public @Nullable String parse(String token) {
        return parse(token, null);
    }

    public @Nullable String parse(String token, String alias) {
        int indexOfToken = indexOf(args, token);
        if (alias != null && indexOfToken == -1) { // check alias
            indexOfToken = indexOf(args, alias);
        }

        if (indexOfToken == -1) {
            return null;
        } else if (args.length - 1 == indexOfToken) { // no value right after the token
            return null;
        } else {
            return args[indexOfToken + 1];
        }
    }

    private int indexOf(String[] array, String valueToFind) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equalsIgnoreCase(valueToFind)) {
                return i;
            }
        }
        return -1;
    }
}
