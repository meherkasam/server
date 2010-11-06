import java.io.File;

public class FileObject {
	public File fileHandle;
	public CustomReadWriteLock lock;
	FileObject(File handle) {
		fileHandle = handle;
		lock = new CustomReadWriteLock();
	}
}
