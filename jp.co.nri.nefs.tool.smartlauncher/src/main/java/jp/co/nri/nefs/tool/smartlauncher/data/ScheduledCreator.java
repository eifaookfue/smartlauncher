package jp.co.nri.nefs.tool.smartlauncher.data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScheduledCreator {

	private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	private DataModelUpdater dataModelUpdater;
	private Path directoryPath;
	private Path aliasPath;
	private Logger logger = LoggerFactory.getLogger(getClass());

	public ScheduledCreator(DataModelUpdater dataModelUpdater, Path directoryPath, Path aliasPath) {
		this.dataModelUpdater = dataModelUpdater;
		this.directoryPath = directoryPath;
		this.aliasPath = aliasPath;

	}

	public List<File> createList() throws IOException{
		return Files.readAllLines(directoryPath).stream()
				.map(File::new)
				.flatMap(dir ->
					Arrays.stream(dir.listFiles(f -> f.isFile())))
				.collect(Collectors.toList());
	}

	public Map<String, String> createAlias() {
		if (aliasPath != null) {
			try {
				return Files.readAllLines(aliasPath).stream().map(line -> line.split(","))
					.collect(Collectors.toMap(
						lines -> lines[0],
						lines -> lines[1]
								));
			} catch (IOException e) {
				logger.warn("", e);
				return null;
			}
		}
		return null;
	}

	public void start(){
		Runnable r = () -> {
			try {
				Map<String, String> newAlias = createAlias();
				dataModelUpdater.replaceAlias(newAlias);
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
