package actors;

public enum EventTypes {

	JOIN("join"), QUIT("quit"), MESSAGE("message");
	
	private String name;
	
	private EventTypes(String name) {
		this.name = name;
	}
	
	public String getName() {
		return this.name;
	}
	
}
