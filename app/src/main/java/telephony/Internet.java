package telephony;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Internet {
	
	public static boolean isReachable()
	{
		try {
			InetAddress.getAllByName("google.com")[0].isReachable(3);
			return true;
		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}

}
