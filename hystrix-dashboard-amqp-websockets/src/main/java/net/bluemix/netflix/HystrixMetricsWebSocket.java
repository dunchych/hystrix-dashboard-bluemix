
/**
 * Implement streaming of Hystrix metrics over a websocket 
 * Follows same logic as SEE streaming in 
 * com.netflix.hystrix.contrib.metrics.eventstreamHystrixMetricsStreamServlet
 */

package net.bluemnix.netflix;

import java.io.IOException;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

import com.rabbitmq.client.*;

import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import net.bluemix.vcap.IVCapServiceCredentials;
import net.bluemix.vcap.impl.VCapServices;
import net.bluemix.vcap.impl.VCapServiceCredentials;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



@ServerEndpoint(value = "/hystrix.stream.ws")

public class HystrixMetricsWebSocket {

    private final Set<Session> sessions = new HashSet<Session>();

    public void addSession(Session session) {
        sessions.add(session);
    }

    public void removeSession(Session session) {
        sessions.remove(session);
    }
	

    private final static String EXCHANGE_NAME = "springCloudHystrixStream";
    private final static String QUEUE_NAME    = "#";
    private final static String ROUTING_KEY   = "#";

    private  static Connection connection = null;
    private  static Channel channel = null;
    private  static boolean isConnected = false;

    private Thread metricsLoop = null;


	
	@OnOpen
	public void onOpen(Session session, EndpointConfig ec) {
		// Store the WebSocket session for later use.
        addSession(session);
        //isConnected = initRMQ();
         //spin off a thread for observing metrics and sending them to RMQ
        if (metricsLoop == null) {
            metricsLoop  = new Thread (new MetricsLoop());
            metricsLoop.start();
        }
        
	}
			
	@OnMessage
	public void receiveMessage(String messageIn) {	

      //      handleRequest();
        System.out.println("HystrixMetricsWebSocket: OnMessage " + messageIn);
	}

	
	@OnClose
	public void onClose(Session session, CloseReason reason) {
		System.out.println("HystrixMetricsWebSocket: OnClose " + reason);
        removeSession(session);
	}
	
	@OnError
	public void onError(Throwable t) {
		System.err.println("HystrixMetricsWebSocket: OnError " + t);
		t.printStackTrace();
	}
	
	/**
	 * Send a message to all clients 
	 * @param message
	 */
	public void sendMessage(String message){
        if (sessions != null) {
            Iterator itr = sessions.iterator();
            while(itr.hasNext()) {
                 Session s = (Session) itr.next();
                 try {
                    s.getBasicRemote().sendText(message);
                 } catch (IOException ioe) {
                    System.err.println("HystrixMetricsWebSocket.sendMessage: error  " + ioe);
                    ioe.printStackTrace();
                 }
            }
	   }
    }

 

    private  static boolean initRMQ() {

        String uri = null;

        //Process VCAP 
        VCapServices vservices = new VCapServices();
        IVCapServiceCredentials cred = VCapServiceCredentials.getCredentialsOfFirstService(vservices, "compose-for-rabbitmq");
        if (cred == null) {
                System.err.println("HystrixMetricsWebSocket: VCAP_SERVICES environment variable not found, initialization failed");
                return false;
        }

        uri = cred.getURI();
        System.out.println("HystrixMetricsWebSocket: VCAP connection uri " + uri);
      

        //initilize RMQ connection/channel
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUri(uri);
            factory.setRequestedHeartbeat(120);
            connection = factory.newConnection();
            channel = connection.createChannel();   
            //a durable, non-autodelete exchange of "direct" type
            channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
            //a durable, non-exclusive, non-autodelete queue with a well-known name
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);
            System.out.println("HystrixMetricsWebSocket: RMQ channel established: "+ channel);
            return true;

        } catch (Throwable e) {
            System.err.println("HystrixMetricsWebSocket: Error establishing RMQ channel : "+ e);
            e.printStackTrace();
            return false;
        }

    }



    private class MetricsLoop implements Runnable {

         public void run() {

            //establish connection to RMQ in this thread
            isConnected = initRMQ();
            if (!isConnected) {
                System.err.println("HystrixMetricsCollectionServlet: can't connect to RMQ, exiting message loop");
                return;
            }   

            while (isConnected) {
                try {
                    boolean noAck = false;
                    QueueingConsumer consumer = new QueueingConsumer(channel);
                    channel.basicConsume(QUEUE_NAME, noAck, consumer);
                    boolean runInfinite = true;
                    while (runInfinite) {
                        //get a message from RMQ 
                        QueueingConsumer.Delivery delivery;
                        try {
                            delivery = consumer.nextDelivery();
                        } catch (InterruptedException ie) {
                            continue;
                        }
                        String message = new String(delivery.getBody(), "UTF-8");
                        System.out.println("Message received: " + message);
                        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                        //TurbineAMQP message is like this:
                        //{"origin":{"host":"192.168.1.10","port":8080,"serviceId":"sample-hystrix-app"},"data":{"type":"HystrixCommand","name":"sxxx etc
                        //origin is used by Turbine client to aggregate data by origin for extra efficiency by "compressing" metrics sent to the browser dashboard
                        //this implementation simply relays metric messages, so we simply extract metrics data like this
                        //{"type":"HystrixCommand","name":"sxxx etc
                        message = extractData(message);
                        //push tha message to the browser dashnoard via web socket
                        sendMessage(message);  
                    }
                } catch (IOException ioe) {
                        System.err.println("HystrixMetricsWebSocket:  error getting message from RMQ " + ioe);
                        System.err.println("HystrixMetricsWebSocket:  attempting to reconnect....");
                        isConnected = initRMQ();
                        if (isConnected) {
                            System.out.println("HystrixMetricsCollectionServlet: reconnected OK");
                        } else {
                            System.err.println("HystrixMetricsCollectionServlet: can't reconnect to RMQ, exiting message loop..");
                        }


                }  
            }                   
        }  

        private String extractData (String message) {
            JSONObject obj = new JSONObject(message);
            obj = (JSONObject) obj.get("data");
            return obj.toString();

        }  
    }

}
