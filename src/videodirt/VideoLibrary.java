package videodirt;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

class VideoLibrary {
    private static Path library_path;
    private static PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:{**.MOV,**.mov,**.MP4, **.mp4}");
    private static Map<Path, List<Path>> library = new HashMap<>();
    private static boolean loaded;

    static void load(String library_dir) {
        try {
            library_path = Paths.get(library_dir);

            Files.walkFileTree(library_path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (matcher.matches(file)) {
                        library.putIfAbsent(file.getParent(), new ArrayList<>());
                        library.get(file.getParent()).add(file);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });

            library.values().forEach(dir -> dir.sort(Comparator.naturalOrder()));

        } catch (IOException e) { /*do nothing*/ }

        loaded = true;
    }

    static File getFile(VideoClip clip) {

        if (!loaded) return null;

        int indx = clip.getNum();
        StringBuilder dir_path = new StringBuilder(library_path.toString());
        dir_path.append("/");
        dir_path.append(clip.getDir());

        try {
            return new File(library.get(Paths.get(dir_path.toString())).get(indx).toString());
        } catch (Exception e) {
            return null;
        }
    }

    static boolean loaded() {
        return loaded;
    }
}
