package actors;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import models.User;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Akka;
import play.libs.F.Callback;
import play.libs.F.Callback0;
import play.libs.Json;
import play.mvc.WebSocket;
import scala.concurrent.Await;
import scala.concurrent.duration.Duration;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.pattern.Patterns;

public class Room extends UntypedActor {

	static ActorRef room = Akka.system().actorOf(Props.create(Room.class));
	Map<User, WebSocket.Out<JsonNode>> onlineUsers = new HashMap<User, WebSocket.Out<JsonNode>>();
	
	public static void join(final User usr, WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) throws Exception {
		String result = (String) Await.result(Patterns.ask(room, new Join(usr, out), 1000), Duration.create(1, TimeUnit.SECONDS));
		
		if ("OK".equals(result)) {
			
			in.onMessage(new Callback<JsonNode>() {
				public void invoke(JsonNode event) {
					room.tell(new Talk(usr, event.get("msg").asText()), null);
				} 
            });
            
			in.onClose(new Callback0() {
				public void invoke() {
					room.tell(new Quit(usr), null);
				}
			});
			
		} else {
			ObjectNode error = Json.newObject();
            error.put("error", result);
            out.write(error);
		}
	}
	
	@Override
	public void onReceive(Object message) throws Exception {
		if (message instanceof Join) {
			
			Join j = (Join) message;
			if (onlineUsers.containsKey(j.user)) {
				getSender().tell("This username is already used", getSelf());
			} else {
				onlineUsers.put(j.user, j.out);
				notifyAll(EventTypes.JOIN, j.user, "has entered the room");
				getSender().tell("OK", getSelf());
			}
			
		} else if (message instanceof Talk) {
			
			Talk t = (Talk) message;
			notifyAll(EventTypes.MESSAGE, t.user, t.message);
			
		} else if (message instanceof Quit) {
			
			Quit q = (Quit) message;
			notifyAll(EventTypes.QUIT, q.user, "has left the room");
			onlineUsers.remove(q.user);
			
		} else {
			unhandled(message);
		}
	}

	public void notifyAll(EventTypes type, User user, String text) {
		 for(WebSocket.Out<JsonNode> channel : onlineUsers.values()) {
            ObjectNode event = Json.newObject();

            event.put("type", type.getName());
            event.put("username", user.name);
            event.put("email", user.email);
            event.put("message", text);
            
            channel.write(event);
        }
	}
	
	public static class Join {
		
		public User user;
		public WebSocket.Out<JsonNode> out;
		
		public Join(User user, WebSocket.Out<JsonNode> out) {
			this.user = user;
			this.out = out;
		}
	}
	
	public static class Talk {
		public User user;
		public String message;
		
		public Talk(User user, String message) {
			this.user = user;
			this.message = message;
		}
	}
	
	public static class Quit {
		public User user;
		
		public Quit(User user) {
			this.user = user;
		}
	}
	
}
