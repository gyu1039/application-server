package webserver;

public interface Controller {

	public void service(HttpRequest request, HttpResponse response);
}
