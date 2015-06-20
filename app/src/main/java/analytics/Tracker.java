package analytics;

public interface Tracker {
	public void sendEvent(String category, String action, String label, long value);

}
