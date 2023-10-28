package depends.entity;

import java.io.Serializable;

public class Location implements Serializable {
	Integer line = null;
	// add for kotlin expression location
	Integer startIndex = null;
	Integer stopIndex = null;

	public Integer getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(Integer startIndex) {
		this.startIndex = startIndex;
	}

	public Integer getStopIndex() {
		return stopIndex;
	}

	public void setStopIndex(Integer endIndex) {
		this.stopIndex = endIndex;
	}

	public Integer getLine() {
		return line;
	}

	public void setLine(int line) {
		this.line = line;
	}
}
