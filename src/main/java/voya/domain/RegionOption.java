package voya.domain;

public class RegionOption {
	
	private String option;
	private String type;
	
	public String getOption() {
		return option;
	}
	public void setOption(String option) {
		this.option = option;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString() {
		return "RegionOption [option=" + option + ", type=" + type + "]";
	}
}
