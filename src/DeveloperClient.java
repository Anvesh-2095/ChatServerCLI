import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DeveloperClient
{
	public static void main(String[] args) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		System.out.print("Enter the host: ");
		String host = br.readLine();

		Client client = new Client(host);
		client.run();
	}
}
