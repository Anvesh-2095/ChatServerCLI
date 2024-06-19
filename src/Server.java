import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable
{
	private ArrayList<ConnectionHandler> connections;
	private ServerSocket server;
	private boolean done;
	private ExecutorService pool;

	public Server()
	{
		connections = new ArrayList<ConnectionHandler>();
		done = false;
	}

	@Override
	public void run()
	{
		try
		{
			server = new ServerSocket(8080);
			pool = Executors.newCachedThreadPool();
			while (!done)
			{
				Socket client = server.accept();
				ConnectionHandler handler = new ConnectionHandler(client);
				connections.add(handler);
				pool.execute(handler);
			}
		} catch (Exception e)
		{
			shutDown();
		}
	}

	public void shutDown() // TODO: exit messages are appearing twice
	{
		try
		{
			done = true;
			pool.shutdown();
			if (!server.isClosed())
				server.close();
			for (ConnectionHandler ch: connections)
			{
				ch.shutDown();
			}
		} catch (IOException e)
		{
			// Ignore the exception
			e.printStackTrace();
		}
	}

	class ConnectionHandler implements Runnable
	{
		private Socket client;
		private BufferedReader in;
		private PrintWriter out;
		private String alias;

		public ConnectionHandler(Socket client)
		{
			this.client = client;
		}

		public void broadcast(String message)
		{
			for (ConnectionHandler ch: connections)
			{
				if (ch != null)
				{
					ch.sendMessage(message);
				}
			}
		}

		@Override
		public void run()
		{
			try
			{
				out = new PrintWriter(client.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(client.getInputStream()));

				out.println("Please enter an alias: ");
				alias = in.readLine(); // TODO: check if alias is already taken and validate it too

				System.out.println(alias + " has connected.");
				broadcast(alias + " has joined the chat!");

				String message;
				while ((message = in.readLine()) != null)
				{
					if (message.startsWith("/quit"))
					{
//						broadcast(alias + " has left the chat.");
//						System.out.println(alias + " has disconnected.");
						shutDown();
					}
					else if (message.startsWith("/nick"))
					{
						String[] messageSplit = message.split(" ", 2);
						if (messageSplit.length == 2)
						{
							String newAlias = messageSplit[1];
							System.out.println(alias + " has changed their alias to " + newAlias);
							broadcast(alias + " has changed their alias to " + newAlias);
							alias = newAlias;
							out.println("Your alias has been changed to " + alias);
						}
						else
						{
							out.println("Invalid command. Usage: /nick <new alias>");
						}
					}
					else
					{
						System.out.println(alias + ": " + message);
						broadcast(alias + ": " + message);
					}
				}
			} catch (IOException e)
			{
				shutDown();
			}
		}

		public void sendMessage(String message)
		{
			out.println(message);
		}

		public void shutDown()
		{
			try
			{
				in.close();
				broadcast(alias + " has left the chat."); // TODO: handle when server shut down and quit differently
				System.out.println(alias + " has disconnected.");
				out.close();
				if (!client.isClosed())
				{
					client.close();
				}
			} catch (IOException e)
			{
				// Ignore the exception
				e.printStackTrace();
			}
		}
	}

	public static void main(String... args)
	{
		Server server = new Server();
		server.run();
	}
}
