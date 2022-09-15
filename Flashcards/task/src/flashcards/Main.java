package flashcards;

import java.util.*;

public class Main {

    public static void main(String[] args) {

        List<String> argsList = List.of(args);

        String importFileName = argsList.contains("-import") ?
                argsList.get(argsList.indexOf("-import") + 1) : null;

        String exportFileName = argsList.contains("-export") ?
                argsList.get(argsList.indexOf("-export") + 1) : null;

        App app = new App(Optional.of(importFileName), Optional.of(exportFileName));
        app.start();

    }

}
