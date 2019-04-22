package controllers

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc._
import repositories.AccountsRepository

import scala.concurrent.{ExecutionContext, Future}

class AccountController @Inject()(cc: MessagesControllerComponents, repo: AccountsRepository)(implicit ec: ExecutionContext) extends MessagesAbstractController(cc) {

  private val logger = Logger(this.getClass)

  /**
    * The mapping for the account form.
    */
  val accountForm: Form[CreateAccountForm] = Form {
    mapping(
      "email" -> nonEmptyText,
      "password" -> nonEmptyText,
      "category" -> nonEmptyText,
      "city" -> nonEmptyText,
    )(CreateAccountForm.apply)(CreateAccountForm.unapply)
  }


  /**
    * The index action.
    */
  def index = Action { implicit request =>
    Ok(views.html.index(accountForm))
  }

  /**
    * A REST endpoint that gets all the accounts as JSON.
    */
  def getAccounts = Action.async { implicit request =>
    repo.list().map { account =>
      Ok(Json.toJson(account))
    }
  }

  /**
    * The add account action.
    *
    * This is asynchronous, since we're invoking the asynchronous methods on PersonRepository.
    */
  def createAccount = Action.async { implicit request =>
    // Bind the form first, then fold the result, passing a function to handle errors, and a function to handle succes.
    accountForm.bindFromRequest.fold(
      // The error function. We return the index page with the error form, which will render the errors.
      // We also wrap the result in a successful future, since this action is synchronous, but we're required to return
      // a future because the person creation function returns a future.
      errorForm => {
        Future.successful(Ok(views.html.index(errorForm)))
      },
      // There were no errors in the from, so create the person.
      account => {
        repo.create(account.email, account.password, account.category, account.city).map { _ =>
          // If successful, we simply redirect to the index page.
          Redirect(routes.AccountController.index).flashing("success" -> "account.created")
        }
      }
    )
  }


}


case class CreateAccountForm(email: String, password: String, category: String, city: String)


