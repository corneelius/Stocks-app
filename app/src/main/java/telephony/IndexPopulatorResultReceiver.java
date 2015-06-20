package telephony;

import data.Index;

public interface IndexPopulatorResultReceiver {
	public void updateIncicies(Index index);
	public void updateProgress(int progress);
	public void noDataNotifyIndex(Index index);

}
