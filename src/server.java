
public class server {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Listener.serverRoot = args[1];
		new Listener(args[0]);
		new CommandProcessor();
	}

}
