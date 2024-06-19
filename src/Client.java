import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable
{
	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private boolean done;
	@Override
	public void run()
	{
		try
		{
			 client = new Socket("localhost", 8080);
			 out = new PrintWriter(client.getOutputStream(), true);
			 in = new BufferedReader(new InputStreamReader(client.getInputStream()));

			 InputHandler inHandler = new InputHandler();
			 Thread t = new Thread(inHandler);
			 t.start(); // run does not open a new thread but start does

			String inMessage;
			while ((inMessage = in.readLine()) != null)
			{
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
			in.close();
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
					else if (message.equals("Please enter an alias:"))
					{
						out.print(message);
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
