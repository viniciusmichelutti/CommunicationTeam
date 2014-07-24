package controllers;

import actors.Room;

import com.fasterxml.jackson.databind.JsonNode;

import play.*;
import play.data.Form;
import play.mvc.*;
import utils.HashUtils;
import views.html.*;

public class Application extends Controller {

    public static Result index() {
    	if (session("email") != null) {
    		return redirect(routes.Application.chat());
    	}
    	
        return ok(index.render());
    }

    public static Result enter() {
    	Form<User> form = Form.form(User.class).bindFromRequest();
    	User user = form.get();
    	
    	if (user.email.trim().isEmpty() || user.name.trim().isEmpty()) {
    		flash("error", "Please, fill all the fields.");
    		return redirect(routes.Application.index());
    	}
    	
    	session("email", user.email);
    	session("name", user.name);
    	return redirect(routes.Application.chat());
    }
    
    public static Result chat() {
    	if (session("email") == null) {
    		return redirect(routes.Application.enter());
    	}
    	
    	return ok(views.html.chat.index.render());
    }
    
    public static WebSocket<JsonNode> chatWeb() {
    	final models.User user = new models.User();
    	user.email = session("email");
    	user.name = session("name");
    	
    	return new WebSocket<JsonNode>() {
    		@Override
    		public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
    			
    			try {
					Room.join(user, in, out);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			
    		}
    	};
    }
    
    public static Result getHashFromEmail(String email) {
    	return ok(HashUtils.generateHash(email));
    }
    
    public static Result logout() {
    	session().clear();
    	return redirect(routes.Application.index());
    }
    
    public static class User {

    	public String email;
    	public String name;
    	
    	public String validate() {
    		return null;
    	}
    	
    }
    
}
