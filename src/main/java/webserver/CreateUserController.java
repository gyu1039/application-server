package webserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;

public class CreateUserController implements Controller {

	private static final Logger log = LoggerFactory.getLogger(CreateUserController.class);
	
	@Override
	public void service(HttpRequest request, HttpResponse response) {

		User user1 = new User(
				request.getParameter("userId"),
				request.getParameter("password"),
				request.getParameter("name"),
				request.getParameter("email"));

		DataBase.addUser(user1);
		response.sendRedirect("/index.html");

	}

}
