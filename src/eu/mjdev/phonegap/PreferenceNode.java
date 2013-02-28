package eu.mjdev.phonegap;

public class PreferenceNode {
    
	public String name;
    public String value;
    public boolean readonly;

    public PreferenceNode(String name, String value, boolean readonly) {
        this.name = name;
        this.value = value;
        this.readonly = readonly;
    }
    
}
