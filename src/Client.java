import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;

// TODO: Handle NullPointerExceptions
public class Client implements Runnable
{
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private boolean done;
	private String host = "localhost";

	public Client()
	{
		done = false;
	}

	public Client(String host)
	{
		done = false;
		this.host = host;
	}

	@Override
	public void run()
	{
		try
		{
			client = new Socket(host, 8080);
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			 InputHandler inHandler = new InputHandler();
			 Thread t = new Thread(inHandler);
			 t.start(); // run does not open a new thread but start does

			String inMessage;
			while ((inMessage = in.readLine()) != null)
			{
				if (inMessage.equals("Please enter an alias: "))
				{
					System.out.print(inMessage);
					continue;
				}
				System.out.println(inMessage);
			}
		} catch (IOException e)
		{
			shutDown();
		}
	}

	public void shutDown()
	{
		done = true;
		try
		{
			if (in != null)
				in.close();
			if (out != null)
				out.close();
			if (!client.isClosed())
				client.close();
		} catch (IOException e)
		{
			// ignore
		}
	}

	class InputHandler implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
				while (!done)
				{
					String message = inReader.readLine();
					if(message.equals("/quit"))
					{
						out.println(message); // if not done, terminal will be useless
						inReader.close();
						shutDown();
					}
					else
					{
						out.println(message);
					}
				}
			} catch (IOException e)
			{
				shutDown();
			}
		}
	}

	public static void main(String[] args)
	{
		Client client = new Client();
		client.run();
	}
}
