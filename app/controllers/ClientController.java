package controllers;

import models.Task;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.client;
import views.html.creerClient;
import views.html.tests;

import static play.mvc.Results.ok;

/**
 * Created by Gishan on 03/01/2016.
 */
public class ClientController extends Controller {

    public Result afficherClients() {
        return ok(client.render());
    }

    public Result afficherCreerClient() {
        return ok(creerClient.render());
    }


}