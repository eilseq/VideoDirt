package videodirt;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoLibrary {
	private static Path						library_path	;
	private static PathMatcher				matcher			=	FileSystems.getDefault().getPathMatcher("glob:**.{mov, mp4}");
	private static Map<Path, List<Path>>		library  		=	new HashMap<Path, List<Path>>();
	private static boolean					loaded;
	
	public static void load (String library_dir) {
		try {
			library_path = Paths.get(library_dir);
	
			Files.walkFileTree(library_path,  new SimpleFileVisitor<Path>() { 
	            @Override
	            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
	            {
	                if (matcher.matches(file)) {
	                		library.putIfAbsent(file.getParent(), new ArrayList<Path>());
	                		library.get(file.getParent()).add(file);
	                }
	
	                return FileVisitResult.CONTINUE;
	            }
	        }); 
			
			library.values().forEach(dir -> dir.sort((file1, file2) -> file1.compareTo(file2)) );

		} catch (IOException e) { /*do nothing*/ }
		
		loaded = true;
	}

	public static String getFilename (Object[] args) {		
		
		while (!loaded) { 
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) { /*do nothing*/ }
		}
		
		String	dir 	 = null;
		int		indx = 0	;
		for (int i=0; i < args.length; i++) {
			switch(args[i].toString()) {
				case "s": dir  = (String)args[i+1];
				break;
				case "n": indx = (Integer)args[i+1];
				break;
			}
		}
		
		return library.get(Paths.get(library_path+"/"+dir)).get(indx).toString();
	}
	
	public static boolean loaded() {
		return loaded;
	}
}
