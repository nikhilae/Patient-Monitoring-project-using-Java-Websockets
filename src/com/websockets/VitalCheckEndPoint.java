package com.websockets;
import javax.websocket.server.ServerEndpoint;

import java.io.StringWriter;
import java.util.Collections;
import java.util.Set;
import java.util.HashSet;
import javax.websocket.*;
import javax.json.*;
@ServerEndpoint(value="/VitalCheckEndPoint",configurator=VitalCheckconfigurator.class)

public class VitalCheckEndPoint {
	static Set<Session> subscribers=Collections.synchronizedSet(new HashSet<Session>());
	
	@OnOpen
	public void handleOpen(EndpointConfig endpointconfig,Session userSession) {
		userSession.getUserProperties().put("username",endpointconfig.getUserProperties().get("username") );
		subscribers.add(userSession);
		
	}
	
	@OnMessage
	public void handleMessage(String Message,Session userSession) {
		String username=(String) userSession.getUserProperties().get("username");
		
		if(username!=null && !username.equals("doctor"))
		{
			subscribers.stream().forEach(x->{
				try {
					if(x.getUserProperties().get("username").equals("doctor")) {
						x.getBasicRemote().sendText(buildJson(username,Message));
					}
				}
				catch(Exception e) {
					e.getStackTrace();
				}	
			});	
		}
		else if(username!=null && username.equals("doctor"))
		{
			String[] messages=Message.split(",");
			String patient =messages[0];
			String subject=messages[1];
			subscribers .stream().forEach(x->{
				try {
					if(subject.equals("ambulance")) {
						if(x.getUserProperties().get("username").equals(patient)) {
							x.getBasicRemote().sendText(buildJson("doctor","has summoned an ambulance"));
						}
						else if(x.getUserProperties().get("username").equals("ambulance")) {
							x.getBasicRemote().sendText(buildJson(patient,"requires an ambulance"));
						}
					}
					else if(subject.equals("medication")) {
						if(x.getUserProperties().get("username").equals(patient)) {
							x.getBasicRemote().sendText(buildJson("doctor",messages[2]+","+messages[3]));
						}
					}
					
					
					
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			});
		}
		}
	
	@OnClose
	public void handleClose(Session userSession) {
		subscribers.remove(userSession);
		
	}

	@OnError
	public void handleError(Throwable t)
	{
		
		
	}
	private String buildJson(String username, String message) {
		// TODO Auto-generated method stub
		
		JsonObject jsonobject =Json.createObjectBuilder().add("message", username+","+message).build();
		StringWriter stringWriter= new StringWriter();
		
		
		try(JsonWriter jsonWriter=Json.createWriter(stringWriter)) {
			jsonWriter.write(jsonobject);
		}
		return stringWriter.toString();
	}

}
