package principale;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;


import client.MulinoClient;
import it.unibo.ai.didattica.mulino.domain.State.Checker;
import it.unibo.ai.didattica.mulino.domain.State.Phase;
import it.unibo.ai.didattica.mulino.actions.Action;
import it.unibo.ai.didattica.mulino.actions.Phase1Action;
import it.unibo.ai.didattica.mulino.actions.Phase2Action;
import it.unibo.ai.didattica.mulino.actions.PhaseFinalAction;
import it.unibo.ai.didattica.mulino.domain.State;




public class JavaInterfaceServer extends MulinoClient{

	public JavaInterfaceServer(Checker player) throws UnknownHostException, IOException {
		super(player);
		// TODO Auto-generated constructor stub
	}

	public static final int PYTHON_PORT = 3033;
	
	public static final String [] mapping = {
	                                         "a7","d7","g7",
	                                         "b6","d6","f6",
	                                         "c5","d5","e5",
	                                         "a4","b4","c4","e4","f4","g4",
	                                         "c3","d3","e3",
	                                         "b2","d2","f2",
	                                         "a1","d1","g1"
	                                         };
	
	public static void main (String [] args) {
		
		
		
		ServerSocket serverSocket = null;
		Socket pythonSocket = null;
		
		/* INIZIALIZZAZIONE SOCKET E START DEL SERVER */
		try {
			serverSocket = new ServerSocket(PYTHON_PORT);
			serverSocket.setReuseAddress(true);
			System.out.println("JavaInterface: avviato, porta agente: " + PYTHON_PORT);
		} catch (Exception e) {
			System.err.println("JavaInterface: problemi nella creazione della server socket: " + e.getMessage());
			e.printStackTrace();
			System.exit(-1);
		}

		/**Per prima cosa si stabilisce una connessione con l'agente python*/
		try {
			System.out.println("JavaInterface: attendo la connessione con l'agente...\n");

			try {
				
				pythonSocket = serverSocket.accept();
				
				pythonSocket.setSoTimeout(70000);
				System.out.println("JavaInterface: connessione avvenuta con successo: " + pythonSocket);
				
				/*Incapsulamento del canale di comunicazione con python*/
				DataInputStream pythonInSock = null;
				DataOutputStream pythonOutSock = null;
				
				try {
					pythonInSock = new DataInputStream(pythonSocket.getInputStream()); 
					pythonOutSock = new DataOutputStream(pythonSocket.getOutputStream());
				} catch (IOException ioe) {
					System.out.println("Problemi nella creazione degli stream di input/output " + "su socket: ");
					ioe.printStackTrace();
				}
				
				try {
					
					State.Checker player = null;
					State currentState = null;
					MulinoClient client = null;
					/* FASE DI COMUNICAZIONE */
					/*
					 * Terminerà con un eccezione che andrò a catturare con il try/catch
					 */
					byte [] my_byte = new byte[1];
					
					int letti = pythonInSock.read(my_byte);
					//System.out.println("Ho letto: " + JavaInterfaceServer.toText(my_byte));
		
					String playerInString = toText(my_byte);
					System.out.println(playerInString + ": " + letti);
					
					if(playerInString.equalsIgnoreCase("B")) {
						System.out.println("Giocatore selezionato: " + playerInString);
						player = Checker.BLACK;
						client = new JavaInterfaceServer(player);
						
					}else if (playerInString.equalsIgnoreCase("W")) {
						System.out.println("Giocatore selezionato: " + playerInString);
						player = Checker.WHITE;
						client = new JavaInterfaceServer(player);
						
					}else
						System.out.println("Giocatore selezionato non valido");
					
					if(client != null) {
						
						System.out.println("Current state:");
						currentState = client.read();
						System.out.println(currentState.toString());
						
						/*Invio lo stato corrente a python*/
						pythonOutSock.write(currentState.toCompactString().getBytes());
						
						if(player.compareTo(Checker.BLACK)==0) {
							/*attendo dal server lo stato con la mossa del bianco*/
							System.out.println("Attendo la mossa dell'avversario...");
							currentState = client.read();
							System.out.println(currentState.toString());
							
							System.out.println("Invio lo stato all'agente...");
							pythonOutSock.write(currentState.toCompactString().getBytes());
							System.out.println("Stato inviato!");
							
						}
						byte [] mossa_byte;
						while(true) {
							mossa_byte = new byte[15];
							
							System.out.println("\nSono in attesa di una mossa dall'agente...");
							/*leggo la mossa dall'agente*/
							pythonInSock.read(mossa_byte);
							String mossa = toText(mossa_byte);
							System.out.println("\nMossa ricevuta: " + mossa);
							
							/*Invio la mossa al server*/
							Action myAction = stringToAction(toSend(mossa), currentState.getCurrentPhase());
							System.out.println("Azione che sto per inviare: " + myAction.toString());
							client.write(myAction);
							
							/*Attendo il nuovo stato di aggiornamento...*/
							System.out.println("Attendo lo stato di aggiornamento della scacchiera...");
							currentState = client.read();
							System.out.println("Stato ricevuto: \n" + currentState.toString());
							
							/*Invio il nuovo stato generato dalla mossa dell'agente all'agente*/
							System.out.println("Invio lo stato all'agente...");
							pythonOutSock.write(currentState.toCompactString().getBytes());
							System.out.println("Stato inviato...");
							
							/*ricezione mossa dell'avversario */
							System.out.println("\n\nAttendo mossa dell'avversario...");
							currentState = client.read();
							System.out.println(currentState.toString());
							
							System.out.println("Invio lo stato all'agente...");
							pythonOutSock.write(currentState.toCompactString().getBytes());
							System.out.println("Stato inviato!");
							
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
				System.out.println("\n\nJavaInterface chiudo il canale di comunicazione...");
				// CHIUDO LA SOCKET
				try {
					pythonSocket.shutdownOutput();
					pythonSocket.shutdownInput();
					pythonSocket.close();
				} catch (IOException ex) {
					System.out.println("JavaInterface: Errore nella chiusura della socket");
					ex.printStackTrace();
				}
			} catch (Exception e) {
				System.err.println("JavaInterface: problemi nella accettazione della connessione: " + e.getMessage());
				e.printStackTrace();
				
			}
			
	}catch (Exception e) {
			e.printStackTrace();
			System.out.println("Server: termino...");
			System.exit(2);
		}
	}
	
	public static String toText(byte[] encoded)throws UnsupportedEncodingException{
        
        String text = new String(encoded, "UTF-8");
        //System.out.println("print: "+text);
        return text;
    }
	
	public static byte[] toByte(String info){
	        return info.getBytes();
	 }
	
	public static String toSend(String fromPython) {
		String toReturn = "";
		fromPython = fromPython.replace("(", "");
		fromPython = fromPython.replace(")", "");
		
		String[] data = fromPython.split(",");
		
		if(Integer.parseInt(data[0].trim()) != -1) {
			toReturn += mapping[Integer.parseInt(data[0].trim())];
		}
		
		toReturn += mapping[Integer.parseInt(data[1].trim())];
		
		if(Integer.parseInt(data[2].trim()) != -1) {
			toReturn += mapping[Integer.parseInt(data[2].trim())];
		}
		
		return toReturn;
	}
	
	private static Action stringToAction(String actionString, Phase fase) {
		if (fase == Phase.FIRST) { // prima fase
			Phase1Action action;
			action = new Phase1Action();
			action.setPutPosition(actionString.substring(0, 2));
			if (actionString.length() == 4)
				action.setRemoveOpponentChecker(actionString.substring(2, 4));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		} else if (fase == Phase.SECOND) { // seconda fase
			Phase2Action action;
			action = new Phase2Action();
			action.setFrom(actionString.substring(0, 2));
			action.setTo(actionString.substring(2, 4));
			if (actionString.length() == 6)
				action.setRemoveOpponentChecker(actionString.substring(4, 6));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		} else { // ultima fase
			PhaseFinalAction action;
			action = new PhaseFinalAction();
			action.setFrom(actionString.substring(0, 2));
			action.setTo(actionString.substring(2, 4));
			if (actionString.length() == 6)
				action.setRemoveOpponentChecker(actionString.substring(4, 6));
			else
				action.setRemoveOpponentChecker(null);
			return action;
		}
	}

	
}
