package src;

public class FileInfo {

	private String path;
	private int Nchunks;
	private int replicationDeg;
	
	public FileInfo(String path, int Nchunks, int replicationDeg){
		this.path = path;
		this.Nchunks = Nchunks;
		this.replicationDeg = replicationDeg;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getNchunks() {
		return Nchunks;
	}

	public void setNchunks(int nchunks) {
		Nchunks = nchunks;
	}

	public int getReplicationDeg() {
		return replicationDeg;
	}

	public void setReplicationDeg(int replicationDeg) {
		this.replicationDeg = replicationDeg;
	}
	
}
