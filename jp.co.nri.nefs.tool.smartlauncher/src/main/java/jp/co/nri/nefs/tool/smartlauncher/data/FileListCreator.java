package jp.co.nri.nefs.tool.smartlauncher.data;

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
	private DataModelUpdater dataModelUpdater;
	private Path directoryFile;

	public FileListCreator(DataModelUpdater dataModelUpdater, Path directoryFile) {
		this.dataModelUpdater = dataModelUpdater;
		this.directoryFile = directoryFile;

	}

	public List<File> createList() throws IOException{
		return Files.readAllLines(directoryFile).stream()
				.map(File::new)
				.flatMap(dir ->
					Arrays.stream(dir.listFiles(f -> f.isFile())))
				.collect(Collectors.toList());
	}

	public void start(){
		Runnable r = () -> {
			try {
				List<File> newList = createList();
				//newList.forEach(System.out::println);
				dataModelUpdater.replaceList(newList);
			} catch (IOException e) {
				// TODO 自動生成された catch ブロック
				e.printStackTrace();
			}
		};
		executor.scheduleAtFixedRate(r, 0, 5, TimeUnit.SECONDS);
	}

	public void stop() {
		executor.shutdown();
	}


}
