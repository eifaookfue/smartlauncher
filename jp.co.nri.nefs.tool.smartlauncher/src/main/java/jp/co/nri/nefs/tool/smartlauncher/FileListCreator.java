package jp.co.nri.nefs.tool.smartlauncher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class FileListCreator {

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private SmartFrame frame;
	private Path directoryFile;

	public FileListCreator(SmartFrame frame, Path directoryFile) {
		this.frame = frame;
		this.directoryFile = directoryFile;

	}

	private List<File> createList(Path directoryFile) throws IOException{
		return Files.readAllLines(directoryFile).stream()
				.map(File::new)
				.flatMap(dir ->
					Arrays.stream(dir.listFiles(f -> f.isFile())))
				.collect(Collectors.toList());
	}

	public void start(){
		Runnable r = () -> {
			try {
				List<File> newList = createList(directoryFile);
				//newList.forEach(System.out::println);
				frame.replaceList(newList);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		};
		executor.	scheduleAtFixedRate(r, 0, 5, TimeUnit.SECONDS);
	}

	public void stop() {
		executor.shutdown();
	}


}
