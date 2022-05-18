package webserver;

import java.util.Collection;
import java.util.Map;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;

public class ListUserController extends AbstractController{

	@Override
	public void doGet(HttpRequest request, HttpResponse response) {
		if(!isLogin(request.getSession())) {
			response.sendRedirect("/user/login.html");
			return;
		}

		Collection<User> users = DataBase.findAll();
		StringBuilder sb = new StringBuilder();
		sb.append("<table border='1'>");
		for(User user : users) {
			sb.append("<tr>");
			sb.append("<td>" + user.getUserId() + "</td>");
			sb.append("<td>" + user.getName() + "</td>");
			sb.append("<td>" + user.getEmail() + "</td>");
			sb.append("</tr>");
		}
		sb.append("</table>");

		response.forwardBody(sb.toString());

	}

	private static boolean isLogin(HttpSession session) {

		Object user = session.getAttribute("user");
		if(user == null) return false;
		return true;
	}

	@Override
	protected void doPost(HttpRequest request, HttpResponse response) {
		// TODO Auto-generated method stub
		
	}
}
